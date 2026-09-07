// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/BiFuelCostMetrics.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo inmutable de métricas de costo por kilómetro del sistema bi-combustible.
// Desglosa el costo unitario por km para Gas Natural Vehicular (GNV), Gasolina
// y el costo ponderado mixto (blended), protegiendo contra división por cero.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO INMUTABLE DE MÉTRICAS BI-COMBUSTIBLE
// ------------------------------------------------------------------------------
/**
 * Métricas de rendimiento y costo kilométrico del vehículo bi-combustible.
 *
 * @property totalKilometers Kilómetros recorridos durante la muestra o turno.
 * @property gasCostTotal Gasto total incurrido en Gas Natural Vehicular (COP).
 * @property gasolineCostTotal Gasto total incurrido en Gasolina Corriente (COP).
 * @property totalFuelCost Gasto combinado de combustible (COP).
 * @property gasCostPerKm Costo unitario en gas por kilómetro (COP/km).
 * @property gasolineCostPerKm Costo unitario en gasolina por kilómetro (COP/km).
 * @property blendedCostPerKm Costo combinado total por kilómetro recorrido (COP/km).
 */
data class BiFuelCostMetrics(
    val totalKilometers: Int,
    val gasCostTotal: Double,
    val gasolineCostTotal: Double,
    val totalFuelCost: Double,
    val gasCostPerKm: Double,
    val gasolineCostPerKm: Double,
    val blendedCostPerKm: Double
) {
    // --------------------------------------------------------------------------
    // BLOQUE 2: PROPIEDADES DERIVADAS DE EFICIENCIA
    // --------------------------------------------------------------------------
    /**
     * Porcentaje del gasto total atribuible a Gas Natural Vehicular.
     */
    val gasExpenditureShare: Double
        get() = if (totalFuelCost > 0.0) {
            (gasCostTotal / totalFuelCost) * 100.0
        } else {
            0.0
        }

    /**
     * Porcentaje del gasto total atribuible a Gasolina.
     */
    val gasolineExpenditureShare: Double
        get() = if (totalFuelCost > 0.0) {
            (gasolineCostTotal / totalFuelCost) * 100.0
        } else {
            0.0
        }
}
