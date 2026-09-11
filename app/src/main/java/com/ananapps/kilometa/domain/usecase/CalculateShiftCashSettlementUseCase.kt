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
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.PlatformWalletBalance
import com.ananapps.kilometa.domain.model.Trip
import javax.inject.Inject

class CalculateShiftCashSettlementUseCase @Inject constructor() {

    operator fun invoke(
        trips: List<Trip>,
        fuelings: List<Fueling>,
        expenses: List<Expense>
    ): CashSettlementSummary {
        var cashGross = 0.0
        var digitalGross = 0.0
        var digitalCommissions = 0.0
        var cashCommissions = 0.0
        var totalCommission = 0.0

        val platformMap = mutableMapOf<PlatformType, MutablePlatformAccumulator>()
        PlatformType.entries.forEach {
            platformMap[it] = MutablePlatformAccumulator(it)
        }

        trips.forEach { trip ->
            val acc = platformMap.getOrPut(trip.platformType) { MutablePlatformAccumulator(trip.platformType) }
            if (trip.paymentDestination == PaymentDestination.CASH_IN_HAND) {
                cashGross += trip.grossAmount
                cashCommissions += trip.commissionAmount
                acc.cashGross += trip.grossAmount
                acc.cashCommissionOwed += trip.commissionAmount
            } else {
                digitalGross += trip.grossAmount
                digitalCommissions += trip.commissionAmount
                acc.digitalGross += trip.grossAmount
                acc.digitalCommission += trip.commissionAmount
            }
            totalCommission += trip.commissionAmount
        }

        val totalGross = cashGross + digitalGross
        val totalNet = totalGross - totalCommission

        val totalFueling = fuelings.sumOf { it.amountPaid }
        val totalExpenses = expenses.sumOf { it.amount }

        // El combustible y los gastos en ruta se descuentan de la caja física disponible
        val cashAvailable = cashGross - totalFueling - totalExpenses
        val digitalPending = (digitalGross - digitalCommissions).coerceAtLeast(0.0)
        // Cruce global referencial
        val netPlatformBalance = (digitalGross - digitalCommissions) - cashCommissions

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
            cashInHandGross = cashGross,
            digitalBalanceGross = digitalGross,
            totalNet = totalNet,
            cashInHandAvailable = cashAvailable,
            digitalPendingDisbursement = digitalPending,
            cashCommissionsOwed = cashCommissions,
            netPlatformBalance = netPlatformBalance,
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
