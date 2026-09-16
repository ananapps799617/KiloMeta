// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/reports/ReportsUiState.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Estado inmutable para el Dashboard de Informes y Analíticas de alto nivel (Orden #042).
// Incorpora metadatos del ciclo acotado (título, subtítulo y estado de navegación stepper).
// ==============================================================================

package com.ananapps.kilometa.presentation.reports

import com.ananapps.kilometa.domain.model.BiFuelSavingsMetrics
import com.ananapps.kilometa.domain.model.PlatformAnalytics
import com.ananapps.kilometa.presentation.reports.components.ExpenseDonutSlice
import com.ananapps.kilometa.presentation.reports.components.KpiReportData

enum class ReportPeriod(val displayName: String) {
    WEEKLY("Semanal"),
    MONTHLY("Mensual"),
    ALL("Todo");

    companion object {
        val THIS_WEEK = WEEKLY
        val THIS_MONTH = MONTHLY
    }
}

sealed interface ReportsUiState {
    data object Loading : ReportsUiState

    data class Success(
        val selectedPeriod: ReportPeriod,
        val kpiData: KpiReportData,
        val expenseSlices: List<ExpenseDonutSlice>,
        val platformBreakdowns: List<PlatformAnalytics>,
        val biFuelSavings: BiFuelSavingsMetrics,
        val cycleTitle: String = "",
        val cycleSubtitle: String = "",
        val canGoNext: Boolean = false,
        val periodStartMillis: Long = 0L,
        val periodEndMillis: Long = Long.MAX_VALUE
    ) : ReportsUiState

    data class Empty(
        val selectedPeriod: ReportPeriod,
        val cycleTitle: String = "",
        val cycleSubtitle: String = "",
        val canGoNext: Boolean = false
    ) : ReportsUiState

    data class Error(val message: String) : ReportsUiState
}
