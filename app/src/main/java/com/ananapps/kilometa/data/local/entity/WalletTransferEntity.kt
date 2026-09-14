// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/entity/WalletTransferEntity.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Entidad Room SQLite para registrar transferencias inter-billeteras (Orden #033).
// Regla inquebrantable: Modularidad estricta < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wallet_transfers",
    indices = [
        Index(value = ["timestamp"])
    ]
)
data class WalletTransferEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "source_wallet")
    val sourceWallet: String,

    @ColumnInfo(name = "destination_wallet")
    val destinationWallet: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "transfer_type")
    val transferType: String,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
