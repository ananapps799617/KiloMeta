// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/HistoryDataProcessor.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Procesador desacoplado de analíticas y filtrado de datos históricos para HistoryViewModel.
// Garantiza la modularidad estricta (<= 280 LOC) de la capa de presentación.
// ==============================================================================

package com.ananapps.kilometa.presentation.history

import com.ananapps.kilometa.domain.model.*
import com.ananapps.kilometa.domain.usecase.CalculateWeeklyCycleBoundsUseCase
import com.ananapps.kilometa.domain.usecase.GetBiFuelSavingsUseCase
import com.ananapps.kilometa.domain.usecase.GetPlatformAnalyticsUseCase
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class HistoryDataProcessor @Inject constructor(
    private val calculateWeeklyCycleBoundsUseCase: CalculateWeeklyCycleBoundsUseCase,
    private val getBiFuelSavingsUseCase: GetBiFuelSavingsUseCase,
    private val getPlatformAnalyticsUseCase: GetPlatformAnalyticsUseCase
) {

    fun process(
        filter: HistoryFilterPeriod,
        shifts: List<ShiftFinancialSummary>,
        trips: List<Trip>,
        fuelings: List<Fueling>,
        expenses: List<Expense>,
        maintenances: List<MaintenanceRecord>,
        vehicle: VehicleProfile
    ): HistoryUiState {
        val cutoffMillis = calculatePeriodCutoff(filter, vehicle)
        val filteredShifts = if (cutoffMillis > 0L) shifts.filter { it.startTime >= cutoffMillis } else shifts
        val days = DayHistoryMapper.mapToDays(trips, fuelings, expenses, maintenances, cutoffMillis)

        if (shifts.isEmpty() && days.isEmpty()) return HistoryUiState.Empty(selectedFilter = filter)
        if (filteredShifts.isEmpty() && days.isEmpty()) return HistoryUiState.Empty(selectedFilter = filter)

        val filteredTrips = if (cutoffMillis > 0L) trips.filter { it.timestamp >= cutoffMillis } else trips
        val totalNetProfit = if (days.isNotEmpty()) days.sumOf { it.netProfit } else filteredShifts.sumOf { it.netProfit }
        val totalKilometers = if (days.isNotEmpty()) days.sumOf { it.distanceKm } else filteredShifts.sumOf { it.totalKilometers }
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
            days = days,
            selectedFilter = filter
        )
    }

    fun calculatePeriodCutoff(period: HistoryFilterPeriod, vehicle: VehicleProfile, nowMillis: Long = System.currentTimeMillis()): Long {
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
