// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/entity/TripEntity.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Entidad Room para la tabla 'trips'. Modela servicios realizados en plataformas.
// Configura clave foránea referenciando 'shifts(id)' con borrado en cascada (CASCADE)
// e indexación obligatoria en 'shift_id' para prevenir escaneos de tabla completa.
// ==============================================================================

package com.ananapps.kilometa.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ------------------------------------------------------------------------------
// BLOQUE 1: DEFINICIÓN DE TABLA, CLAVES FORÁNEAS E ÍNDICES
// ------------------------------------------------------------------------------
@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = ShiftEntity::class,
            parentColumns = ["id"],
            childColumns = ["shift_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["shift_id"])
    ]
)
data class TripEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "shift_id")
    val shiftId: String,

    @ColumnInfo(name = "platform_name")
    val platformName: String,

    @ColumnInfo(name = "gross_amount")
    val grossAmount: Double,

    @ColumnInfo(name = "commission_amount")
    val commissionAmount: Double,

    @ColumnInfo(name = "net_amount")
    val netAmount: Double,

    @ColumnInfo(name = "payment_method")
    val paymentMethod: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)
