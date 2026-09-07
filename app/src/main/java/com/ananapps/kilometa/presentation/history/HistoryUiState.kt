// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/HistoryUiState.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Estado de UI sellado y tipado de forma exhaustiva para la pantalla de Historial y Analíticas.
// Representa los cuatro estados canónicos: Loading, Success, Empty y Error.
// ==============================================================================

package com.ananapps.kilometa.presentation.history

import com.ananapps.kilometa.domain.model.BiFuelSavingsMetrics
import com.ananapps.kilometa.domain.model.PlatformAnalytics
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary

// ------------------------------------------------------------------------------
// BLOQUE 1: PERÍODOS DE FILTRADO TEMPORAL
// ------------------------------------------------------------------------------
/**
 * Opciones de corte temporal para la agregación de métricas analíticas.
 */
enum class HistoryFilterPeriod(val displayName: String) {
    WEEK("Esta Semana"),
    MONTH("Este Mes"),
    ALL("Todo")
}

// ------------------------------------------------------------------------------
// BLOQUE 2: ESTADO SELLADO DE LA INTERFAZ DE HISTORIAL
// ------------------------------------------------------------------------------
/**
 * Jerarquía sellada inmutable para el renderizado reactivo del módulo de Historial.
 */
sealed interface HistoryUiState {

    /**
     * Estado inicial o de transición mientras se consultan los flujos de base de datos.
     */
    data object Loading : HistoryUiState

    /**
     * Estado de éxito cuando existen registros y las analíticas han sido calculadas.
     *
     * @property totalNetProfit Ganancia neta consolidada en COP del período seleccionado.
     * @property totalKilometers Kilómetros acumulados en turnos completados.
     * @property totalTrips Número global de carreras efectuadas.
     * @property biFuelSavings Métricas de auditoría económica de GNV vs Gasolina.
     * @property platformBreakdowns Desglose por plataforma con comisiones e ingresos netos.
     * @property shifts Listado cronológico descendente de turnos finalizados.
     * @property selectedFilter Filtro temporal activo aplicado a los datos.
     */
    data class Success(
        val totalNetProfit: Double,
        val totalKilometers: Int,
        val totalTrips: Int,
        val biFuelSavings: BiFuelSavingsMetrics,
        val platformBreakdowns: List<PlatformAnalytics>,
        val shifts: List<ShiftFinancialSummary>,
        val selectedFilter: HistoryFilterPeriod
    ) : HistoryUiState

    /**
     * Estado vacío cuando no hay turnos registrados en la base de datos o en el filtro seleccionado.
     *
     * @property selectedFilter Filtro actualmente activo para permitir alternancia de períodos.
     */
    data class Empty(
        val selectedFilter: HistoryFilterPeriod = HistoryFilterPeriod.ALL
    ) : HistoryUiState

    /**
     * Estado de error en caso de fallo inesperado de lectura o cálculo.
     *
     * @property message Mensaje descriptivo para depuración.
     */
    data class Error(
        val message: String
    ) : HistoryUiState
}
