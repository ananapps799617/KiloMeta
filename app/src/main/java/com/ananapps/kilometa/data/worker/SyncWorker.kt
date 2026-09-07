// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/worker/SyncWorker.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Worker ejecutor de sincronización en segundo plano con WorkManager y Dagger Hilt.
// Gestiona reintentos exponenciales con límite de reintentos para resiliencia en ruta.
// ==============================================================================

package com.ananapps.kilometa.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ananapps.kilometa.domain.usecase.SyncDataUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

// ------------------------------------------------------------------------------
// BLOQUE 1: TRABAJADOR CORRUTINA PARA SINCRONIZACIÓN EN SEGUNDO PLANO
// ------------------------------------------------------------------------------
/**
 * Tarea de fondo orquestada por WorkManager. Ejecuta el caso de uso [SyncDataUseCase]
 * en un hilo en segundo plano con reintento ante fallos de conexión.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncDataUseCase: SyncDataUseCase
) : CoroutineWorker(appContext, workerParams) {

    // --------------------------------------------------------------------------
    // BLOQUE 2: EJECUCIÓN DEL TRABAJO CON POLÍTICA DE RESILIENCIA
    // --------------------------------------------------------------------------
    override suspend fun doWork(): Result {
        return try {
            val syncResult = syncDataUseCase()
            if (syncResult.isSuccess) {
                Result.success()
            } else {
                // Si falla por conectividad u otra causa y no hemos superado 3 intentos, reintentar
                if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME_PERIODIC = "KiloMetaPeriodicSyncWork"
        const val WORK_NAME_IMMEDIATE = "KiloMetaImmediateSyncWork"
        private const val MAX_RETRY_ATTEMPTS = 3
    }
}
