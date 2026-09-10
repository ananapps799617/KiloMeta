// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/GetAccountingAuditDashboardUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso de Dominio que consolida la auditoría contable y vehicular del ciclo semanal
// (Miércoles 00:00:00 a Martes 23:59:59 en Bogotá). Cruza turnos cerrados, gastos,
// cuota semanal y estado de sincronización en Firestore.
// Regla de Oro: Cero dependencias del framework Android en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.AccountingAuditSummary
import com.ananapps.kilometa.domain.model.SyncState
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE AUDITORÍA CONTABLE AVANZADA
// ------------------------------------------------------------------------------
/**
 * Emite de forma reactiva el balance contable semanal unificado para el conductor.
 */
class GetAccountingAuditDashboardUseCase @Inject constructor(
    private val shiftRepository: ShiftRepository,
    @Suppress("unused") private val expenseRepository: ExpenseRepository,
    private val calculateDailyQuotaUseCase: CalculateDailyQuotaUseCase,
    private val syncRepository: SyncRepository,
    private val driverProfileRepository: DriverProfileRepository
) {

    private val bogotaZone: ZoneId = ZoneId.of("America/Bogota")

    /**
     * Observa reactivamente el balance contable consolidado para el ciclo en curso.
     *
     * @param nowMillis Marca de tiempo base para calcular el ciclo semanal (default: ahora).
     * @param zoneId Zona horaria de referencia (default: America/Bogota).
     * @return [Flow] que emite el [AccountingAuditSummary] actualizado ante cualquier cambio en DB o Cloud.
     */
    operator fun invoke(
        nowMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = bogotaZone
    ): Flow<AccountingAuditSummary> {
        return combine(
            shiftRepository.getCompletedShiftsHistory(),
            syncRepository.observeSyncStatus(),
            driverProfileRepository.getVehicleProfile()
        ) { completedShifts, syncStatus, vehicleProfile ->
            val targetCutoffDay = DayOfWeek.of(vehicleProfile.quotaCutoffDay.coerceIn(1, 7))
            val targetCutoffHour = vehicleProfile.quotaCutoffHour.coerceIn(0, 23)
            val weeklyTarget = if (vehicleProfile.quotaAmount > 0) vehicleProfile.quotaAmount else Constants.DEFAULT_WEEKLY_QUOTA_DOUBLE

            val (cycleStartMillis, cycleEndMillis) = calculateWeeklyCycleBounds(
                nowMillis = nowMillis,
                zoneId = zoneId,
                cutoffDay = targetCutoffDay,
                cutoffHour = targetCutoffHour
            )

            // Filtro estricto por jornadas completadas dentro del ciclo continuo de 168 horas
            val weeklyShifts = completedShifts.filter { shift ->
                shift.startTime in cycleStartMillis..cycleEndMillis
            }

            val totalGross = weeklyShifts.sumOf { it.totalGrossIncome }
            val totalCommissions = weeklyShifts.sumOf { it.totalCommissions }
            val totalNetInHand = weeklyShifts.sumOf { it.totalNetIncome }
            val totalKm = weeklyShifts.sumOf { it.totalKilometers }
            val totalGas = weeklyShifts.sumOf { it.totalGasExpense }
            val totalGasoline = weeklyShifts.sumOf { it.totalGasolineExpense }
            val totalOperational = weeklyShifts.sumOf { it.totalOperationalExpenses }
            val netProfit = weeklyShifts.sumOf { it.netProfit }

            val totalOperatingCost = totalGas + totalGasoline + totalOperational
            val costPerKm = if (totalKm > 0) totalOperatingCost / totalKm.toDouble() else 0.0
            val revenuePerKm = if (totalKm > 0) totalGross / totalKm.toDouble() else 0.0

            val totalFuel = totalGas + totalGasoline
            val fuelRatio = if (totalGross > 0.0) {
                (totalFuel / totalGross) * 100.0
            } else {
                0.0
            }

            val quotaProgress = calculateDailyQuotaUseCase(
                weeklyTarget = weeklyTarget,
                accumulatedSoFar = netProfit.coerceAtLeast(0.0),
                cycleEndMillis = cycleEndMillis,
                cutoffDayOfWeek = targetCutoffDay,
                cutoffHour = targetCutoffHour,
                nowMillis = nowMillis,
                zoneId = zoneId
            )

            val isCloudSyncHealthy = syncStatus.state != SyncState.ERROR

            AccountingAuditSummary(
                cycleStartDateMillis = cycleStartMillis,
                cycleEndDateMillis = cycleEndMillis,
                totalGrossIncome = totalGross,
                totalNetIncomeInHand = totalNetInHand,
                totalCommissions = totalCommissions,
                totalKilometers = totalKm,
                costPerKilometer = costPerKm,
                revenuePerKilometer = revenuePerKm,
                fuelExpenseToIncomeRatio = fuelRatio,
                totalGasExpense = totalGas,
                totalGasolineExpense = totalGasoline,
                totalOperationalExpenses = totalOperational,
                netProfit = netProfit,
                quotaProgress = quotaProgress,
                lastCloudSyncMillis = syncStatus.lastSyncTimestamp,
                isCloudSyncHealthy = isCloudSyncHealthy
            )
        }
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: CÁLCULO DE BORDES TEMPORALES DEL CICLO (VENTANA CONTINUA DE 168 HORAS)
    // --------------------------------------------------------------------------
    /**
     * Calcula los timestamps de inicio y fin del ciclo semanal según el corte configurado.
     */
    fun calculateWeeklyCycleBounds(
        nowMillis: Long,
        zoneId: ZoneId = bogotaZone,
        cutoffDay: DayOfWeek = DayOfWeek.TUESDAY,
        cutoffHour: Int = 23
    ): Pair<Long, Long> {
        val now = Instant.ofEpochMilli(nowMillis).atZone(zoneId)

        var cutoffCandidate = if (cutoffHour >= 23) {
            now.with(java.time.temporal.TemporalAdjusters.nextOrSame(cutoffDay))
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999_000_000)
        } else {
            now.with(java.time.temporal.TemporalAdjusters.nextOrSame(cutoffDay))
                .withHour(cutoffHour)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
        }

        if (!now.isBefore(cutoffCandidate)) {
            cutoffCandidate = cutoffCandidate.plusWeeks(1)
        }

        val cycleEnd = cutoffCandidate
        val cycleStart = if (cutoffHour >= 23) {
            cycleEnd.minusDays(6).toLocalDate().atStartOfDay(zoneId)
        } else {
            cycleEnd.minusWeeks(1)
        }

        return Pair(
            cycleStart.toInstant().toEpochMilli(),
            cycleEnd.toInstant().toEpochMilli()
        )
    }
}
