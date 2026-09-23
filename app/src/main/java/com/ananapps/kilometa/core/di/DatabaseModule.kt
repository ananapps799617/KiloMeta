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

    val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `places` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `address` TEXT NOT NULL,
                    `latitude` REAL,
                    `longitude` REAL,
                    `notes` TEXT,
                    `created_at` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_places_category` ON `places` (`category`)")
        }
    }

    val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `trips` ADD COLUMN `start_odometer` INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE `trips` ADD COLUMN `end_odometer` INTEGER DEFAULT NULL")
        }
    }

    val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `pending_deletions` (
                    `entity_id` TEXT NOT NULL,
                    `entity_type` TEXT NOT NULL,
                    `deleted_at` INTEGER NOT NULL,
                    PRIMARY KEY(`entity_id`)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `wallet_transfers` (
                    `id` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    `source_wallet` TEXT NOT NULL,
                    `destination_wallet` TEXT NOT NULL,
                    `amount` REAL NOT NULL,
                    `transfer_type` TEXT NOT NULL,
                    `notes` TEXT,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_wallet_transfers_timestamp` ON `wallet_transfers` (`timestamp`)")
            db.execSQL("ALTER TABLE `fuelings` ADD COLUMN `payment_source` TEXT NOT NULL DEFAULT 'CASH_PHYSICAL'")
            db.execSQL("ALTER TABLE `expenses` ADD COLUMN `payment_source` TEXT NOT NULL DEFAULT 'CASH_PHYSICAL'")
            db.execSQL("ALTER TABLE `trips` ADD COLUMN `platform_promo_discount` REAL NOT NULL DEFAULT 0.0")
        }
    }

    val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `places` ADD COLUMN `gnv_price_per_m3` REAL DEFAULT NULL")
            db.execSQL("ALTER TABLE `places` ADD COLUMN `gasoline_price_per_gal` REAL DEFAULT NULL")
        }
    }

    val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `trips` ADD COLUMN `distance_km` REAL DEFAULT NULL")
            db.execSQL("ALTER TABLE `trips` ADD COLUMN `duration_minutes` INTEGER DEFAULT NULL")
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
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

    @Provides
    fun providePlaceDao(
        database: KiloMetaDatabase
    ): com.ananapps.kilometa.data.local.dao.PlaceDao = database.placeDao()

    @Provides
    fun providePendingDeletionDao(
        database: KiloMetaDatabase
    ): com.ananapps.kilometa.data.local.dao.PendingDeletionDao = database.pendingDeletionDao()

    @Provides
    fun provideWalletTransferDao(
        database: KiloMetaDatabase
    ): com.ananapps.kilometa.data.local.dao.WalletTransferDao = database.walletTransferDao()
}
