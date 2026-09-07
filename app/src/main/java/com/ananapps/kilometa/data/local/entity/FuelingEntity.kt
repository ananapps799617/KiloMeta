// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/entity/FuelingEntity.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Entidad Room para la tabla 'fuelings' (recargas de combustible bi-modal GNV / Gasolina).
// Implementa integridad referencial con borrado en cascada e indexación en 'shift_id'.
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
    tableName = "fuelings",
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
data class FuelingEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "shift_id")
    val shiftId: String,

    @ColumnInfo(name = "fuel_type")
    val fuelType: String,

    @ColumnInfo(name = "amount_paid")
    val amountPaid: Double,

    @ColumnInfo(name = "volume")
    val volume: Double,

    @ColumnInfo(name = "odometer")
    val odometer: Int,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)
