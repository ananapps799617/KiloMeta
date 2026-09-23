// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/entity/TripEntity.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Entidad Room para la tabla 'trips'. Modela servicios realizados en plataformas.
// Incorpora columna 'payment_destination' con valor por defecto CASH_IN_HAND.
// ==============================================================================

package com.ananapps.kilometa.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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

    @ColumnInfo(name = "payment_destination", defaultValue = "CASH_IN_HAND")
    val paymentDestination: String = "CASH_IN_HAND",

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "start_odometer", defaultValue = "NULL")
    val startOdometer: Int? = null,

    @ColumnInfo(name = "end_odometer", defaultValue = "NULL")
    val endOdometer: Int? = null,

    @ColumnInfo(name = "platform_promo_discount", defaultValue = "0.0")
    val platformPromoDiscount: Double = 0.0,

    @ColumnInfo(name = "distance_km", defaultValue = "NULL")
    val distanceKm: Double? = null,

    @ColumnInfo(name = "duration_minutes", defaultValue = "NULL")
    val durationMinutes: Int? = null
)
