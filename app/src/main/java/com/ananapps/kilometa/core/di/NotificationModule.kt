// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/core/di/NotificationModule.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Módulo de Inyección de Dependencias Hilt para notificaciones y alertas vehiculares.
// Vincula las implementaciones Android con los contratos de la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.core.di

import com.ananapps.kilometa.data.notification.AndroidVehicularNotifier
import com.ananapps.kilometa.data.worker.WorkManagerQuotaScheduler
import com.ananapps.kilometa.domain.repository.VehicularNotificationNotifier
import com.ananapps.kilometa.domain.repository.VehicularNotificationScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: MÓDULO HILT DE NOTIFICACIONES VEHICULARES
// ------------------------------------------------------------------------------
@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindVehicularNotificationNotifier(
        impl: AndroidVehicularNotifier
    ): VehicularNotificationNotifier

    @Binds
    @Singleton
    abstract fun bindVehicularNotificationScheduler(
        impl: WorkManagerQuotaScheduler
    ): VehicularNotificationScheduler
}
