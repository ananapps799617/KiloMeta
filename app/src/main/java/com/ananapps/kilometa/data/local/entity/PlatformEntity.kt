package com.ananapps.kilometa.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad relacional SQLite para el catálogo multi-país de plataformas de transporte (Orden #054 / #055).
 * Regla inquebrantable: Inmutabilidad profunda y tipado estricto.
 */
@Entity(tableName = "platforms")
data class PlatformEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "monogram")
    val monogram: String,

    @ColumnInfo(name = "icon_name", defaultValue = "🚘")
    val iconName: String = "🚘",

    @ColumnInfo(name = "brand_color_hex")
    val brandColorHex: String,

    @ColumnInfo(name = "default_commission_pct")
    val defaultCommissionPct: Double,

    @ColumnInfo(name = "disbursement_day")
    val disbursementDay: String,

    @ColumnInfo(name = "is_custom", defaultValue = "0")
    val isCustom: Boolean = false,

    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
