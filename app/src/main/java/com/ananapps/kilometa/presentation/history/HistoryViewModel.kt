// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/HistoryViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel para la pantalla de Historial y Analíticas (@HiltViewModel).
// Combina de forma reactiva los flujos de turnos completados y carreras históricas,
// aplicando filtros temporales (Semana, Mes, Todo) con WhileSubscribed(5000ms).
// ==============================================================================

package com.ananapps.kilometa.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.BiFuelSavingsMetrics
import com.ananapps.kilometa.domain.model.PlatformAnalytics
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.usecase.GetBiFuelSavingsUseCase
import com.ananapps.kilometa.domain.usecase.GetPlatformAnalyticsUseCase
import com.ananapps.kilometa.domain.usecase.GetShiftHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: VIEWMODEL DE HISTORIAL Y ANALÍTICAS BI-COMBUSTIBLE
// ------------------------------------------------------------------------------
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getShiftHistoryUseCase: GetShiftHistoryUseCase,
    private val getPlatformAnalyticsUseCase: GetPlatformAnalyticsUseCase,
    private val getBiFuelSavingsUseCase: GetBiFuelSavingsUseCase,
    private val shiftRepository: ShiftRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(HistoryFilterPeriod.ALL)

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
    // BLOQUE 2: ACCIONES DE INTERACCIÓN DE USUARIO
    // --------------------------------------------------------------------------
    /**
     * Alterna la ventana de corte temporal para recalcular las analíticas.
     *
     * @param filter Nuevo período de corte seleccionado.
     */
    fun onFilterSelected(filter: HistoryFilterPeriod) {
        _selectedFilter.value = filter
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: LÓGICA DE AGREGACIÓN Y FILTRADO MATEMÁTICO
    // --------------------------------------------------------------------------
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
