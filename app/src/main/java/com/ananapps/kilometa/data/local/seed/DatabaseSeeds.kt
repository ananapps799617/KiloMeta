package com.ananapps.kilometa.data.local.seed

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Semillas deterministas e idempotentes para la base de datos Room SQLite (Orden #055).
 * Garantiza la siembra de plataformas y bolsillos protegidos del sistema en instalaciones limpias
 * y aperturas, mitigando la patología de tablas vacías (MND-FB-01).
 */
object DatabaseSeeds {

    /**
     * Puebla las plataformas y bolsillos base de forma 100% idempotente (INSERT OR IGNORE).
     * No bloquea el hilo principal y opera 100% local sin dependencias remotas.
     */
    fun populateSeeds(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()

        // 1. Semillas de plataformas vehiculares oficiales
        db.execSQL(
            """
            INSERT OR IGNORE INTO `platforms` (
                `id`, `name`, `monogram`, `icon_name`, `brand_color_hex`,
                `default_commission_pct`, `disbursement_day`, `is_custom`, `is_active`, `created_at`
            ) VALUES
            ('uber', 'Uber', 'UB', '🚘', '#1C1C1E', 28.0, 'Lunes', 0, 1, $now),
            ('didi', 'DiDi', 'DD', '🚘', '#FF7A00', 21.5, 'Martes', 0, 1, $now),
            ('indrive', 'inDrive', 'IN', '🚘', '#B4F309', 10.0, 'Miércoles', 0, 1, $now),
            ('private', 'Particular', 'PR', '🚘', '#7B1FA2', 0.0, 'Inmediato', 0, 1, $now)
            """.trimIndent()
        )

        // 2. Semillas de bolsillos base soberanos
        db.execSQL(
            """
            INSERT OR IGNORE INTO `wallets` (
                `id`, `name`, `category`, `icon_name`, `color_hex`,
                `is_system_protected`, `is_active`, `balance`
            ) VALUES
            ('cash_physical', 'Efectivo en Mano', 'CASH', '💵', '#00E676', 1, 1, 0.0),
            ('digital_wallet', 'Nequi / Daviplata', 'DIGITAL', '🟣', '#2979FF', 1, 1, 0.0),
            ('uber', 'Billetera Uber', 'APP_PLATFORM', '🚘', '#1C1C1E', 0, 1, 0.0),
            ('didi', 'Billetera DiDi', 'APP_PLATFORM', '🚘', '#FF7A00', 0, 1, 0.0),
            ('car_quota', 'Cuota Carro', 'SAVINGS', '🚘', '#2979FF', 0, 1, 0.0)
            """.trimIndent()
        )
    }

    /**
     * Retorna el Callback de Room que ejecuta la siembra en onCreate y onOpen de forma idempotente.
     */
    fun getRoomCallback(): RoomDatabase.Callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            populateSeeds(db)
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            populateSeeds(db)
        }
    }
}
