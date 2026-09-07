// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/worker/WorkManagerSyncScheduler.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación de SyncScheduler mediante Jetpack WorkManager.
// Configura restricciones de batería y red (Híbrido por defecto: Móviles + Wi-Fi).
// ==============================================================================

package com.ananapps.kilometa.data.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ananapps.kilometa.domain.model.SyncPreferences
import com.ananapps.kilometa.domain.repository.SyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: IMPLEMENTACIÓN DE SCHEDULER BASADA EN WORKMANAGER
// ------------------------------------------------------------------------------
/**
 * Orquestador de tareas en segundo plano que programa workers respetando
 * las directrices energéticas del sistema y las preferencias del conductor.
 */
@Singleton
class WorkManagerSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : SyncScheduler {

    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(context)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: PROGRAMACIÓN PERIÓDICA CON CONSTRAINTS HÍBRIDOS
    // --------------------------------------------------------------------------
    override fun schedulePeriodicSync(preferences: SyncPreferences) {
        // Determinación de la restricción de red:
        // Si requireWifiOnly == true -> UNMETERED (solo Wi-Fi sin costo)
        // Por defecto -> CONNECTED (Híbrido: Datos Móviles + Wi-Fi)
        val networkType = if (preferences.requireWifiOnly) {
            NetworkType.UNMETERED
        } else {
            NetworkType.CONNECTED
        }

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(networkType)
            .build()

        // El intervalo mínimo de WorkManager para trabajos periódicos es 15 minutos
        val intervalMinutes = preferences.syncIntervalMinutes.coerceAtLeast(15L)

        val periodicWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = intervalMinutes,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                INITIAL_BACKOFF_SECONDS,
                TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: DISPARO EXPEDITO (ONE-TIME SYNC)
    // --------------------------------------------------------------------------
    override fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                INITIAL_BACKOFF_SECONDS,
                TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            SyncWorker.WORK_NAME_IMMEDIATE,
            ExistingWorkPolicy.KEEP,
            immediateWorkRequest
        )
    }

    // --------------------------------------------------------------------------
    // BLOQUE 4: CANCELACIÓN DE TAREAS PERIÓDICAS
    // --------------------------------------------------------------------------
    override fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME_PERIODIC)
    }

    companion object {
        private const val INITIAL_BACKOFF_SECONDS = 30L
    }
}
