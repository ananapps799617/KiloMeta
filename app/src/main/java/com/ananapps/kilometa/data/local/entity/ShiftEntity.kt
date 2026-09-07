// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/entity/ShiftEntity.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Entidad Room para la tabla 'shifts' (unidad fundamental de jornada laboral).
// Almacena marcas de tiempo, lecturas de odómetro, meta diaria y estado.
// ==============================================================================

package com.ananapps.kilometa.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// ------------------------------------------------------------------------------
// BLOQUE 1: DEFINICIÓN DE TABLA Y COLUMNAS
// ------------------------------------------------------------------------------
/**
 * Entidad relacional principal de la jornada de trabajo.
 */
@Entity(tableName = "shifts")
data class ShiftEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "start_time")
    val startTime: Long,

    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,

    @ColumnInfo(name = "start_odometer")
    val startOdometer: Int,

    @ColumnInfo(name = "end_odometer")
    val endOdometer: Int? = null,

    @ColumnInfo(name = "daily_quota_target")
    val dailyQuotaTarget: Double,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
