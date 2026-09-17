// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/CalculateShiftCashSettlementUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso que computa la liquidación de doble bolsillo del conductor:
// 1. Efectivo físico disponible en mano (Recaudo cash - combustible - gastos en ruta).
// 2. Billeteras independientes por plataforma (Uber vs DiDi vs inDrive) sin cruces ilícitos.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.CashSettlementSummary
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.PlatformWalletBalance
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.domain.model.WalletTransfer
import com.ananapps.kilometa.domain.model.WalletType
import javax.inject.Inject

class CalculateShiftCashSettlementUseCase @Inject constructor() {

    operator fun invoke(
        trips: List<Trip>,
        fuelings: List<Fueling>,
        expenses: List<Expense>,
        transfers: List<WalletTransfer> = emptyList(),
        platformTrips: List<Trip> = trips
    ): CashSettlementSummary {
        var cashPhysicalGross = 0.0
        var digitalWalletGross = 0.0
        var digitalGross = 0.0
        var digitalCommissions = 0.0
        var cashCommissions = 0.0
        var totalCommission = 0.0

        val platformMap = mutableMapOf<PlatformType, MutablePlatformAccumulator>()
        PlatformType.entries.forEach {
            platformMap[it] = MutablePlatformAccumulator(it)
        }

        trips.forEach { trip ->
            val netTripCash = (trip.grossAmount - trip.platformPromoDiscount).coerceAtLeast(0.0)
            when (trip.paymentMethod) {
                PaymentMethod.CASH -> {
                    cashPhysicalGross += netTripCash
                    cashCommissions += trip.commissionAmount
                }
                PaymentMethod.NEQUI -> {
                    digitalWalletGross += netTripCash
                    cashCommissions += trip.commissionAmount
                }
                PaymentMethod.APP_WALLET -> {
                    digitalGross += trip.grossAmount
                    digitalCommissions += trip.commissionAmount
                }
            }
            totalCommission += trip.commissionAmount
        }

        // Acumulación soberana de bolsillos de plataforma (Ciclo Lunes a Domingo)
        platformTrips.forEach { trip ->
            val acc = platformMap.getOrPut(trip.platformType) { MutablePlatformAccumulator(trip.platformType) }
            when (trip.paymentMethod) {
                PaymentMethod.CASH, PaymentMethod.NEQUI -> {
                    acc.cashGross += trip.grossAmount
                    acc.cashCommissionOwed += trip.commissionAmount
                }
                PaymentMethod.APP_WALLET -> {
                    acc.digitalGross += trip.grossAmount
                    acc.digitalCommission += trip.commissionAmount
                }
            }
        }

        // Deducción de combustible y gastos por origen soberano de fondos
        val fuelCash = fuelings.filter { it.paymentSource == WalletType.CASH_PHYSICAL }.sumOf { it.amountPaid }
        val fuelDigital = fuelings.filter { it.paymentSource == WalletType.DIGITAL_WALLET }.sumOf { it.amountPaid }

        val expenseCash = expenses.filter { it.paymentSource == WalletType.CASH_PHYSICAL }.sumOf { it.amount }
        val expenseDigital = expenses.filter { it.paymentSource == WalletType.DIGITAL_WALLET }.sumOf { it.amount }
        val expenseCarQuota = expenses.filter { it.paymentSource == WalletType.CAR_QUOTA }.sumOf { it.amount }

        // Transferencias inter-bolsillos (entradas y salidas por cada cartera)
        fun netTransfersFor(wallet: WalletType): Double {
            val incoming = transfers.filter { it.destinationWallet == wallet }.sumOf { it.amount }
            val outgoing = transfers.filter { it.sourceWallet == wallet }.sumOf { it.amount }
            return incoming - outgoing
        }

        val cashPhysicalBalance = cashPhysicalGross - fuelCash - expenseCash + netTransfersFor(WalletType.CASH_PHYSICAL)
        val digitalWalletBalance = digitalWalletGross - fuelDigital - expenseDigital + netTransfersFor(WalletType.DIGITAL_WALLET)

        val uberBase = (platformMap[PlatformType.UBER]?.let { it.digitalGross - it.digitalCommission - it.cashCommissionOwed } ?: 0.0)
        val uberWalletBalance = uberBase + netTransfersFor(WalletType.UBER)

        val didiBase = (platformMap[PlatformType.DIDI]?.let { it.digitalGross - it.digitalCommission - it.cashCommissionOwed } ?: 0.0)
        val didiWalletBalance = didiBase + netTransfersFor(WalletType.DIDI)

        val carQuotaWalletBalance = netTransfersFor(WalletType.CAR_QUOTA) - expenseCarQuota

        val totalNetAssets = cashPhysicalBalance + digitalWalletBalance + uberWalletBalance + didiWalletBalance + carQuotaWalletBalance

        val totalGross = cashPhysicalGross + digitalWalletGross + digitalGross
        val totalNet = totalGross - totalCommission

        val wallets = platformMap.mapValues { (_, acc) ->
            PlatformWalletBalance(
                platformType = acc.type,
                digitalGross = acc.digitalGross,
                digitalCommission = acc.digitalCommission,
                cashCommissionOwed = acc.cashCommissionOwed,
                cashGross = acc.cashGross
            )
        }

        return CashSettlementSummary(
            cashPhysicalBalance = cashPhysicalBalance,
            digitalWalletBalance = digitalWalletBalance,
            uberWalletBalance = uberWalletBalance,
            didiWalletBalance = didiWalletBalance,
            carQuotaWalletBalance = carQuotaWalletBalance,
            totalNetAssets = totalNetAssets,
            cashInHandGross = cashPhysicalGross,
            digitalBalanceGross = digitalGross + digitalWalletGross,
            totalNet = totalNet,
            cashInHandAvailable = cashPhysicalBalance,
            digitalPendingDisbursement = (digitalGross - digitalCommissions).coerceAtLeast(0.0),
            cashCommissionsOwed = cashCommissions,
            netPlatformBalance = (digitalGross - digitalCommissions) - cashCommissions,
            uberWallet = wallets[PlatformType.UBER] ?: PlatformWalletBalance(PlatformType.UBER),
            didiWallet = wallets[PlatformType.DIDI] ?: PlatformWalletBalance(PlatformType.DIDI),
            indriveWallet = wallets[PlatformType.INDRIVE] ?: PlatformWalletBalance(PlatformType.INDRIVE),
            platformWallets = wallets
        )
    }

    private class MutablePlatformAccumulator(
        val type: PlatformType,
        var digitalGross: Double = 0.0,
        var digitalCommission: Double = 0.0,
        var cashCommissionOwed: Double = 0.0,
        var cashGross: Double = 0.0
    )
}
