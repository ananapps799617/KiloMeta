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
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.usecase.GenerateCsvReportUseCase
import com.ananapps.kilometa.domain.usecase.GenerateShareableSummaryUseCase
import com.ananapps.kilometa.domain.usecase.GetBiFuelSavingsUseCase
import com.ananapps.kilometa.domain.usecase.GetPlatformAnalyticsUseCase
import com.ananapps.kilometa.domain.usecase.GetShiftHistoryUseCase
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
    private val shiftRepository: ShiftRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(HistoryFilterPeriod.ALL)

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    val uiState: StateFlow<HistoryUiState> = combine(
        _selectedFilter,
        getShiftHistoryUseCase(),
        shiftRepository.getAllTrips()
    ) { filter, completedShifts, allTrips ->
        processHistoricalData(filter, completedShifts, allTrips)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = HistoryUiState.Loading
    )

    // --------------------------------------------------------------------------
    // BLOQUE 2: ACCIONES DE INTERACCIÓN DE USUARIO Y FILTROS
    // --------------------------------------------------------------------------
    /**
     * Alterna la ventana de corte temporal para recalcular las analíticas.
     */
    fun onFilterSelected(filter: HistoryFilterPeriod) {
        _selectedFilter.value = filter
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: EXPORTACIÓN CONTABLE Y MENSAJERÍA ASÍNCRONA
    // --------------------------------------------------------------------------
    /**
     * Compila y prepara el archivo CSV con BOM UTF-8 para el período seleccionado.
     */
    fun onExportCsvClicked(period: HistoryFilterPeriod) {
        viewModelScope.launch {
            _exportState.value = ExportUiState.Exporting
            try {
                val allShifts = getShiftHistoryUseCase().first()
                val filteredShifts = filterShifts(period, allShifts)
                val report = generateCsvReportUseCase(period, filteredShifts)
                _exportState.value = ExportUiState.CsvReady(report)
            } catch (e: Exception) {
                _exportState.value = ExportUiState.Error(
                    e.localizedMessage ?: "Error al compilar el reporte CSV contable"
                )
            }
        }
    }

    /**
     * Genera la tarjeta de texto consolidada para difusión en WhatsApp o Telegram.
     */
    fun onShareSummaryClicked(period: HistoryFilterPeriod) {
        viewModelScope.launch {
            _exportState.value = ExportUiState.Exporting
            try {
                val allShifts = getShiftHistoryUseCase().first()
                val filteredShifts = filterShifts(period, allShifts)
                val summary = generateShareableSummaryUseCase(period, filteredShifts)
                _exportState.value = ExportUiState.ShareReady(summary)
            } catch (e: Exception) {
                _exportState.value = ExportUiState.Error(
                    e.localizedMessage ?: "Error al generar el balance compartible"
                )
            }
        }
    }

    /**
     * Genera el balance individual de una jornada específica para WhatsApp.
     */
    fun onShareShiftSummaryClicked(shift: ShiftFinancialSummary) {
        viewModelScope.launch {
            try {
                val summary = generateShareableSummaryUseCase(shift)
                _exportState.value = ExportUiState.ShareReady(summary)
            } catch (e: Exception) {
                _exportState.value = ExportUiState.Error(
                    e.localizedMessage ?: "Error al generar liquidación de jornada"
                )
            }
        }
    }

    /**
     * Restablece el estado de exportación a Idle tras completar el flujo con SAF o Intent.
     */
    fun resetExportState() {
        _exportState.value = ExportUiState.Idle
    }

    // --------------------------------------------------------------------------
    // BLOQUE 4: LÓGICA DE AGREGACIÓN Y FILTRADO MATEMÁTICO
    // --------------------------------------------------------------------------
    private fun filterShifts(
        period: HistoryFilterPeriod,
        shifts: List<ShiftFinancialSummary>
    ): List<ShiftFinancialSummary> {
        val cutoffMillis = calculatePeriodCutoff(period)
        return if (cutoffMillis > 0L) {
            shifts.filter { it.startTime >= cutoffMillis }
        } else {
            shifts
        }
    }

    private fun processHistoricalData(
        filter: HistoryFilterPeriod,
        shifts: List<ShiftFinancialSummary>,
        trips: List<Trip>
    ): HistoryUiState {
        if (shifts.isEmpty()) {
            return HistoryUiState.Empty(selectedFilter = filter)
        }

        val cutoffMillis = calculatePeriodCutoff(filter)
        val filteredShifts = if (cutoffMillis > 0L) {
            shifts.filter { it.startTime >= cutoffMillis }
        } else {
            shifts
        }

        if (filteredShifts.isEmpty()) {
            return HistoryUiState.Empty(selectedFilter = filter)
        }

        val filteredTrips = if (cutoffMillis > 0L) {
            trips.filter { it.timestamp >= cutoffMillis }
        } else {
            trips
        }

        val totalNetProfit = filteredShifts.sumOf { it.netProfit }
        val totalKilometers = filteredShifts.sumOf { it.totalKilometers }
        val totalTrips = if (filteredTrips.isNotEmpty()) {
            filteredTrips.size
        } else {
            filteredShifts.sumOf { it.tripsCount }
        }

        val totalGasExpense = filteredShifts.sumOf { it.totalGasExpense }
        val totalGasolineExpense = filteredShifts.sumOf { it.totalGasolineExpense }

        val savingsMetrics: BiFuelSavingsMetrics = getBiFuelSavingsUseCase(
            totalKmDriven = totalKilometers,
            totalGasExpense = totalGasExpense,
            totalGasolineExpense = totalGasolineExpense
        )

        val platformBreakdowns: List<PlatformAnalytics> = getPlatformAnalyticsUseCase(filteredTrips)

        return HistoryUiState.Success(
            totalNetProfit = totalNetProfit,
            totalKilometers = totalKilometers,
            totalTrips = totalTrips,
            biFuelSavings = savingsMetrics,
            platformBreakdowns = platformBreakdowns,
            shifts = filteredShifts,
            selectedFilter = filter
        )
    }

    private fun calculatePeriodCutoff(
        period: HistoryFilterPeriod,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val zone = ZoneId.of("America/Bogota")
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        return when (period) {
            HistoryFilterPeriod.WEEK -> {
                now.with(DayOfWeek.MONDAY)
                    .truncatedTo(ChronoUnit.DAYS)
                    .toInstant()
                    .toEpochMilli()
            }
            HistoryFilterPeriod.MONTH -> {
                now.with(TemporalAdjusters.firstDayOfMonth())
                    .truncatedTo(ChronoUnit.DAYS)
                    .toInstant()
                    .toEpochMilli()
            }
            HistoryFilterPeriod.ALL -> 0L
        }
    }
}
