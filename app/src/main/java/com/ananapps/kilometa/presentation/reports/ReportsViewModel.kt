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
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import com.ananapps.kilometa.domain.repository.MaintenanceRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.usecase.CalculateMonthlyCycleBoundsUseCase
import com.ananapps.kilometa.domain.usecase.CalculateWeeklyCycleBoundsUseCase
import com.ananapps.kilometa.domain.usecase.GenerateCsvReportUseCase
import com.ananapps.kilometa.domain.usecase.GenerateShareableSummaryUseCase
import com.ananapps.kilometa.domain.usecase.GetBiFuelSavingsUseCase
import com.ananapps.kilometa.domain.usecase.GetShiftHistoryUseCase
import com.ananapps.kilometa.presentation.history.DayHistoryMapper
import com.ananapps.kilometa.presentation.history.ExportUiState
import com.ananapps.kilometa.presentation.reports.components.ExpenseDonutSlice
import com.ananapps.kilometa.presentation.reports.components.KpiReportData
import com.ananapps.kilometa.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val expenseRepository: ExpenseRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val getBiFuelSavingsUseCase: GetBiFuelSavingsUseCase,
    private val generateCsvReportUseCase: GenerateCsvReportUseCase,
    private val generateShareableSummaryUseCase: GenerateShareableSummaryUseCase,
    private val getShiftHistoryUseCase: GetShiftHistoryUseCase,
    private val driverProfileRepository: DriverProfileRepository,
    private val calculateWeeklyCycleBoundsUseCase: CalculateWeeklyCycleBoundsUseCase,
    private val calculateMonthlyCycleBoundsUseCase: CalculateMonthlyCycleBoundsUseCase
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.WEEKLY)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val _weekOffset = MutableStateFlow(0)
    private val _monthOffset = MutableStateFlow(0)

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    private val periodAndVehicleFlow = combine(
        _selectedPeriod,
        _weekOffset,
        _monthOffset,
        driverProfileRepository.getVehicleProfile()
    ) { period, weekOff, monthOff, vehicle ->
        PeriodContext(period, weekOff, monthOff, vehicle)
    }

    val uiState: StateFlow<ReportsUiState> = combine(
        periodAndVehicleFlow,
        shiftRepository.getAllTrips(),
        shiftRepository.getAllFuelings(),
        expenseRepository.getAllExpenses(),
        maintenanceRepository.getAllMaintenances()
    ) { pCtx, trips, fuelings, expenses, maintenances ->
        val bounds = computeBounds(pCtx)
        val fTrips = trips.filter { it.timestamp in bounds.startMillis..bounds.endMillis }
        val fFuelings = fuelings.filter { it.timestamp in bounds.startMillis..bounds.endMillis }
        val fExpenses = expenses.filter { it.timestamp in bounds.startMillis..bounds.endMillis }
        val fMaintenances = maintenances.filter { it.timestamp in bounds.startMillis..bounds.endMillis }

        if (fTrips.isEmpty() && fFuelings.isEmpty() && fExpenses.isEmpty() && fMaintenances.isEmpty()) {
            ReportsUiState.Empty(
                selectedPeriod = pCtx.period,
                cycleTitle = bounds.title,
                cycleSubtitle = bounds.subtitle,
                canGoNext = bounds.canGoNext
            )
        } else {
            val grossIncome = fTrips.sumOf { it.grossAmount }
            val netTripIncome = fTrips.sumOf { it.netAmount }
            val fuelCost = fFuelings.sumOf { it.amountPaid }
            val expenseCost = fExpenses.sumOf { it.amount }
            val maintCost = fMaintenances.sumOf { it.costCop }
            val totalCosts = fuelCost + expenseCost + maintCost
            val netBalance = netTripIncome - totalCosts

            val days = DayHistoryMapper.mapToDays(fTrips, fFuelings, fExpenses, fMaintenances)
            val totalDistanceKm = days.sumOf { it.distanceKm }
            val activeDays = maxOf(1, days.size)

            val kpiData = KpiReportData(
                netBalance = netBalance,
                totalDistanceKm = totalDistanceKm,
                totalCosts = totalCosts,
                grossIncome = grossIncome,
                activeDaysCount = activeDays
            )

            val rentCost = fExpenses.filter { it.concept == ExpenseConcept.RENT }.sumOf { it.amount }
            val slices = listOf(
                ExpenseDonutSlice("Alquiler", rentCost, PlatformPrivate),
                ExpenseDonutSlice("Combustible", fuelCost, FuelGasoline),
                ExpenseDonutSlice("Mantenimiento", maintCost, PlatformUber.copy(alpha = 0.8f)),
                ExpenseDonutSlice("Operación", expenseCost - rentCost, StatusRed)
            )

            val totalGross = fTrips.sumOf { it.grossAmount }
            val platformBreakdowns = PlatformType.entries.map { platform ->
                val pTrips = fTrips.filter { it.platformType == platform }
                val pGross = pTrips.sumOf { it.grossAmount }
                val pComm = pTrips.sumOf { it.commissionAmount }
                val pNet = pTrips.sumOf { it.netAmount }
                val pCount = pTrips.size
                PlatformAnalytics(
                    platformType = platform,
                    grossIncome = pGross,
                    commissionPaid = pComm,
                    netIncome = pNet,
                    tripsCount = pCount,
                    averageTicket = if (pCount > 0) pGross / pCount else 0.0,
                    netMarginPercentage = if (pGross > 0.0) (pNet / pGross) * 100.0 else 0.0,
                    revenueSharePercentage = if (totalGross > 0.0) (pGross / totalGross) * 100.0 else 0.0
                )
            }

            val gasExpense = fFuelings.filter { it.fuelType == FuelType.GAS }.sumOf { it.amountPaid }
            val gasolineExpense = fFuelings.filter { it.fuelType != FuelType.GAS }.sumOf { it.amountPaid }
            val biFuelSavings = getBiFuelSavingsUseCase(totalDistanceKm, gasExpense, gasolineExpense)

            ReportsUiState.Success(
                selectedPeriod = pCtx.period,
                kpiData = kpiData,
                expenseSlices = slices,
                platformBreakdowns = platformBreakdowns,
                biFuelSavings = biFuelSavings,
                cycleTitle = bounds.title,
                cycleSubtitle = bounds.subtitle,
                canGoNext = bounds.canGoNext,
                periodStartMillis = bounds.startMillis,
                periodEndMillis = bounds.endMillis
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ReportsUiState.Loading)

    fun onPeriodSelected(period: ReportPeriod) {
        _selectedPeriod.value = period
        _weekOffset.value = 0
        _monthOffset.value = 0
    }

    fun onPreviousPeriodClicked() {
        when (_selectedPeriod.value) {
            ReportPeriod.WEEKLY -> _weekOffset.value++
            ReportPeriod.MONTHLY -> _monthOffset.value++
            ReportPeriod.ALL -> Unit
        }
    }

    fun onNextPeriodClicked() {
        when (_selectedPeriod.value) {
            ReportPeriod.WEEKLY -> if (_weekOffset.value > 0) _weekOffset.value--
            ReportPeriod.MONTHLY -> if (_monthOffset.value > 0) _monthOffset.value--
            ReportPeriod.ALL -> Unit
        }
    }

    fun onExportCsvClicked() = viewModelScope.launch {
        _exportState.value = ExportUiState.Exporting
        try {
            val shifts = getShiftHistoryUseCase().first()
            val state = uiState.value
            val filteredShifts = if (state is ReportsUiState.Success) {
                shifts.filter { it.startTime in state.periodStartMillis..state.periodEndMillis }
            } else shifts
            val report = generateCsvReportUseCase(HistoryFilterPeriod.ALL, filteredShifts)
            _exportState.value = ExportUiState.CsvReady(report)
        } catch (e: Exception) {
            _exportState.value = ExportUiState.Error("Error al generar CSV: ${e.message}")
        }
    }

    fun onShareSummaryClicked() = viewModelScope.launch {
        _exportState.value = ExportUiState.Exporting
        try {
            val shifts = getShiftHistoryUseCase().first()
            val state = uiState.value
            val filteredShifts = if (state is ReportsUiState.Success) {
                shifts.filter { it.startTime in state.periodStartMillis..state.periodEndMillis }
            } else shifts
            val summary = generateShareableSummaryUseCase(HistoryFilterPeriod.ALL, filteredShifts)
            _exportState.value = ExportUiState.ShareReady(summary)
        } catch (e: Exception) {
            _exportState.value = ExportUiState.Error("Error al generar resumen: ${e.message}")
        }
    }

    fun resetExportState() { _exportState.value = ExportUiState.Idle }

    private fun computeBounds(ctx: PeriodContext): PeriodBounds {
        val zoneId = ZoneId.of("America/Bogota")
        val esCo = Locale("es", "CO")
        return when (ctx.period) {
            ReportPeriod.WEEKLY -> {
                val cutoffDay = java.time.DayOfWeek.of(ctx.vehicle.quotaCutoffDay.coerceIn(1, 7))
                val (start, end) = calculateWeeklyCycleBoundsUseCase(
                    cutoffDay = cutoffDay,
                    cutoffHour = ctx.vehicle.quotaCutoffHour,
                    weekOffset = ctx.weekOffset
                )
                val sLd = Instant.ofEpochMilli(start).atZone(zoneId).toLocalDate()
                val eLd = Instant.ofEpochMilli(end).atZone(zoneId).toLocalDate()
                val fmt = DateTimeFormatter.ofPattern("d MMM", esCo)
                val title = if (ctx.weekOffset == 0) "Esta Semana" else "Semana: ${sLd.format(fmt)} – ${eLd.format(fmt)} (Cerrada)"
                val sub = "${sLd.dayOfWeek.getDisplayName(TextStyle.SHORT, esCo)} 00:00 ➔ ${eLd.dayOfWeek.getDisplayName(TextStyle.SHORT, esCo)} 23:59"
                PeriodBounds(start, end, title, sub, ctx.weekOffset > 0)
            }
            ReportPeriod.MONTHLY -> {
                val (start, end) = calculateMonthlyCycleBoundsUseCase(monthOffset = ctx.monthOffset)
                val mLd = Instant.ofEpochMilli(start).atZone(zoneId).toLocalDate()
                val monthName = mLd.month.getDisplayName(TextStyle.FULL, esCo).replaceFirstChar { it.uppercase() }
                val title = if (ctx.monthOffset == 0) "Este Mes ($monthName ${mLd.year})" else "$monthName ${mLd.year} (Cerrado)"
                PeriodBounds(start, end, title, "Día 1 ➔ Fin de mes", ctx.monthOffset > 0)
            }
            ReportPeriod.ALL -> PeriodBounds(0L, Long.MAX_VALUE, "Historial Completo", "Todos los registros", false)
        }
    }

    private data class PeriodContext(
        val period: ReportPeriod,
        val weekOffset: Int,
        val monthOffset: Int,
        val vehicle: VehicleProfile
    )

    private data class PeriodBounds(
        val startMillis: Long,
        val endMillis: Long,
        val title: String,
        val subtitle: String,
        val canGoNext: Boolean
    )
}
