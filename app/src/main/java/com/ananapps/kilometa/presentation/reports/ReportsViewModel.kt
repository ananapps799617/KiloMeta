// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/reports/ReportsViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel del Dashboard de Informes y Analíticas. Procesa métricas 2x2,
// desglose de costos en dona y auditoría bi-combustible para períodos seleccionados.
// ==============================================================================

package com.ananapps.kilometa.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.*
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import com.ananapps.kilometa.domain.repository.MaintenanceRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.usecase.GenerateCsvReportUseCase
import com.ananapps.kilometa.domain.usecase.GenerateShareableSummaryUseCase
import com.ananapps.kilometa.domain.usecase.GetBiFuelSavingsUseCase
import com.ananapps.kilometa.domain.usecase.GetShiftHistoryUseCase
import com.ananapps.kilometa.presentation.history.ExportUiState
import com.ananapps.kilometa.presentation.reports.components.ExpenseDonutSlice
import com.ananapps.kilometa.presentation.reports.components.KpiReportData
import com.ananapps.kilometa.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val expenseRepository: ExpenseRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val getBiFuelSavingsUseCase: GetBiFuelSavingsUseCase,
    private val generateCsvReportUseCase: GenerateCsvReportUseCase,
    private val generateShareableSummaryUseCase: GenerateShareableSummaryUseCase,
    private val getShiftHistoryUseCase: GetShiftHistoryUseCase
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.THIS_WEEK)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    val uiState: StateFlow<ReportsUiState> = combine(
        _selectedPeriod,
        shiftRepository.getAllTrips(),
        shiftRepository.getAllFuelings(),
        expenseRepository.getAllExpenses(),
        maintenanceRepository.getAllMaintenances()
    ) { period, trips, fuelings, expenses, maintenances ->
        val cutoffMillis = computeCutoff(period)

        val fTrips = trips.filter { it.timestamp >= cutoffMillis }
        val fFuelings = fuelings.filter { it.timestamp >= cutoffMillis }
        val fExpenses = expenses.filter { it.timestamp >= cutoffMillis }
        val fMaintenances = maintenances.filter { it.timestamp >= cutoffMillis }

        if (fTrips.isEmpty() && fFuelings.isEmpty() && fExpenses.isEmpty() && fMaintenances.isEmpty()) {
            ReportsUiState.Empty(period)
        } else {
            // 1. Matriz 2x2
            val grossIncome = fTrips.sumOf { it.grossAmount }
            val netTripIncome = fTrips.sumOf { it.netAmount }
            val fuelCost = fFuelings.sumOf { it.amountPaid }
            val expenseCost = fExpenses.sumOf { it.amount }
            val maintCost = fMaintenances.sumOf { it.costCop }
            val totalCosts = fuelCost + expenseCost + maintCost
            val netBalance = netTripIncome - totalCosts

            // Distancia en KM
            val tripKm = fTrips.mapNotNull { it.tripDistanceKm }.sum().toInt()
            val minOdo = (fTrips.mapNotNull { it.startOdometer } + fFuelings.map { it.odometer } + fMaintenances.map { it.odometer }).minOrNull()
            val maxOdo = (fTrips.mapNotNull { it.endOdometer ?: it.startOdometer } + fFuelings.map { it.odometer } + fMaintenances.map { it.odometer }).maxOrNull()
            val odoKm = if (minOdo != null && maxOdo != null && maxOdo > minOdo) maxOdo - minOdo else 0
            val totalDistanceKm = maxOf(tripKm, odoKm)

            // Días activos únicos
            val calendar = Calendar.getInstance()
            val activeDays = (fTrips.map { it.timestamp } + fFuelings.map { it.timestamp } + fExpenses.map { it.timestamp } + fMaintenances.map { it.timestamp })
                .map {
                    calendar.timeInMillis = it
                    "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
                }.distinct().size

            val kpiData = KpiReportData(
                netBalance = netBalance,
                totalDistanceKm = totalDistanceKm,
                totalCosts = totalCosts,
                grossIncome = grossIncome,
                activeDaysCount = maxOf(1, activeDays)
            )

            // 2. Gráfico de Dona de Costos
            val rentCost = fExpenses.filter { it.concept == ExpenseConcept.RENT }.sumOf { it.amount }
            val otherExpenseCost = expenseCost - rentCost
            val slices = listOf(
                ExpenseDonutSlice("Alquiler", rentCost, PlatformPrivate),
                ExpenseDonutSlice("Combustible", fuelCost, FuelGasoline),
                ExpenseDonutSlice("Mantenimiento", maintCost, PlatformUber.copy(alpha = 0.8f)),
                ExpenseDonutSlice("Operación", otherExpenseCost, StatusRed)
            )

            // 3. Desglose por Plataforma
            val totalGrossIncome = fTrips.sumOf { it.grossAmount }
            val platformBreakdowns = PlatformType.entries.map { platform ->
                val pTrips = fTrips.filter { it.platformType == platform }
                val pGross = pTrips.sumOf { it.grossAmount }
                val pComm = pTrips.sumOf { it.commissionAmount }
                val pNet = pTrips.sumOf { it.netAmount }
                val pCount = pTrips.size
                val pAvgTicket = if (pCount > 0) pGross / pCount else 0.0
                val pNetMargin = if (pGross > 0.0) (pNet / pGross) * 100.0 else 0.0
                val pRevShare = if (totalGrossIncome > 0.0) (pGross / totalGrossIncome) * 100.0 else 0.0

                PlatformAnalytics(
                    platformType = platform,
                    grossIncome = pGross,
                    commissionPaid = pComm,
                    netIncome = pNet,
                    tripsCount = pCount,
                    averageTicket = pAvgTicket,
                    netMarginPercentage = pNetMargin,
                    revenueSharePercentage = pRevShare
                )
            }

            // 4. Auditoría Bi-Combustible
            val gasExpense = fFuelings.filter { it.fuelType == FuelType.GAS }.sumOf { it.amountPaid }
            val gasolineExpense = fFuelings.filter { it.fuelType != FuelType.GAS }.sumOf { it.amountPaid }
            val biFuelSavings = getBiFuelSavingsUseCase(totalDistanceKm, gasExpense, gasolineExpense)

            ReportsUiState.Success(
                selectedPeriod = period,
                kpiData = kpiData,
                expenseSlices = slices,
                platformBreakdowns = platformBreakdowns,
                biFuelSavings = biFuelSavings
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ReportsUiState.Loading)

    fun onPeriodSelected(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    fun onExportCsvClicked() = viewModelScope.launch {
        _exportState.value = ExportUiState.Exporting
        try {
            val shifts = getShiftHistoryUseCase().first()
            val report = generateCsvReportUseCase(HistoryFilterPeriod.ALL, shifts)
            _exportState.value = ExportUiState.CsvReady(report)
        } catch (e: Exception) {
            _exportState.value = ExportUiState.Error("Error al generar CSV: ${e.message}")
        }
    }

    fun onShareSummaryClicked() = viewModelScope.launch {
        _exportState.value = ExportUiState.Exporting
        try {
            val shifts = getShiftHistoryUseCase().first()
            val summary = generateShareableSummaryUseCase(HistoryFilterPeriod.ALL, shifts)
            _exportState.value = ExportUiState.ShareReady(summary)
        } catch (e: Exception) {
            _exportState.value = ExportUiState.Error("Error al generar resumen: ${e.message}")
        }
    }

    fun resetExportState() {
        _exportState.value = ExportUiState.Idle
    }

    private fun computeCutoff(period: ReportPeriod): Long {
        val cal = Calendar.getInstance()
        return when (period) {
            ReportPeriod.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            ReportPeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            ReportPeriod.ALL -> 0L
        }
    }
}
