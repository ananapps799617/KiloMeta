// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/core/di/FirebaseModule.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Módulo Dagger Hilt para enlazar los contratos de autenticación y sincronización
// con sus implementaciones para la nube en la capa de datos.
// ==============================================================================

package com.ananapps.kilometa.core.di

import com.ananapps.kilometa.data.repository.AuthRepositoryImpl
import com.ananapps.kilometa.data.repository.SyncRepositoryImpl
import com.ananapps.kilometa.domain.repository.AuthRepository
import com.ananapps.kilometa.domain.repository.SyncRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: ENLACES DE DEPENDENCIAS PARA NUBE Y DISASTER RECOVERY
// ------------------------------------------------------------------------------
@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        impl: SyncRepositoryImpl
    ): SyncRepository
}
