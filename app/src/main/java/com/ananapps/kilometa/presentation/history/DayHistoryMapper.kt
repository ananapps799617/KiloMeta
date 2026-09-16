// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/DayHistoryMapper.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Mapeador de Dominio/Presentación para agrupar actividades en días calendario (Orden #041).
// Transforma carreras, tanqueos, gastos y mantenimientos en DayHistorySummary independientes de turnos.
// ==============================================================================

package com.ananapps.kilometa.presentation.history

import com.ananapps.kilometa.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

object DayHistoryMapper {

    fun mapToDays(
        trips: List<Trip>,
        fuelings: List<Fueling>,
        expenses: List<Expense>,
        maintenances: List<MaintenanceRecord>,
        cutoffMillis: Long = 0L
    ): List<DayHistorySummary> {
        val fTrips = if (cutoffMillis > 0L) trips.filter { it.timestamp >= cutoffMillis } else trips
        val fFuelings = if (cutoffMillis > 0L) fuelings.filter { it.timestamp >= cutoffMillis } else fuelings
        val fExpenses = if (cutoffMillis > 0L) expenses.filter { it.timestamp >= cutoffMillis } else expenses
        val fMaintenances = if (cutoffMillis > 0L) maintenances.filter { it.timestamp >= cutoffMillis } else maintenances

        val allEvents = mutableListOf<DayActivityEvent>()
        allEvents.addAll(fTrips.map { DayActivityEvent.TripItem(it) })
        allEvents.addAll(fFuelings.map { DayActivityEvent.FuelingItem(it) })
        allEvents.addAll(fExpenses.map { DayActivityEvent.ExpenseItem(it) })
        allEvents.addAll(fMaintenances.map { DayActivityEvent.MaintenanceItem(it) })

        if (allEvents.isEmpty()) return emptyList()

        val dayKeyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayTitleFormatter = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "CO"))
        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val grouped = allEvents.groupBy { dayKeyFormatter.format(Date(it.timestamp)) }

        return grouped.map { (_, events) ->
            val sortedEvents = events.sortedByDescending { it.timestamp }
            val firstEventTime = sortedEvents.minOf { it.timestamp }
            val lastEventTime = sortedEvents.maxOf { it.timestamp }

            val dayTrips = events.filterIsInstance<DayActivityEvent.TripItem>().map { it.trip }
            val dayFuelings = events.filterIsInstance<DayActivityEvent.FuelingItem>().map { it.fueling }
            val dayExpenses = events.filterIsInstance<DayActivityEvent.ExpenseItem>().map { it.expense }
            val dayMaintenances = events.filterIsInstance<DayActivityEvent.MaintenanceItem>().map { it.maintenance }

            val gross = dayTrips.sumOf { it.grossAmount }
            val commissions = dayTrips.sumOf { it.commissionAmount }
            val netTrips = dayTrips.sumOf { it.netAmount }
            val fuelCost = dayFuelings.sumOf { it.amountPaid }
            val expensesCost = dayExpenses.sumOf { it.amount } + dayMaintenances.sumOf { it.costCop }
            val netProfit = netTrips - fuelCost - expensesCost

            val odos = (dayTrips.mapNotNull { it.startOdometer } + dayTrips.mapNotNull { it.endOdometer } +
                    dayFuelings.map { it.odometer } + dayMaintenances.map { it.odometer }).filter { it > 0 }

            val startOdo = odos.minOrNull() ?: 0
            val endOdo = odos.maxOrNull() ?: startOdo
            val odoDiff = if (endOdo >= startOdo && startOdo > 0) endOdo - startOdo else 0
            val tripKm = dayTrips.mapNotNull { it.tripDistanceKm }.sum().toInt()
            val distanceKm = maxOf(odoDiff, tripKm)

            val elapsedMinutes = ((lastEventTime - firstEventTime) / 60000).coerceAtLeast(0)
            val elapsedHours = elapsedMinutes / 60
            val elapsedRemMinutes = elapsedMinutes % 60
            val elapsedFormatted = "${elapsedHours}h ${elapsedRemMinutes}m"

            val ratePerKm = if (distanceKm > 0) netProfit / distanceKm else 0.0

            DayHistorySummary(
                dateMillis = firstEventTime,
                dateFormatted = dayTitleFormatter.format(Date(firstEventTime)).replaceFirstChar { it.uppercase() },
                netProfit = netProfit,
                grossProduced = gross,
                appCommissions = commissions,
                fuelSpent = fuelCost,
                expensesSpent = expensesCost,
                startOdometer = startOdo,
                endOdometer = endOdo,
                distanceKm = distanceKm,
                startTimeFormatted = timeFormatter.format(Date(firstEventTime)),
                endTimeFormatted = timeFormatter.format(Date(lastEventTime)),
                elapsedHoursFormatted = elapsedFormatted,
                ratePerKm = ratePerKm,
                events = sortedEvents
            )
        }.sortedByDescending { it.dateMillis }
    }
}
