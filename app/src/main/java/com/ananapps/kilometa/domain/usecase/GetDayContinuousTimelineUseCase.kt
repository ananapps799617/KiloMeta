// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/GetDayContinuousTimelineUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso que orquesta el Timeline Continuo estilo KiloMeta.
// Agrupa cronológicamente todas las actividades del día (carreras, tanqueos, gastos)
// y calcula dinámicamente la jornada entre el primer y último registro sin exigir
// un botón bloqueante de "Iniciar Turno".
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.DayActivityEvent
import com.ananapps.kilometa.domain.model.DayContinuousSummary
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DEL TIMELINE CONTINUO
// ------------------------------------------------------------------------------
class GetDayContinuousTimelineUseCase @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val expenseRepository: ExpenseRepository
) {

    operator fun invoke(): Flow<DayContinuousSummary> {
        return combine(
            shiftRepository.getActiveShift(),
            shiftRepository.getAllTrips(),
            shiftRepository.getAllFuelings(),
            expenseRepository.getAllExpenses(),
            shiftRepository.getLastKnownOdometer()
        ) { activeShift, allTrips, allFuelings, allExpenses, lastOdo ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val todayStartMillis = calendar.timeInMillis

            // Filtrar actividades del día de hoy o del turno activo si existe
            val activeShiftId = activeShift?.id
            val todayTrips = allTrips.filter {
                it.timestamp >= todayStartMillis || (activeShiftId != null && it.shiftId == activeShiftId)
            }
            val todayFuelings = allFuelings.filter {
                it.timestamp >= todayStartMillis || (activeShiftId != null && it.shiftId == activeShiftId)
            }
            val todayExpenses = allExpenses.filter {
                it.timestamp >= todayStartMillis || (activeShiftId != null && it.shiftId == activeShiftId)
            }

            // Construir lista polimórfica de eventos del día
            val events = mutableListOf<DayActivityEvent>()
            todayTrips.forEach { events.add(DayActivityEvent.TripItem(it)) }
            todayFuelings.forEach { events.add(DayActivityEvent.FuelingItem(it)) }
            todayExpenses.forEach { events.add(DayActivityEvent.ExpenseItem(it)) }

            // Ordenar de más reciente a más antiguo
            events.sortByDescending { it.timestamp }

            // Calcular totales financieros
            val totalNetIncome = todayTrips.sumOf { it.netAmount }
            val totalGrossProduced = todayTrips.sumOf { it.grossAmount }
            val totalFuelSpent = todayFuelings.sumOf { it.amountPaid }
            val totalExpenses = todayExpenses.sumOf { it.amount }
            val pocketNet = totalNetIncome - totalFuelSpent - totalExpenses

            // Determinar extremos de la jornada
            val firstEventTime = if (activeShift != null) {
                activeShift.startTime
            } else {
                events.minByOrNull { it.timestamp }?.timestamp
            }

            val lastEventTime = if (activeShift != null) {
                System.currentTimeMillis()
            } else {
                events.maxByOrNull { it.timestamp }?.timestamp
            }

            val elapsedHoursFormatted = if (firstEventTime != null && lastEventTime != null && lastEventTime >= firstEventTime) {
                val diffMs = lastEventTime - firstEventTime
                val totalMins = diffMs / (1000 * 60)
                val hours = totalMins / 60
                val mins = totalMins % 60
                "${hours}h ${mins}m"
            } else {
                "0h 0m"
            }

            val tripMinOdo = todayTrips.mapNotNull { it.startOdometer ?: it.endOdometer }.minOrNull()
            val fuelMinOdo = todayFuelings.map { it.odometer }.minOrNull()
            val firstOdoCandidates = listOfNotNull(activeShift?.startOdometer, tripMinOdo, fuelMinOdo)
            val firstOdo = firstOdoCandidates.minOrNull()

            val tripMaxOdo = todayTrips.mapNotNull { it.endOdometer ?: it.startOdometer }.maxOrNull()
            val fuelMaxOdo = todayFuelings.map { it.odometer }.maxOrNull()
            val lastOdoCandidates = listOfNotNull(lastOdo, tripMaxOdo, fuelMaxOdo, activeShift?.endOdometer, activeShift?.startOdometer)
            val effectiveLastOdo = lastOdoCandidates.maxOrNull() ?: firstOdo ?: 0

            val sumTripKm = todayTrips.mapNotNull { it.tripDistanceKm }.sum().toInt()
            val odoDistanceKm = if (firstOdo != null && effectiveLastOdo >= firstOdo) {
                effectiveLastOdo - firstOdo
            } else {
                0
            }
            val distanceKm = maxOf(odoDistanceKm, sumTripKm)

            DayContinuousSummary(
                totalNetIncome = totalNetIncome,
                totalGrossProduced = totalGrossProduced,
                totalFuelSpent = totalFuelSpent,
                totalExpenses = totalExpenses,
                pocketNet = pocketNet,
                totalTripsCount = todayTrips.size,
                firstEventTimestamp = firstEventTime,
                lastEventTimestamp = lastEventTime,
                firstEventOdometer = firstOdo,
                lastEventOdometer = effectiveLastOdo,
                elapsedHoursFormatted = elapsedHoursFormatted,
                distanceTraversedKm = distanceKm,
                hasActiveShift = activeShift != null,
                activeShiftId = activeShift?.id,
                events = events
            )
        }
    }
}
