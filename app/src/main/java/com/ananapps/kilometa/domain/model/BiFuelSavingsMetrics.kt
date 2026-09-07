// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/BiFuelSavingsMetrics.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio que consolida la auditoría de ahorro del sistema bi-combustible.
// Compara el gasto real incurrido en Gas Natural Vehicular (GNV) y gasolina frente
// al gasto hipotético equivalente en 100% gasolina corriente.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO INMUTABLE DE MÉTRICAS DE AHORRO BI-COMBUSTIBLE
// ------------------------------------------------------------------------------
/**
 * Cuantificación económica del ahorro logrado al operar con GNV en comparación con gasolina.
 *
 * @property totalKmDriven Kilómetros totales recorridos en el período analizado.
 * @property totalGasExpense Dinero gastado efectivamente en recargas de GNV (COP).
 * @property totalGasolineExpense Dinero gastado en gasolina corriente de apoyo o emergencia (COP).
 * @property actualFuelExpense Gasto real consolidado de combustible (gas + gasolina) en COP.
 * @property equivalentGasolineExpense Gasto proyectado que habría costado recorrer los mismos km en gasolina.
 * @property netSavingsAmount Ahorro monetario neto en COP (equivalentGasolineExpense - actualFuelExpense).
 * @property savingsPercentage Porcentaje relativo de ahorro frente al escenario de gasolina 100%.
 * @property costPerKmGas Costo unitario real por kilómetro derivado del GNV (totalGasExpense / totalKmDriven).
 * @property costPerKmGasoline Costo unitario real por kilómetro derivado de gasolina (totalGasolineExpense / totalKmDriven).
 */
data class BiFuelSavingsMetrics(
    val totalKmDriven: Int,
    val totalGasExpense: Double,
    val totalGasolineExpense: Double,
    val actualFuelExpense: Double,
    val equivalentGasolineExpense: Double,
    val netSavingsAmount: Double,
    val savingsPercentage: Double,
    val costPerKmGas: Double,
    val costPerKmGasoline: Double
) {
    // --------------------------------------------------------------------------
    // BLOQUE 2: PROPIEDADES DERIVADAS DE AUDITORÍA
    // --------------------------------------------------------------------------
    /**
     * Costo total consolidado de combustible por kilómetro recorrido ($/km).
     */
    val blendedCostPerKm: Double
        get() = if (totalKmDriven > 0) {
            actualFuelExpense / totalKmDriven
        } else {
            0.0
        }

    /**
     * Confirma si la operación arrojó un ahorro positivo frente a gasolina tradicional.
     */
    val hasPositiveSavings: Boolean
        get() = netSavingsAmount > 0.0
}
