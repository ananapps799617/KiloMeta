package com.ananapps.kilometa.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Catálogo centralizado y desacoplado de migraciones Room SQLite (Ecosistema AnAnApps v2.0).
 * Garantiza modularidad estricta (< 280 LOC) y determinismo en el esquema relacional.
 */
object DatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE trips ADD COLUMN payment_destination TEXT NOT NULL DEFAULT 'CASH_IN_HAND'")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
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

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
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

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `trips` ADD COLUMN `start_odometer` INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE `trips` ADD COLUMN `end_odometer` INTEGER DEFAULT NULL")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
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

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
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

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `places` ADD COLUMN `gnv_price_per_m3` REAL DEFAULT NULL")
            db.execSQL("ALTER TABLE `places` ADD COLUMN `gasoline_price_per_gal` REAL DEFAULT NULL")
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `trips` ADD COLUMN `distance_km` REAL DEFAULT NULL")
            db.execSQL("ALTER TABLE `trips` ADD COLUMN `duration_minutes` INTEGER DEFAULT NULL")
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Crear tabla platforms
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `platforms` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `monogram` TEXT NOT NULL,
                    `brand_color_hex` TEXT NOT NULL,
                    `default_commission_pct` REAL NOT NULL,
                    `disbursement_day` TEXT NOT NULL,
                    `is_custom` INTEGER NOT NULL DEFAULT 0,
                    `is_active` INTEGER NOT NULL DEFAULT 1,
                    `created_at` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )

            // 2. Semillas iniciales de plataformas históricas
            val now = System.currentTimeMillis()
            db.execSQL("INSERT OR IGNORE INTO `platforms` VALUES ('uber', 'Uber', 'UB', '#000000', 28.0, 'Lunes', 0, 1, $now)")
            db.execSQL("INSERT OR IGNORE INTO `platforms` VALUES ('didi', 'DiDi', 'DD', '#FF5C00', 21.5, 'Martes', 0, 1, $now)")
            db.execSQL("INSERT OR IGNORE INTO `platforms` VALUES ('indrive', 'inDrive', 'IN', '#B2D95D', 10.0, 'Miércoles', 0, 1, $now)")
            db.execSQL("INSERT OR IGNORE INTO `platforms` VALUES ('private', 'Particular', 'PR', '#3F51B5', 0.0, 'Inmediato', 0, 1, $now)")

            // 3. Crear tabla wallets
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `wallets` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `color_hex` TEXT NOT NULL,
                    `is_system_protected` INTEGER NOT NULL DEFAULT 0,
                    `is_active` INTEGER NOT NULL DEFAULT 1,
                    `balance` REAL NOT NULL DEFAULT 0.0,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )

            // 4. Semillas iniciales de bolsillos del sistema
            db.execSQL("INSERT OR IGNORE INTO `wallets` VALUES ('cash_physical', 'Efectivo en Mano', 'CASH', '#00E676', 1, 1, 0.0)")
            db.execSQL("INSERT OR IGNORE INTO `wallets` VALUES ('digital_wallet', 'Nequi / Daviplata', 'DIGITAL', '#BA68C8', 1, 1, 0.0)")
            db.execSQL("INSERT OR IGNORE INTO `wallets` VALUES ('uber', 'Billetera Uber', 'APP_PLATFORM', '#000000', 0, 1, 0.0)")
            db.execSQL("INSERT OR IGNORE INTO `wallets` VALUES ('didi', 'Billetera DiDi', 'APP_PLATFORM', '#FF5C00', 0, 1, 0.0)")
            db.execSQL("INSERT OR IGNORE INTO `wallets` VALUES ('car_quota', 'Cuota Carro', 'SAVINGS', '#2979FF', 0, 1, 0.0)")

            // 5. Inyección preventiva de lápidas en pending_deletions (MND-02 anti-zombie Firestore)
            db.execSQL(
                """
                INSERT OR IGNORE INTO `pending_deletions` (`entity_id`, `entity_type`, `deleted_at`)
                SELECT `id`, 'WALLET_TRANSFER', strftime('%s', 'now') * 1000
                FROM `wallet_transfers`
                WHERE `id` LIKE 'auto_quota_%'
                """.trimIndent()
            )

            // 6. Purga física SQL local de transferencias automáticas fantasma
            db.execSQL("DELETE FROM `wallet_transfers` WHERE `id` LIKE 'auto_quota_%'")
        }
    }

    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9,
        MIGRATION_9_10
    )
}
