// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/reports/ReportsUiState.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Estado inmutable para el Dashboard de Informes y Analíticas de alto nivel.
// ==============================================================================

package com.ananapps.kilometa.presentation.reports

import com.ananapps.kilometa.domain.model.BiFuelSavingsMetrics
import com.ananapps.kilometa.domain.model.PlatformAnalytics
import com.ananapps.kilometa.presentation.history.ExportUiState
import com.ananapps.kilometa.presentation.reports.components.ExpenseDonutSlice
import com.ananapps.kilometa.presentation.reports.components.KpiReportData

enum class ReportPeriod(val displayName: String) {
    THIS_WEEK("Esta Semana"),
    THIS_MONTH("Este Mes"),
    ALL("Todo")
}

sealed interface ReportsUiState {
    data object Loading : ReportsUiState

    data class Success(
        val selectedPeriod: ReportPeriod,
        val kpiData: KpiReportData,
        val expenseSlices: List<ExpenseDonutSlice>,
        val platformBreakdowns: List<PlatformAnalytics>,
        val biFuelSavings: BiFuelSavingsMetrics
    ) : ReportsUiState

    data class Empty(val selectedPeriod: ReportPeriod) : ReportsUiState
    data class Error(val message: String) : ReportsUiState
}
