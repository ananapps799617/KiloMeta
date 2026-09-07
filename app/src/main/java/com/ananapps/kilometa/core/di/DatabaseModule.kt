// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/core/di/DatabaseModule.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Módulo Dagger Hilt para la provisión de la base de datos Room SQLite y sus DAOs.
// Confinado al almacenamiento privado de la aplicación (sandbox) y preparado
// para migración a SQLCipher en fases comerciales posteriores.
// ==============================================================================

package com.ananapps.kilometa.core.di

import android.content.Context
import androidx.room.Room
import com.ananapps.kilometa.data.local.KiloMetaDatabase
import com.ananapps.kilometa.data.local.dao.ExpenseDao
import com.ananapps.kilometa.data.local.dao.FuelingDao
import com.ananapps.kilometa.data.local.dao.ShiftDao
import com.ananapps.kilometa.data.local.dao.TripDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: MÓDULO HILT DE PROVISIÓN DE PERSISTENCIA
// ------------------------------------------------------------------------------
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKiloMetaDatabase(
        @ApplicationContext context: Context
    ): KiloMetaDatabase {
        return Room.databaseBuilder(
            context,
            KiloMetaDatabase::class.java,
            KiloMetaDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: PROVISIÓN INDIVIDUAL DE DATA ACCESS OBJECTS (DAOS)
    // --------------------------------------------------------------------------
    @Provides
    fun provideShiftDao(database: KiloMetaDatabase): ShiftDao {
        return database.shiftDao()
    }

    @Provides
    fun provideTripDao(database: KiloMetaDatabase): TripDao {
        return database.tripDao()
    }

    @Provides
    fun provideFuelingDao(database: KiloMetaDatabase): FuelingDao {
        return database.fuelingDao()
    }

    @Provides
    fun provideExpenseDao(database: KiloMetaDatabase): ExpenseDao {
        return database.expenseDao()
    }
}
