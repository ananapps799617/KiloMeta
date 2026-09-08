// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/CalculateShiftCashSettlementUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso que computa la liquidación de doble bolsillo del conductor:
// 1. Efectivo físico disponible en mano (Recaudo cash - combustible - gastos en ruta).
// 2. Saldo retenido en billeteras digitales de plataformas pendiente de desembolso semanal.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.CashSettlementSummary
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.PaymentDestination
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
        var totalCommission = 0.0

        trips.forEach { trip ->
            if (trip.paymentDestination == PaymentDestination.CASH_IN_HAND) {
                cashGross += trip.grossAmount
            } else {
                digitalGross += trip.grossAmount
                digitalCommissions += trip.commissionAmount
            }
            totalCommission += trip.commissionAmount
        }

        val totalGross = cashGross + digitalGross
        val totalNet = totalGross - totalCommission

        val totalFueling = fuelings.sumOf { it.costCop }
        val totalExpenses = expenses.sumOf { it.amountCop }

        // El combustible y los gastos en ruta se descuentan de la caja física disponible
        val cashAvailable = cashGross - totalFueling - totalExpenses
        val digitalPending = (digitalGross - digitalCommissions).coerceAtLeast(0.0)

        return CashSettlementSummary(
            cashInHandGross = cashGross,
            digitalBalanceGross = digitalGross,
            totalNet = totalNet,
            cashInHandAvailable = cashAvailable,
            digitalPendingDisbursement = digitalPending
        )
    }
}
