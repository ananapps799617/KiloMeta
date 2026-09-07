// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/worker/WorkManagerQuotaScheduler.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación de VehicularNotificationScheduler con Jetpack WorkManager.
// Programa tareas temporizadas con delay exacto hacia el próximo martes a las
// 18:00 hrs (alerta preventiva) y 23:59:59 hrs (corte final de ciclo).
// ==============================================================================

package com.ananapps.kilometa.data.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ananapps.kilometa.domain.repository.VehicularNotificationScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.DayOfWeek
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: PROGRAMADOR DE CORTE SEMANAL CON WORKMANAGER
// ------------------------------------------------------------------------------
@Singleton
class WorkManagerQuotaScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : VehicularNotificationScheduler {

    companion object {
        const val WORK_NAME_WARNING = "kilometa_quota_warning"
        const val WORK_NAME_FINAL = "kilometa_quota_final"
    }

    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(context)
    }

    private val bogotaZone: ZoneId = ZoneId.of("America/Bogota")

    // --------------------------------------------------------------------------
    // BLOQUE 2: PROGRAMACIÓN DE ALERTAS DE CORTE
    // --------------------------------------------------------------------------
    override fun scheduleWeeklyCutoffAlarms() {
        val now = ZonedDateTime.now(bogotaZone)

        // 1. Alerta preventiva: Martes a las 18:00:00
        val warningDelayMillis = calculateDelayToTuesday(
            targetHour = 18,
            targetMinute = 0,
            targetSecond = 0,
            now = now
        )

        val warningRequest = OneTimeWorkRequestBuilder<WeeklyCutoffWorker>()
            .setInitialDelay(warningDelayMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(WeeklyCutoffWorker.KEY_IS_FINAL to false))
            .addTag(WeeklyCutoffWorker.TAG_WEEKLY_CUTOFF)
            .build()

        workManager.enqueueUniqueWork(
            WORK_NAME_WARNING,
            ExistingWorkPolicy.REPLACE,
            warningRequest
        )

        // 2. Alerta de cierre final: Martes a las 23:59:59
        val finalDelayMillis = calculateDelayToTuesday(
            targetHour = 23,
            targetMinute = 59,
            targetSecond = 59,
            now = now
        )

        val finalRequest = OneTimeWorkRequestBuilder<WeeklyCutoffWorker>()
            .setInitialDelay(finalDelayMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(WeeklyCutoffWorker.KEY_IS_FINAL to true))
            .addTag(WeeklyCutoffWorker.TAG_WEEKLY_CUTOFF)
            .build()

        workManager.enqueueUniqueWork(
            WORK_NAME_FINAL,
            ExistingWorkPolicy.REPLACE,
            finalRequest
        )
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: CANCELACIÓN DE ALERTAS
    // --------------------------------------------------------------------------
    override fun cancelScheduledAlarms() {
        workManager.cancelUniqueWork(WORK_NAME_WARNING)
        workManager.cancelUniqueWork(WORK_NAME_FINAL)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 4: CÁLCULO TEMPORAL DE DEMORA HACIA EL PRÓXIMO MARTES
    // --------------------------------------------------------------------------
    fun calculateDelayToTuesday(
        targetHour: Int,
        targetMinute: Int,
        targetSecond: Int,
        now: ZonedDateTime
    ): Long {
        var target = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY))
            .withHour(targetHour)
            .withMinute(targetMinute)
            .withSecond(targetSecond)
            .withNano(0)

        if (!target.isAfter(now)) {
            target = target.plusWeeks(1)
        }

        return Duration.between(now, target).toMillis().coerceAtLeast(1000L)
    }
}
