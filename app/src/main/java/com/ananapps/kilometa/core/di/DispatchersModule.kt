// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/core/di/DispatchersModule.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Módulo de inyección de dependencias Hilt para proveer los CoroutineDispatchers.
// Desacopla la lógica de negocio de los despachadores estáticos de Kotlin Coroutines.
// ==============================================================================

package com.ananapps.kilometa.core.di

import com.ananapps.kilometa.core.dispatcher.AppDispatchers
import com.ananapps.kilometa.core.dispatcher.CoroutineDispatchers
import com.ananapps.kilometa.core.dispatcher.DefaultDispatcher
import com.ananapps.kilometa.core.dispatcher.IoDispatcher
import com.ananapps.kilometa.core.dispatcher.MainDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: DECLARACIÓN DEL MÓDULO HILT
// ------------------------------------------------------------------------------
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    // --------------------------------------------------------------------------
    // BLOQUE 2: PROVEEDORES INDIVIDUALES POR QUALIFIER
    // --------------------------------------------------------------------------
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    // --------------------------------------------------------------------------
    // BLOQUE 3: PROVEEDOR DEL CONTENEDOR AGRUPADO
    // --------------------------------------------------------------------------
    @Provides
    @Singleton
    fun provideCoroutineDispatchers(
        @IoDispatcher io: CoroutineDispatcher,
        @DefaultDispatcher default: CoroutineDispatcher,
        @MainDispatcher main: CoroutineDispatcher
    ): CoroutineDispatchers {
        return CoroutineDispatchers(
            io = io,
            default = default,
            main = main
        )
    }
}
