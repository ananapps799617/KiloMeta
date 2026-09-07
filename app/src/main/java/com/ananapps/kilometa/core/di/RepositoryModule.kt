// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/core/di/RepositoryModule.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Módulo Dagger Hilt para enlazar las interfaces del dominio con sus implementaciones
// concretas en la capa de datos (Repository Pattern y Clean Architecture).
// ==============================================================================

package com.ananapps.kilometa.core.di

import com.ananapps.kilometa.data.repository.ExpenseRepositoryImpl
import com.ananapps.kilometa.data.repository.FuelingRepositoryImpl
import com.ananapps.kilometa.data.repository.ShiftRepositoryImpl
import com.ananapps.kilometa.data.repository.TripRepositoryImpl
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import com.ananapps.kilometa.domain.repository.FuelingRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.repository.TripRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: ENLACES DE DEPENDENCIAS DE REPOSITORIOS
// ------------------------------------------------------------------------------
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindShiftRepository(
        impl: ShiftRepositoryImpl
    ): ShiftRepository

    @Binds
    @Singleton
    abstract fun bindTripRepository(
        impl: TripRepositoryImpl
    ): TripRepository

    @Binds
    @Singleton
    abstract fun bindFuelingRepository(
        impl: FuelingRepositoryImpl
    ): FuelingRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        impl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindDriverProfileRepository(
        impl: com.ananapps.kilometa.data.repository.DriverProfileRepositoryImpl
    ): com.ananapps.kilometa.domain.repository.DriverProfileRepository
}
