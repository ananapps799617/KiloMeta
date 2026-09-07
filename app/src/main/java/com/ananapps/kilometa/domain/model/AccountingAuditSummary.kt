// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/AccountingAuditSummary.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio para el Dashboard de Auditoría Contable Avanzada.
// Consolida las métricas clave de la semana contable (Miércoles a Martes 23:59:59),
// ratios de rentabilidad por kilómetro, eficiencia bi-combustible y estado cloud.
// Regla de Oro: Inmutabilidad estricta (val), 0% de Any y cero dependencias de Android.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO INMUTABLE DE AUDITORÍA CONTABLE AVANZADA
// ------------------------------------------------------------------------------
/**
 * Agregado financiero integral para la auditoría contable vehicular del conductor.
 *
 * @property cycleStartDateMillis Marca de tiempo de inicio del ciclo semanal (Miércoles 00:00:00).
 * @property cycleEndDateMillis Marca de tiempo de fin del ciclo semanal (Martes 23:59:59).
 * @property totalGrossIncome Ingresos brutos generados en el ciclo antes de comisiones (COP).
 * @property totalNetIncomeInHand Ingreso neto en mano (después de deducir comisiones de plataformas).
 * @property totalCommissions Total de deducciones o comisiones cobradas por aplicaciones intermediarias.
 * @property totalKilometers Total de kilómetros rodados registrados durante las jornadas del ciclo.
 * @property costPerKilometer Costo operativo por kilómetro rodado (COP/km).
 * @property revenuePerKilometer Ingreso bruto generado por kilómetro rodado (COP/km).
 * @property fuelExpenseToIncomeRatio Proporción porcentual del gasto en combustible frente al ingreso bruto.
 * @property totalGasExpense Gasto total en Gas Natural Vehicular (GNV) del ciclo.
 * @property totalGasolineExpense Gasto total en Gasolina corriente del ciclo.
 * @property totalOperationalExpenses Otros gastos de operación (lavado, peajes, mecánicos, etc.).
 * @property netProfit Utilidad líquida final (ingreso neto en mano menos combustible y otros gastos).
 * @property quotaProgress Progreso prorrateado hacia la meta semanal de bolsa ($500.000 COP).
 * @property lastCloudSyncMillis Marca de tiempo del último respaldo exitoso en Google Cloud Firestore.
 * @property isCloudSyncHealthy Indica si el estado de la sincronización en la nube se encuentra operativo.
 */
data class AccountingAuditSummary(
    val cycleStartDateMillis: Long,
    val cycleEndDateMillis: Long,
    val totalGrossIncome: Double,
    val totalNetIncomeInHand: Double,
    val totalCommissions: Double,
    val totalKilometers: Int,
    val costPerKilometer: Double,
    val revenuePerKilometer: Double,
    val fuelExpenseToIncomeRatio: Double,
    val totalGasExpense: Double,
    val totalGasolineExpense: Double,
    val totalOperationalExpenses: Double,
    val netProfit: Double,
    val quotaProgress: WeeklyQuotaProgress,
    val lastCloudSyncMillis: Long?,
    val isCloudSyncHealthy: Boolean
) {
    // --------------------------------------------------------------------------
    // BLOQUE 2: PROPIEDADES CALCULADAS Y REGLAS DE NEGOCIO CONTABLES
    // --------------------------------------------------------------------------
    /**
     * El ratio de gasto de combustible se considera óptimo si no supera el 25.0%
     * del total de ingresos brutos generados (parámetro estándar de rentabilidad bi-combustible).
     */
    val isFuelEfficiencyOptimal: Boolean
        get() = fuelExpenseToIncomeRatio <= 25.0

    /**
     * Gasto total de combustible consolidando gas y gasolina.
     */
    val totalFuelExpense: Double
        get() = totalGasExpense + totalGasolineExpense

    /**
     * Ahorro estimado por uso de GNV frente a gasolina equivalente.
     * En el modelo bi-combustible de Bogotá, el GNV suele generar ~40% de ahorro por km.
     */
    val estimatedGasSavingsPercentage: Double
        get() = if (totalFuelExpense > 0.0 && totalGasExpense > 0.0) {
            ((totalGasExpense / totalFuelExpense) * 40.0).coerceIn(0.0, 40.0)
        } else {
            0.0
        }
}
