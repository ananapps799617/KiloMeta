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

    val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE trips ADD COLUMN payment_destination TEXT NOT NULL DEFAULT 'CASH_IN_HAND'")
        }
    }

    val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `maintenance_records` (
                    `id` TEXT NOT NULL,
                    `vehicle_id` TEXT NOT NULL,
                    `maintenance_type` TEXT NOT NULL,
                    `odometer` INTEGER NOT NULL,
                    `cost_cop` REAL NOT NULL,
                    `notes` TEXT,
                    `next_due_odometer` INTEGER,
                    `next_due_date_millis` INTEGER,
                    `timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_maintenance_records_vehicle_id` ON `maintenance_records` (`vehicle_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_maintenance_records_odometer` ON `maintenance_records` (`odometer`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_maintenance_records_timestamp` ON `maintenance_records` (`timestamp`)")
        }
    }

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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .fallbackToDestructiveMigration()
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
    fun provideMaintenanceDao(
        database: KiloMetaDatabase
    ): com.ananapps.kilometa.data.local.dao.MaintenanceDao = database.maintenanceDao()
}
