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
    private val syncRepository: SyncRepository
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
        val (cycleStartMillis, cycleEndMillis) = calculateWeeklyCycleBounds(nowMillis, zoneId)

        return combine(
            shiftRepository.getCompletedShiftsHistory(),
            syncRepository.observeSyncStatus()
        ) { completedShifts, syncStatus ->
            // Filtro estricto por jornadas completadas dentro del ciclo Miércoles-Martes
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
                weeklyTarget = Constants.DEFAULT_WEEKLY_QUOTA_DOUBLE,
                accumulatedSoFar = netProfit.coerceAtLeast(0.0),
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
    // BLOQUE 2: CÁLCULO DE BORDES TEMPORALES DEL CICLO (MIÉRCOLES A MARTES)
    // --------------------------------------------------------------------------
    /**
     * Calcula los timestamps de inicio (Miércoles 00:00:00) y fin (Martes 23:59:59.999) del ciclo.
     */
    fun calculateWeeklyCycleBounds(
        nowMillis: Long,
        zoneId: ZoneId = bogotaZone
    ): Pair<Long, Long> {
        val now = Instant.ofEpochMilli(nowMillis).atZone(zoneId)
        val daysSinceWednesday = when (now.dayOfWeek) {
            DayOfWeek.WEDNESDAY -> 0L
            DayOfWeek.THURSDAY -> 1L
            DayOfWeek.FRIDAY -> 2L
            DayOfWeek.SATURDAY -> 3L
            DayOfWeek.SUNDAY -> 4L
            DayOfWeek.MONDAY -> 5L
            DayOfWeek.TUESDAY -> 6L
            null -> 0L
        }

        val cycleStart = now.minusDays(daysSinceWednesday)
            .toLocalDate()
            .atStartOfDay(zoneId)

        val cycleEnd = cycleStart.plusDays(6)
            .toLocalDate()
            .atTime(23, 59, 59, 999_000_000)
            .atZone(zoneId)

        return Pair(
            cycleStart.toInstant().toEpochMilli(),
            cycleEnd.toInstant().toEpochMilli()
        )
    }
}
