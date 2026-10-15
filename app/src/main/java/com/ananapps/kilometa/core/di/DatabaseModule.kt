// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/core/di/DatabaseModule.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Módulo de inyección de dependencias para persistencia local Room SQLite v11.
// Migraciones desacopladas en DatabaseMigrations.kt y semillas idempotentes con DatabaseSeeds.kt.
// Regla inquebrantable: Modularidad estricta < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.core.di

import android.content.Context
import androidx.room.Room
import com.ananapps.kilometa.data.local.KiloMetaDatabase
import com.ananapps.kilometa.data.local.dao.ExpenseDao
import com.ananapps.kilometa.data.local.dao.FuelingDao
import com.ananapps.kilometa.data.local.dao.MaintenanceDao
import com.ananapps.kilometa.data.local.dao.PendingDeletionDao
import com.ananapps.kilometa.data.local.dao.PlaceDao
import com.ananapps.kilometa.data.local.dao.PlatformDao
import com.ananapps.kilometa.data.local.dao.ShiftDao
import com.ananapps.kilometa.data.local.dao.TripDao
import com.ananapps.kilometa.data.local.dao.WalletDao
import com.ananapps.kilometa.data.local.dao.WalletTransferDao
import com.ananapps.kilometa.data.local.migration.DatabaseMigrations
import com.ananapps.kilometa.data.local.seed.DatabaseSeeds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // --------------------------------------------------------------------------
    // BLOQUE 1: PROVISIÓN DEL SINGLETON DE BASE DE DATOS ROOM
    // --------------------------------------------------------------------------
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
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            .addCallback(DatabaseSeeds.getRoomCallback())
            .build()
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: PROVISIÓN INDIVIDUAL DE DATA ACCESS OBJECTS (DAOS)
    // --------------------------------------------------------------------------
    @Provides
    fun provideShiftDao(database: KiloMetaDatabase): ShiftDao = database.shiftDao()

    @Provides
    fun provideTripDao(database: KiloMetaDatabase): TripDao = database.tripDao()

    @Provides
    fun provideFuelingDao(database: KiloMetaDatabase): FuelingDao = database.fuelingDao()

    @Provides
    fun provideExpenseDao(database: KiloMetaDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun provideMaintenanceDao(database: KiloMetaDatabase): MaintenanceDao = database.maintenanceDao()

    @Provides
    fun providePlaceDao(database: KiloMetaDatabase): PlaceDao = database.placeDao()

    @Provides
    fun providePendingDeletionDao(database: KiloMetaDatabase): PendingDeletionDao = database.pendingDeletionDao()

    @Provides
    fun provideWalletTransferDao(database: KiloMetaDatabase): WalletTransferDao = database.walletTransferDao()

    @Provides
    fun providePlatformDao(database: KiloMetaDatabase): PlatformDao = database.platformDao()

    @Provides
    fun provideWalletDao(database: KiloMetaDatabase): WalletDao = database.walletDao()
}
