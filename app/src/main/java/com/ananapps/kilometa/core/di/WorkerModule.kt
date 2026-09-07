// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/core/di/WorkerModule.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Módulo de Inyección de Dependencias Hilt para abstraer el programador de tareas
// de fondo (SyncScheduler) en el componente Singleton.
// ==============================================================================

package com.ananapps.kilometa.core.di

import com.ananapps.kilometa.data.worker.WorkManagerSyncScheduler
import com.ananapps.kilometa.domain.repository.SyncScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: VINCULACIÓN DEL CONTRATO DE PROGRAMADOR DE TAREAS
// ------------------------------------------------------------------------------
/**
 * Provee la implementación de [SyncScheduler] respaldada por WorkManager.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {

    @Binds
    @Singleton
    abstract fun bindSyncScheduler(
        impl: WorkManagerSyncScheduler
    ): SyncScheduler
}
