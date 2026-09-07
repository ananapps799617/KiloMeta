// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/ShiftFinancialSummary.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio que consolida el estado financiero integral de un turno.
// Agrega ingresos netos, gastos de combustible bi-modal, gastos operativos,
// avance de la cuota diaria y costos unitarios por kilómetro recorrido.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO INMUTABLE DEL RESUMEN FINANCIERO
// ------------------------------------------------------------------------------
/**
 * Agregado financiero calculado en tiempo real para la toma de decisiones en cabina.
 */
data class ShiftFinancialSummary(
    val shiftId: String,
    val status: ShiftStatus,
    val startTime: Long,
    val endTime: Long? = null,
    val startOdometer: Int,
    val endOdometer: Int? = null,
    val totalKilometers: Int,
    val dailyQuotaTarget: Double,
    val totalGrossIncome: Double,
    val totalCommissions: Double,
    val totalNetIncome: Double,
    val totalGasExpense: Double,
    val totalGasolineExpense: Double,
    val totalFuelExpense: Double,
    val totalOperationalExpenses: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val quotaProgressPercentage: Double,
    val costPerKilometer: Double,
    val grossIncomePerKilometer: Double,
    val netIncomePerKilometer: Double,
    val tripsCount: Int,
    val fuelingsCount: Int,
    val expensesCount: Int
) {
    // --------------------------------------------------------------------------
    // BLOQUE 2: PROPIEDADES DERIVADAS
    // --------------------------------------------------------------------------
    /**
     * Monto pendiente para alcanzar la cuota fijada (0 si ya se cumplió).
     */
    val remainingForQuota: Double
        get() = if (netProfit >= dailyQuotaTarget) 0.0 else (dailyQuotaTarget - netProfit)

    /**
     * Indica si la jornada ya generó beneficio neto positivo cubriendo la meta.
     */
    val isQuotaAchieved: Boolean
        get() = netProfit >= dailyQuotaTarget
}
