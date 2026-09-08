// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/KiloMetaDatabase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Base de datos relacional local Room SQLite para KiloMeta.
// Configura las 4 entidades fundamentales, versión 1, esquemas exportados para
// control de versiones DDL y conversores tipados para enumeraciones de dominio.
// ==============================================================================

package com.ananapps.kilometa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ananapps.kilometa.data.local.converter.Converters
import com.ananapps.kilometa.data.local.dao.ExpenseDao
import com.ananapps.kilometa.data.local.dao.FuelingDao
import com.ananapps.kilometa.data.local.dao.MaintenanceDao
import com.ananapps.kilometa.data.local.dao.ShiftDao
import com.ananapps.kilometa.data.local.dao.TripDao
import com.ananapps.kilometa.data.local.entity.ExpenseEntity
import com.ananapps.kilometa.data.local.entity.FuelingEntity
import com.ananapps.kilometa.data.local.entity.MaintenanceEntity
import com.ananapps.kilometa.data.local.entity.ShiftEntity
import com.ananapps.kilometa.data.local.entity.TripEntity

// ------------------------------------------------------------------------------
// BLOQUE 1: CONFIGURACIÓN DE LA BASE DE DATOS ROOM
// ------------------------------------------------------------------------------
@Database(
    entities = [
        ShiftEntity::class,
        TripEntity::class,
        FuelingEntity::class,
        ExpenseEntity::class,
        MaintenanceEntity::class
    ],
    version = 3,
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

    companion object {
        const val DATABASE_NAME: String = "kilometa.db"
    }
}
