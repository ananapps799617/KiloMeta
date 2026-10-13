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
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.MaintenanceRecord
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.domain.model.WalletTransfer
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import com.ananapps.kilometa.domain.repository.MaintenanceRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.repository.WalletTransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DEL TIMELINE CONTINUO (JORNADA 24/7 AUTOMÁTICA)
// ------------------------------------------------------------------------------
class GetDayContinuousTimelineUseCase @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val expenseRepository: ExpenseRepository,
    private val walletTransferRepository: WalletTransferRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val calculateShiftCashSettlementUseCase: CalculateShiftCashSettlementUseCase,
    private val driverProfileRepository: DriverProfileRepository,
    private val calculateWeeklyCycleBoundsUseCase: CalculateWeeklyCycleBoundsUseCase
) {

    @Suppress("UNCHECKED_CAST")
    operator fun invoke(): Flow<DayContinuousSummary> {
        return combine(
            shiftRepository.getAllTrips(),
            shiftRepository.getAllFuelings(),
            expenseRepository.getAllExpenses(),
            shiftRepository.getLastKnownOdometer(),
            walletTransferRepository.getAllTransfers(),
            maintenanceRepository.getAllMaintenances(),
            driverProfileRepository.getVehicleProfile(),
            shiftRepository.getAllShifts()
        ) { args: Array<Any?> ->
            val allTrips = args[0] as List<Trip>
            val allFuelings = args[1] as List<Fueling>
            val allExpenses = args[2] as List<Expense>
            val lastOdo = args[3] as? Int
            val allTransfers = args[4] as List<WalletTransfer>
            val allMaintenances = args[5] as List<MaintenanceRecord>
            val vehicle = args[6] as VehicleProfile
            val allShifts = args[7] as List<com.ananapps.kilometa.domain.model.Shift>

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val todayStartMillis = calendar.timeInMillis

            // Filtrar estrictamente actividades del día de hoy (00:00:00 a 23:59:59)
            val todayTrips = allTrips.filter { it.timestamp >= todayStartMillis }
            val todayFuelings = allFuelings.filter { it.timestamp >= todayStartMillis }
            val todayExpenses = allExpenses.filter { it.timestamp >= todayStartMillis }
            val todayTransfers = allTransfers.filter { it.timestamp >= todayStartMillis }
            val todayMaintenances = allMaintenances.filter { it.timestamp >= todayStartMillis }

            // Construir lista polimórfica de eventos del día
            val events = mutableListOf<DayActivityEvent>()
            todayTrips.forEach { events.add(DayActivityEvent.TripItem(it)) }
            todayFuelings.forEach { events.add(DayActivityEvent.FuelingItem(it)) }
            todayExpenses.forEach { events.add(DayActivityEvent.ExpenseItem(it)) }
            todayTransfers.forEach { events.add(DayActivityEvent.TransferItem(it)) }
            todayMaintenances.forEach { events.add(DayActivityEvent.MaintenanceItem(it)) }

            // Ordenar de más reciente a más antiguo
            events.sortByDescending { it.timestamp }

            // Totales financieros de hoy
            val totalNetIncome = todayTrips.sumOf { it.netAmount }
            val totalGrossProduced = todayTrips.sumOf { it.grossAmount }
            val totalFuelSpent = todayFuelings.sumOf { it.amountPaid }
            val totalExpenses = todayExpenses.sumOf { it.amount } + todayMaintenances.sumOf { it.costCop }
            val dailyQuotaTarget = if (vehicle.workDaysWeek > 0 && vehicle.quotaAmount > 0.0) {
                vehicle.quotaAmount / vehicle.workDaysWeek
            } else {
                0.0
            }
            val netOperationalMargin = totalNetIncome - totalFuelSpent - totalExpenses
            val pocketNet = netOperationalMargin - dailyQuotaTarget

            // Horario de actividad del día
            val firstEventTime = events.minByOrNull { it.timestamp }?.timestamp
            val lastEventTime = events.maxByOrNull { it.timestamp }?.timestamp

            val elapsedHoursFormatted = if (firstEventTime != null && lastEventTime != null && lastEventTime >= firstEventTime) {
                val diffMs = lastEventTime - firstEventTime
                val totalMins = diffMs / (1000 * 60)
                val hours = totalMins / 60
                val mins = totalMins % 60
                "${hours}h ${mins}m"
            } else {
                "0h 0m"
            }

            // Odómetros automáticos y jornada de hoy
            val todayShifts = allShifts.filter { it.startTime >= todayStartMillis || (it.endTime ?: 0L) >= todayStartMillis }
            val activeShift = allShifts.firstOrNull { it.status == com.ananapps.kilometa.domain.model.ShiftStatus.ACTIVE }
            val hasActiveShift = activeShift != null
            val activeShiftId = activeShift?.id

            val primaryShift = activeShift ?: todayShifts.firstOrNull { it.status == com.ananapps.kilometa.domain.model.ShiftStatus.COMPLETED } ?: todayShifts.firstOrNull()
            val shiftStartOdo = primaryShift?.startOdometer?.takeIf { it > 0 }
            val shiftEndOdo = if (hasActiveShift) null else primaryShift?.endOdometer?.takeIf { it > 0 && it >= (shiftStartOdo ?: 0) }

            val tripMinOdo = todayTrips.mapNotNull { it.startOdometer ?: it.endOdometer }.minOrNull()
            val fuelMinOdo = todayFuelings.map { it.odometer }.minOrNull()
            val maintMinOdo = todayMaintenances.map { it.odometer }.minOrNull()
            val firstOdoCandidates = listOfNotNull(shiftStartOdo, tripMinOdo, fuelMinOdo, maintMinOdo)
            val firstOdo = firstOdoCandidates.minOrNull() ?: lastOdo ?: vehicle.initialOdometer

            val tripMaxOdo = todayTrips.mapNotNull { it.endOdometer ?: it.startOdometer }.maxOrNull()
            val fuelMaxOdo = todayFuelings.map { it.odometer }.maxOrNull()
            val maintMaxOdo = todayMaintenances.map { it.odometer }.maxOrNull()
            val lastOdoCandidates = listOfNotNull(shiftEndOdo, tripMaxOdo, fuelMaxOdo, maintMaxOdo)
            val effectiveLastOdo = lastOdoCandidates.maxOrNull() ?: firstOdo

            val isDayClosed = !hasActiveShift && (primaryShift?.status == com.ananapps.kilometa.domain.model.ShiftStatus.COMPLETED && shiftEndOdo != null && shiftEndOdo >= (shiftStartOdo ?: 0))
            val sumTripKm = todayTrips.mapNotNull { it.tripDistanceKm }.sum().toInt()
            val odoDistanceKm = if (firstOdo > 0 && effectiveLastOdo > firstOdo) {
                effectiveLastOdo - firstOdo
            } else {
                0
            }
            val distanceKm = maxOf(odoDistanceKm, sumTripKm)

            val cutoffDay = when (vehicle.quotaCutoffDay) {
                1 -> DayOfWeek.MONDAY
                2 -> DayOfWeek.TUESDAY
                3 -> DayOfWeek.WEDNESDAY
                4 -> DayOfWeek.THURSDAY
                5 -> DayOfWeek.FRIDAY
                6 -> DayOfWeek.SATURDAY
                7 -> DayOfWeek.SUNDAY
                else -> DayOfWeek.TUESDAY
            }

            // 1. Ciclo Operativo del Conductor y Cuota del Carro (según día de corte configurado)
            val (cycleStartMillis, cycleEndMillis) = calculateWeeklyCycleBoundsUseCase(
                cutoffDay = cutoffDay,
                cutoffHour = vehicle.quotaCutoffHour
            )
            val cycleLabel = formatCycleRange(cycleStartMillis, cycleEndMillis)

            // Acumulan transacciones del ciclo en curso del conductor
            val cycleTrips = allTrips.filter { it.timestamp in cycleStartMillis..cycleEndMillis }
            val cycleFuelings = allFuelings.filter { it.timestamp in cycleStartMillis..cycleEndMillis }
            val cycleExpenses = allExpenses.filter { it.timestamp in cycleStartMillis..cycleEndMillis }
            val cycleTransfers = allTransfers.filter { it.timestamp in cycleStartMillis..cycleEndMillis }

            // 2. Ciclo Soberano de Bolsillos de Plataformas (Estricto Lunes 00:00 a Domingo 23:59)
            val (platformStartMillis, platformEndMillis) = calculateWeeklyCycleBoundsUseCase(
                cutoffDay = DayOfWeek.SUNDAY,
                cutoffHour = 23
            )
            val platformCycleTrips = allTrips.filter { it.timestamp in platformStartMillis..platformEndMillis }

            val cashSettlement = calculateShiftCashSettlementUseCase(
                trips = cycleTrips,
                fuelings = cycleFuelings,
                expenses = cycleExpenses,
                transfers = cycleTransfers,
                platformTrips = platformCycleTrips
            )

            DayContinuousSummary(
                totalNetIncome = totalNetIncome,
                totalGrossProduced = totalGrossProduced,
                totalFuelSpent = totalFuelSpent,
                totalExpenses = totalExpenses,
                netOperationalMargin = netOperationalMargin,
                dailyQuotaTarget = dailyQuotaTarget,
                pocketNet = pocketNet,
                totalTripsCount = todayTrips.size,
                firstEventTimestamp = firstEventTime,
                lastEventTimestamp = lastEventTime,
                firstEventOdometer = firstOdo,
                lastEventOdometer = effectiveLastOdo,
                elapsedHoursFormatted = elapsedHoursFormatted,
                distanceTraversedKm = distanceKm,
                hasActiveShift = hasActiveShift,
                activeShiftId = activeShiftId,
                cashSettlement = cashSettlement,
                cycleLabel = cycleLabel,
                cycleStartMillis = cycleStartMillis,
                cycleEndMillis = cycleEndMillis,
                events = events,
                dayStartOdometer = shiftStartOdo ?: firstOdo,
                dayEndOdometer = shiftEndOdo,
                isDayClosed = isDayClosed
            )
        }
    }
}

private fun formatCycleRange(cycleStartMillis: Long, cycleEndMillis: Long): String {
    val zoneId = ZoneId.of("America/Bogota")
    val start = Instant.ofEpochMilli(cycleStartMillis).atZone(zoneId)
    val end = Instant.ofEpochMilli(cycleEndMillis).atZone(zoneId)

    val startDay = start.dayOfMonth
    val endDay = end.dayOfMonth
    val startMonth = start.month.getDisplayName(TextStyle.FULL, Locale("es", "CO"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "CO")) else it.toString() }
    val endMonth = end.month.getDisplayName(TextStyle.FULL, Locale("es", "CO"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "CO")) else it.toString() }
    val startYear = start.year
    val endYear = end.year

    return when {
        startYear != endYear -> {
            "Semana del $startDay de $startMonth de $startYear al $endDay de $endMonth de $endYear"
        }
        start.month != end.month -> {
            "Semana del $startDay de $startMonth al $endDay de $endMonth de $endYear"
        }
        else -> {
            "Semana del $startDay al $endDay de $endMonth de $endYear"
        }
    }
}
