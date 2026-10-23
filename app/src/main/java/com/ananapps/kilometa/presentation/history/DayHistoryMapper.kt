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
        shifts: List<ShiftFinancialSummary>,
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

        return grouped.map { (dayKey, events) ->
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

            // Prioridad Soberana SSOT: Odómetro y Horas de la Jornada del Conductor (Orden #068)
            val matchingShift = shifts.find { s ->
                dayKeyFormatter.format(Date(s.startTime)) == dayKey
            }

            val tripKm = dayTrips.mapNotNull { it.tripDistanceKm }.sum().toInt()
            val (startOdo, endOdo, distanceKm) = if (matchingShift != null && matchingShift.startOdometer > 0) {
                val sOdo = matchingShift.startOdometer
                val eOdo = matchingShift.endOdometer?.takeIf { it >= sOdo } ?: sOdo
                val diff = if (eOdo > sOdo) eOdo - sOdo else 0
                val finalDist = if (diff > 0) diff else tripKm
                Triple(sOdo, eOdo, finalDist)
            } else {
                Triple(0, 0, tripKm)
            }

            val odoDiff = if (endOdo > startOdo && startOdo > 0) endOdo - startOdo else 0
            val passengerKm = dayTrips.mapNotNull { it.tripDistanceKm }.sum()
            val emptyKm = if (odoDiff > passengerKm && odoDiff > 0) odoDiff - passengerKm else 0.0

            val startTimeMillis = matchingShift?.startTime ?: firstEventTime
            val endTimeMillis = matchingShift?.endTime ?: lastEventTime
            val grossElapsedMinutes = ((endTimeMillis - startTimeMillis) / 60000).coerceAtLeast(0).toInt()
            val breakMinutes = parseBreakMinutes(matchingShift?.notes)
            val effectiveWorkMinutes = (grossElapsedMinutes - breakMinutes).coerceAtLeast(0)

            val elapsedFormatted = formatDuration(grossElapsedMinutes)
            val effectiveHoursFormatted = formatDuration(effectiveWorkMinutes)
            val breakHoursFormatted = formatDuration(breakMinutes)

            val passengerTimeMinutes = dayTrips.mapNotNull { it.durationMinutes }.sum()
            val emptyTimeMinutes = (effectiveWorkMinutes - passengerTimeMinutes).coerceAtLeast(0)
            val passengerTimeFormatted = formatDuration(passengerTimeMinutes)
            val emptyTimeFormatted = formatDuration(emptyTimeMinutes)

            val ratePerKm = if (distanceKm > 0) netProfit / distanceKm else 0.0
            val ratePerHour = if (effectiveWorkMinutes > 0) {
                netProfit / (effectiveWorkMinutes / 60.0)
            } else if (grossElapsedMinutes > 0) {
                netProfit / (grossElapsedMinutes / 60.0)
            } else {
                0.0
            }

            val resolvedShiftId = matchingShift?.shiftId
                ?: dayTrips.firstOrNull()?.shiftId
                ?: dayFuelings.firstOrNull()?.shiftId
                ?: dayExpenses.firstOrNull()?.shiftId

            val platformEarnings = PlatformType.entries.mapNotNull { platform ->
                val tripsForPlatform = dayTrips.filter { it.platformType == platform }
                val netSum = tripsForPlatform.sumOf { it.netAmount }
                if (netSum > 0.0) platform to netSum else null
            }.toMap()

            DayHistorySummary(
                dateMillis = startTimeMillis,
                dateFormatted = dayTitleFormatter.format(Date(startTimeMillis)).replaceFirstChar { it.uppercase() },
                netProfit = netProfit,
                grossProduced = gross,
                appCommissions = commissions,
                fuelSpent = fuelCost,
                expensesSpent = expensesCost,
                startOdometer = startOdo,
                endOdometer = endOdo,
                distanceKm = distanceKm,
                startTimeFormatted = timeFormatter.format(Date(startTimeMillis)),
                endTimeFormatted = timeFormatter.format(Date(endTimeMillis)),
                elapsedHoursFormatted = elapsedFormatted,
                ratePerKm = ratePerKm,
                events = sortedEvents,
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis,
                shiftId = resolvedShiftId,
                platformEarnings = platformEarnings,
                passengerKm = passengerKm,
                emptyKm = emptyKm,
                ratePerHour = ratePerHour,
                breakMinutes = breakMinutes,
                effectiveWorkMinutes = effectiveWorkMinutes,
                passengerTimeMinutes = passengerTimeMinutes,
                emptyTimeMinutes = emptyTimeMinutes,
                breakHoursFormatted = breakHoursFormatted,
                effectiveHoursFormatted = effectiveHoursFormatted,
                passengerTimeFormatted = passengerTimeFormatted,
                emptyTimeFormatted = emptyTimeFormatted
            )
        }.sortedByDescending { it.dateMillis }
    }

    private fun parseBreakMinutes(notes: String?): Int {
        if (notes.isNullOrBlank()) return 0
        val match = Regex("BREAKS:(\\d+)").find(notes)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    private fun formatDuration(totalMins: Int): String {
        val h = totalMins / 60
        val m = totalMins % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    fun mapToDays(
        trips: List<Trip>,
        fuelings: List<Fueling>,
        expenses: List<Expense>,
        maintenances: List<MaintenanceRecord>,
        cutoffMillis: Long = 0L
    ): List<DayHistorySummary> = mapToDays(emptyList(), trips, fuelings, expenses, maintenances, cutoffMillis)
}
