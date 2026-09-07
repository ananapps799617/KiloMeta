// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/GetBiFuelSavingsUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso de Dominio que audita el ahorro generado por el sistema bi-combustible (GNV).
// Compara el costo real de combustible (Gas Natural + soporte Gasolina) contra
// el gasto hipotético que habría costado operar exclusivamente con gasolina corriente
// (35 km/galón a $15.800 COP/galón).
// Regla: Cero dependencias del framework Android en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.BiFuelSavingsMetrics
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE AUDITORÍA Y AHORRO BI-COMBUSTIBLE
// ------------------------------------------------------------------------------
/**
 * Calcula el ahorro neto en dinero y porcentaje al utilizar Gas Natural Vehicular (GNV).
 *
 * @property kmPerGallonGasoline Rendimiento de referencia de gasolina (predeterminado 35 km/galón).
 * @property pricePerGallonGasoline Precio de referencia de gasolina corriente en COP (predeterminado $15.800 COP).
 */
class GetBiFuelSavingsUseCase @Inject constructor() {

    private val defaultKmPerGallon: Double = Constants.GASOLINE_REFERENCE_KM_PER_GALLON
    private val defaultPricePerGallon: Double = Constants.GASOLINE_REFERENCE_PRICE_COP

    /**
     * Calcula las métricas de ahorro bi-combustible a partir de los kilómetros y gastos totales.
     *
     * @param totalKmDriven Kilómetros totales recorridos.
     * @param totalGasExpense Inversión total en GNV (COP).
     * @param totalGasolineExpense Inversión total en gasolina (COP).
     * @param kmPerGallonGasoline Rendimiento parametrizable de referencia (opcional).
     * @param pricePerGallonGasoline Precio por galón parametrizable de referencia (opcional).
     * @return [BiFuelSavingsMetrics] inmutable con métricas calculadas y protegidas contra división por cero.
     */
    operator fun invoke(
        totalKmDriven: Int,
        totalGasExpense: Double,
        totalGasolineExpense: Double,
        kmPerGallonGasoline: Double = defaultKmPerGallon,
        pricePerGallonGasoline: Double = defaultPricePerGallon
    ): BiFuelSavingsMetrics {
        val actualFuelExpense = totalGasExpense + totalGasolineExpense

        if (totalKmDriven <= 0) {
            return BiFuelSavingsMetrics(
                totalKmDriven = 0,
                totalGasExpense = totalGasExpense,
                totalGasolineExpense = totalGasolineExpense,
                actualFuelExpense = actualFuelExpense,
                equivalentGasolineExpense = 0.0,
                netSavingsAmount = 0.0,
                savingsPercentage = 0.0,
                costPerKmGas = 0.0,
                costPerKmGasoline = 0.0
            )
        }

        val equivalentGallons = if (kmPerGallonGasoline > 0.0) {
            totalKmDriven.toDouble() / kmPerGallonGasoline
        } else {
            0.0
        }

        val equivalentGasolineExpense = equivalentGallons * pricePerGallonGasoline
        val netSavings = equivalentGasolineExpense - actualFuelExpense

        val savingsPercentage = if (equivalentGasolineExpense > 0.0) {
            (netSavings / equivalentGasolineExpense) * 100.0
        } else {
            0.0
        }

        val costPerKmGas = totalGasExpense / totalKmDriven.toDouble()
        val costPerKmGasoline = totalGasolineExpense / totalKmDriven.toDouble()

        return BiFuelSavingsMetrics(
            totalKmDriven = totalKmDriven,
            totalGasExpense = totalGasExpense,
            totalGasolineExpense = totalGasolineExpense,
            actualFuelExpense = actualFuelExpense,
            equivalentGasolineExpense = equivalentGasolineExpense,
            netSavingsAmount = netSavings,
            savingsPercentage = savingsPercentage,
            costPerKmGas = costPerKmGas,
            costPerKmGasoline = costPerKmGasoline
        )
    }

    /**
     * Sobrecarga de conveniencia que extrae métricas de una colección de resúmenes de turnos.
     *
     * @param shifts Lista de resúmenes financieros de jornadas completadas.
     * @return [BiFuelSavingsMetrics] acumuladas.
     */
    operator fun invoke(shifts: List<ShiftFinancialSummary>): BiFuelSavingsMetrics {
        val totalKm = shifts.sumOf { it.totalKilometers }
        val totalGas = shifts.sumOf { it.totalGasExpense }
        val totalGasoline = shifts.sumOf { it.totalGasolineExpense }
        return invoke(totalKm, totalGas, totalGasoline)
    }
}
