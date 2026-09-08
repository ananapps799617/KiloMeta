// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/entity/MaintenanceEntity.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Entidad Room para la tabla 'maintenance_records'.
// Almacena el historial de mantenimientos preventivos y correctivos del vehículo
// con seguimiento de odómetro, costos en COP y proyecciones de próximo cambio.
// ==============================================================================

package com.ananapps.kilometa.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ------------------------------------------------------------------------------
// BLOQUE 1: DEFINICIÓN DE ENTIDAD ROOM PARA MANTENIMIENTO
// ------------------------------------------------------------------------------
@Entity(
    tableName = "maintenance_records",
    indices = [
        Index(value = ["vehicle_id"]),
        Index(value = ["odometer"]),
        Index(value = ["timestamp"])
    ]
)
data class MaintenanceEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "vehicle_id")
    val vehicleId: String,

    @ColumnInfo(name = "maintenance_type")
    val maintenanceType: String,

    @ColumnInfo(name = "odometer")
    val odometer: Int,

    @ColumnInfo(name = "cost_cop")
    val costCop: Double,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "next_due_odometer")
    val nextDueOdometer: Int? = null,

    @ColumnInfo(name = "next_due_date_millis")
    val nextDueDateMillis: Long? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)
