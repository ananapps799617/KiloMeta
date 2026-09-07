// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/worker/WeeklyCutoffWorker.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Worker de WorkManager para disparar notificaciones vehiculares de corte de cuota.
// Soporta aviso preventivo (18:00) o balance final (23:59:59) evaluando los turnos del ciclo.
// Inyectado con Hilt mediante @HiltWorker y @AssistedInject.
// ==============================================================================

package com.ananapps.kilometa.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.repository.VehicularNotificationNotifier
import com.ananapps.kilometa.domain.usecase.CalculateDailyQuotaUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId

// ------------------------------------------------------------------------------
// BLOQUE 1: COROUTINE WORKER PARA ALERTAS VEHICULARES DE CORTE
// ------------------------------------------------------------------------------
@HiltWorker
class WeeklyCutoffWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val shiftRepository: ShiftRepository,
    private val calculateDailyQuotaUseCase: CalculateDailyQuotaUseCase,
    private val notifier: VehicularNotificationNotifier
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_IS_FINAL = "key_is_final_cutoff"
        const val TAG_WEEKLY_CUTOFF = "kilometa_weekly_cutoff"
    }

    override suspend fun doWork(): Result {
        return runCatching {
            val isFinal = inputData.getBoolean(KEY_IS_FINAL, false)
            val nowMillis = System.currentTimeMillis()
            val zoneId = ZoneId.of("America/Bogota")

            val (cycleStartMillis, cycleEndMillis) = calculateCycleBounds(nowMillis, zoneId)

            // Obtiene los turnos completados y filtra los del ciclo actual
            val completedShifts = shiftRepository.getCompletedShiftsHistory().first()
            val weeklyShifts = completedShifts.filter { shift ->
                shift.startTime in cycleStartMillis..cycleEndMillis
            }

            val accumulatedNetProfit = weeklyShifts.sumOf { it.netProfit }.coerceAtLeast(0.0)

            val quotaProgress = calculateDailyQuotaUseCase(
                weeklyTarget = Constants.DEFAULT_WEEKLY_QUOTA_DOUBLE,
                accumulatedSoFar = accumulatedNetProfit,
                nowMillis = nowMillis,
                zoneId = zoneId
            )

            if (isFinal) {
                notifier.showWeeklyCutoffFinal(
                    achieved = quotaProgress.isWeeklyTargetAchieved,
                    achievedAmount = quotaProgress.accumulatedSoFar,
                    targetAmount = quotaProgress.weeklyTarget
                )
            } else {
                notifier.showWeeklyCutoffWarning(
                    percentage = quotaProgress.progressPercentage.toInt(),
                    remainingAmount = quotaProgress.remainingTarget,
                    deadlineHour = "23:59"
                )
            }

            Result.success()
        }.getOrElse {
            Result.failure()
        }
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: CÁLCULO DE BORDES DEL CICLO OPERACIONAL
    // --------------------------------------------------------------------------
    private fun calculateCycleBounds(nowMillis: Long, zoneId: ZoneId): Pair<Long, Long> {
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
