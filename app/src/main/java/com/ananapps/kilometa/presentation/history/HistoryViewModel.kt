// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/HistoryViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel para la pantalla de Historial y Analíticas (@HiltViewModel).
// Orquesta analíticas reactivas, exportación de reportes contables CSV (RFC 4180/BOM)
// y generación de resúmenes enriquecidos con emojis para WhatsApp/Telegram.
// ==============================================================================

package com.ananapps.kilometa.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.BiFuelSavingsMetrics
import com.ananapps.kilometa.domain.model.HistoryFilterPeriod
import com.ananapps.kilometa.domain.model.PlatformAnalytics
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import com.ananapps.kilometa.domain.repository.FuelingRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.repository.TripRepository
import com.ananapps.kilometa.domain.usecase.CalculateWeeklyCycleBoundsUseCase
import com.ananapps.kilometa.domain.usecase.ConsolidateDayShiftsUseCase
import com.ananapps.kilometa.domain.usecase.DeleteExpenseUseCase
import com.ananapps.kilometa.domain.usecase.DeleteFuelingUseCase
import com.ananapps.kilometa.domain.usecase.DeleteShiftUseCase
import com.ananapps.kilometa.domain.usecase.DeleteTripUseCase
import com.ananapps.kilometa.domain.usecase.GenerateCsvReportUseCase
import com.ananapps.kilometa.domain.usecase.GenerateShareableSummaryUseCase
import com.ananapps.kilometa.domain.usecase.GetBiFuelSavingsUseCase
import com.ananapps.kilometa.domain.usecase.GetPlatformAnalyticsUseCase
import com.ananapps.kilometa.domain.usecase.GetShiftHistoryUseCase
import com.ananapps.kilometa.domain.usecase.UpdateShiftUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: VIEWMODEL DE HISTORIAL Y EXPORTACIÓN CONTABLE
// ------------------------------------------------------------------------------
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getShiftHistoryUseCase: GetShiftHistoryUseCase,
    private val getPlatformAnalyticsUseCase: GetPlatformAnalyticsUseCase,
    private val getBiFuelSavingsUseCase: GetBiFuelSavingsUseCase,
    private val generateCsvReportUseCase: GenerateCsvReportUseCase,
    private val generateShareableSummaryUseCase: GenerateShareableSummaryUseCase,
    private val deleteShiftUseCase: DeleteShiftUseCase,
    private val consolidateDayShiftsUseCase: ConsolidateDayShiftsUseCase,
    private val updateShiftUseCase: UpdateShiftUseCase,
    private val shiftRepository: ShiftRepository,
    private val driverProfileRepository: DriverProfileRepository,
    private val calculateWeeklyCycleBoundsUseCase: CalculateWeeklyCycleBoundsUseCase,
    private val tripRepository: TripRepository,
    private val fuelingRepository: FuelingRepository,
    private val expenseRepository: ExpenseRepository,
    private val deleteTripUseCase: DeleteTripUseCase,
    private val deleteFuelingUseCase: DeleteFuelingUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(HistoryFilterPeriod.ALL)

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    val uiState: StateFlow<HistoryUiState> = combine(
        _selectedFilter,
        getShiftHistoryUseCase(),
        shiftRepository.getAllTrips(),
        driverProfileRepository.getVehicleProfile()
    ) { filter, completedShifts, allTrips, vehicle ->
        processHistoricalData(filter, completedShifts, allTrips, vehicle)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = HistoryUiState.Loading
    )

    fun onFilterSelected(filter: HistoryFilterPeriod) { _selectedFilter.value = filter }

    fun onExportCsvClicked(period: HistoryFilterPeriod) {
        viewModelScope.launch {
            _exportState.value = ExportUiState.Exporting
            try {
                val allShifts = getShiftHistoryUseCase().first()
                val vehicle = driverProfileRepository.getVehicleProfile().first()
                val filteredShifts = filterShifts(period, allShifts, vehicle)
                _exportState.value = ExportUiState.CsvReady(generateCsvReportUseCase(period, filteredShifts))
            } catch (e: Exception) {
                _exportState.value = ExportUiState.Error(e.localizedMessage ?: "Error al compilar CSV")
            }
        }
    }

    fun onShareSummaryClicked(period: HistoryFilterPeriod) {
        viewModelScope.launch {
            _exportState.value = ExportUiState.Exporting
            try {
                val allShifts = getShiftHistoryUseCase().first()
                val vehicle = driverProfileRepository.getVehicleProfile().first()
                val filteredShifts = filterShifts(period, allShifts, vehicle)
                _exportState.value = ExportUiState.ShareReady(generateShareableSummaryUseCase(period, filteredShifts))
            } catch (e: Exception) {
                _exportState.value = ExportUiState.Error(e.localizedMessage ?: "Error al generar balance")
            }
        }
    }

    fun onShareShiftSummaryClicked(shift: ShiftFinancialSummary) {
        viewModelScope.launch {
            try {
                _exportState.value = ExportUiState.ShareReady(generateShareableSummaryUseCase(shift))
            } catch (e: Exception) {
                _exportState.value = ExportUiState.Error(e.localizedMessage ?: "Error al generar liquidación")
            }
        }
    }

    fun resetExportState() { _exportState.value = ExportUiState.Idle }

    fun onDeleteShift(shiftId: String) { viewModelScope.launch { deleteShiftUseCase(shiftId) } }

    fun onConsolidateShiftsForDay(shifts: List<ShiftFinancialSummary>) {
        viewModelScope.launch {
            val allShifts = shiftRepository.getAllShifts().first()
            val targetIds = shifts.map { it.shiftId }.toSet()
            val toConsolidate = allShifts.filter { it.id in targetIds }
            if (toConsolidate.size >= 2) consolidateDayShiftsUseCase(toConsolidate)
        }
    }

    fun onUpdateShift(shiftId: String, startOdo: Int, endOdo: Int?, notes: String?) {
        viewModelScope.launch {
            val allShifts = shiftRepository.getAllShifts().first()
            val existing = allShifts.find { it.id == shiftId } ?: return@launch
            updateShiftUseCase(existing.copy(startOdometer = startOdo, endOdometer = endOdo, notes = notes))
        }
    }

    // Métodos para inspección y edición de entidades hijas del turno
    fun getShiftTrips(shiftId: String) = tripRepository.getTripsByShift(shiftId)
    fun getShiftFuelings(shiftId: String) = fuelingRepository.getFuelingsByShift(shiftId)
    fun getShiftExpenses(shiftId: String) = expenseRepository.getExpensesByShift(shiftId)
    fun onDeleteTrip(tripId: String) = viewModelScope.launch { deleteTripUseCase(tripId) }
    fun onDeleteFueling(fuelingId: String) = viewModelScope.launch { deleteFuelingUseCase(fuelingId) }
    fun onDeleteExpense(expenseId: String) = viewModelScope.launch { deleteExpenseUseCase(expenseId) }

    private fun filterShifts(period: HistoryFilterPeriod, shifts: List<ShiftFinancialSummary>, vehicle: VehicleProfile): List<ShiftFinancialSummary> {
        val cutoffMillis = calculatePeriodCutoff(period, vehicle)
        return if (cutoffMillis > 0L) shifts.filter { it.startTime >= cutoffMillis } else shifts
    }

    private fun processHistoricalData(
        filter: HistoryFilterPeriod,
        shifts: List<ShiftFinancialSummary>,
        trips: List<Trip>,
        vehicle: VehicleProfile
    ): HistoryUiState {
        if (shifts.isEmpty()) return HistoryUiState.Empty(selectedFilter = filter)

        val cutoffMillis = calculatePeriodCutoff(filter, vehicle)
        val filteredShifts = if (cutoffMillis > 0L) shifts.filter { it.startTime >= cutoffMillis } else shifts
        if (filteredShifts.isEmpty()) return HistoryUiState.Empty(selectedFilter = filter)

        val filteredTrips = if (cutoffMillis > 0L) trips.filter { it.timestamp >= cutoffMillis } else trips
        val totalNetProfit = filteredShifts.sumOf { it.netProfit }
        val totalKilometers = filteredShifts.sumOf { it.totalKilometers }
        val totalTrips = if (filteredTrips.isNotEmpty()) filteredTrips.size else filteredShifts.sumOf { it.tripsCount }

        val savingsMetrics = getBiFuelSavingsUseCase(
            totalKmDriven = totalKilometers,
            totalGasExpense = filteredShifts.sumOf { it.totalGasExpense },
            totalGasolineExpense = filteredShifts.sumOf { it.totalGasolineExpense }
        )

        return HistoryUiState.Success(
            totalNetProfit = totalNetProfit,
            totalKilometers = totalKilometers,
            totalTrips = totalTrips,
            biFuelSavings = savingsMetrics,
            platformBreakdowns = getPlatformAnalyticsUseCase(filteredTrips),
            shifts = filteredShifts,
            selectedFilter = filter
        )
    }

    private fun calculatePeriodCutoff(period: HistoryFilterPeriod, vehicle: VehicleProfile, nowMillis: Long = System.currentTimeMillis()): Long {
        val zone = ZoneId.of("America/Bogota")
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        return when (period) {
            HistoryFilterPeriod.WEEK -> {
                val cutoffDay = DayOfWeek.of(vehicle.quotaCutoffDay.coerceIn(1, 7))
                val (start, _) = calculateWeeklyCycleBoundsUseCase(nowMillis, cutoffDay, vehicle.quotaCutoffHour, zone)
                start
            }
            HistoryFilterPeriod.MONTH -> now.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli()
            HistoryFilterPeriod.ALL -> 0L
        }
    }
}
