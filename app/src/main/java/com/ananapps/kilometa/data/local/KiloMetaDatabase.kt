// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/KiloMetaDatabase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Base de datos relacional local Room SQLite para KiloMeta v10.
// Incorpora plataformas de transporte y bolsillos/billeteras dinámicas (Orden #054).
// ==============================================================================

package com.ananapps.kilometa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ananapps.kilometa.data.local.converter.Converters
import com.ananapps.kilometa.data.local.dao.ExpenseDao
import com.ananapps.kilometa.data.local.dao.FuelingDao
import com.ananapps.kilometa.data.local.dao.MaintenanceDao
import com.ananapps.kilometa.data.local.dao.PlaceDao
import com.ananapps.kilometa.data.local.dao.PlatformDao
import com.ananapps.kilometa.data.local.dao.ShiftDao
import com.ananapps.kilometa.data.local.dao.TripDao
import com.ananapps.kilometa.data.local.dao.WalletDao
import com.ananapps.kilometa.data.local.entity.ExpenseEntity
import com.ananapps.kilometa.data.local.entity.FuelingEntity
import com.ananapps.kilometa.data.local.entity.MaintenanceEntity
import com.ananapps.kilometa.data.local.entity.PendingDeletionEntity
import com.ananapps.kilometa.data.local.entity.PlaceEntity
import com.ananapps.kilometa.data.local.entity.PlatformEntity
import com.ananapps.kilometa.data.local.entity.ShiftEntity
import com.ananapps.kilometa.data.local.entity.TripEntity
import com.ananapps.kilometa.data.local.entity.WalletEntity
import com.ananapps.kilometa.data.local.entity.WalletTransferEntity

// ------------------------------------------------------------------------------
// BLOQUE 1: CONFIGURACIÓN DE LA BASE DE DATOS ROOM
// ------------------------------------------------------------------------------
@Database(
    entities = [
        ShiftEntity::class,
        TripEntity::class,
        FuelingEntity::class,
        ExpenseEntity::class,
        MaintenanceEntity::class,
        PlaceEntity::class,
        PendingDeletionEntity::class,
        WalletTransferEntity::class,
        PlatformEntity::class,
        WalletEntity::class
    ],
    version = 10,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class KiloMetaDatabase : RoomDatabase() {

    // --------------------------------------------------------------------------
    // BLOQUE 2: MÉTODOS ABSTRACTOS PARA ACCESO A DAOS
    // --------------------------------------------------------------------------
    abstract fun shiftDao(): ShiftDao
    abstract fun tripDao(): TripDao
    abstract fun fuelingDao(): FuelingDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun placeDao(): PlaceDao
    abstract fun pendingDeletionDao(): com.ananapps.kilometa.data.local.dao.PendingDeletionDao
    abstract fun walletTransferDao(): com.ananapps.kilometa.data.local.dao.WalletTransferDao
    abstract fun platformDao(): PlatformDao
    abstract fun walletDao(): WalletDao

    companion object {
        const val DATABASE_NAME: String = "kilometa.db"
    }
}
