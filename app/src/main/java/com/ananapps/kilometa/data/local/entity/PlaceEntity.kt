// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/entity/PlaceEntity.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Entidad Room para la tabla 'places' (lugares favoritos, gasolineras y talleres).
// Indexada por categoría para filtros rápidos en cabina.
// ==============================================================================

package com.ananapps.kilometa.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "places",
    indices = [
        Index(value = ["category"])
    ]
)
data class PlaceEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,

    @ColumnInfo(name = "longitude")
    val longitude: Double? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "gnv_price_per_m3")
    val gnvPricePerM3: Double? = null,

    @ColumnInfo(name = "gasoline_price_per_gal")
    val gasolinePricePerGal: Double? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
