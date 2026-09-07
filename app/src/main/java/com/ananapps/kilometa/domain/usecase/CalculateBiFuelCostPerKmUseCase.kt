// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/CalculateBiFuelCostPerKmUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para el análisis de costos del sistema bi-combustible (GNV vs Gasolina).
// Segmenta los costos unitarios por kilómetro y el costo mixto ponderado,
// protegiendo matemáticamente contra división por cero cuando el vehículo no ha rodado (km = 0).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.BiFuelCostMetrics
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CALCULADOR DE COSTOS BI-COMBUSTIBLE POR KILÓMETRO
// ------------------------------------------------------------------------------
/**
 * Procesa el kilometraje acumulado y los egresos de combustible bi-modal
 * para determinar el costo por kilómetro real en cabina.
 */
class CalculateBiFuelCostPerKmUseCase @Inject constructor() {

    /**
     * Calcula métricas a partir de valores numéricos explícitos.
     *
     * @param totalKilometers Kilómetros recorridos por el vehículo (odómetro final - inicial).
     * @param gasCostTotal Total invertido en Gas Natural Vehicular (COP).
     * @param gasolineCostTotal Total invertido en Gasolina Corriente (COP).
     * @return [BiFuelCostMetrics] con los costos unitarios y mixtos por kilómetro.
     */
    operator fun invoke(
        totalKilometers: Int,
        gasCostTotal: Double,
        gasolineCostTotal: Double
    ): BiFuelCostMetrics {
        require(totalKilometers >= 0) { "El total de kilómetros no puede ser negativo" }
        require(gasCostTotal >= 0.0) { "El costo de gas no puede ser negativo" }
        require(gasolineCostTotal >= 0.0) { "El costo de gasolina no puede ser negativo" }

        val totalFuelCost = gasCostTotal + gasolineCostTotal

        // Protección contra división por cero cuando el odómetro no ha avanzado
        if (totalKilometers == 0) {
            return BiFuelCostMetrics(
                totalKilometers = 0,
                gasCostTotal = gasCostTotal,
                gasolineCostTotal = gasolineCostTotal,
                totalFuelCost = totalFuelCost,
                gasCostPerKm = 0.0,
                gasolineCostPerKm = 0.0,
                blendedCostPerKm = 0.0
            )
        }

        val kmDouble = totalKilometers.toDouble()
        val gasCostPerKm = gasCostTotal / kmDouble
        val gasolineCostPerKm = gasolineCostTotal / kmDouble
        val blendedCostPerKm = totalFuelCost / kmDouble

        return BiFuelCostMetrics(
            totalKilometers = totalKilometers,
            gasCostTotal = gasCostTotal,
            gasolineCostTotal = gasolineCostTotal,
            totalFuelCost = totalFuelCost,
            gasCostPerKm = gasCostPerKm,
            gasolineCostPerKm = gasolineCostPerKm,
            blendedCostPerKm = blendedCostPerKm
        )
    }

    /**
     * Sobrecarga de conveniencia que extrae métricas directamente del resumen de turno.
     *
     * @param summary Resumen financiero del turno.
     * @return [BiFuelCostMetrics] calculado para el turno.
     */
    operator fun invoke(summary: ShiftFinancialSummary): BiFuelCostMetrics {
        return invoke(
            totalKilometers = summary.totalKilometers,
            gasCostTotal = summary.totalGasExpense,
            gasolineCostTotal = summary.totalGasolineExpense
        )
    }
}
