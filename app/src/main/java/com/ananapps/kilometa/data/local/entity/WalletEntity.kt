package com.ananapps.kilometa.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad relacional SQLite para la gestión de bolsillos y billeteras soberanas (Orden #054).
 * Regla inquebrantable: Inmutabilidad profunda y tipado estricto.
 */
@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "color_hex")
    val colorHex: String,

    @ColumnInfo(name = "is_system_protected", defaultValue = "0")
    val isSystemProtected: Boolean = false,

    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true,

    @ColumnInfo(name = "balance", defaultValue = "0.0")
    val balance: Double = 0.0
)
