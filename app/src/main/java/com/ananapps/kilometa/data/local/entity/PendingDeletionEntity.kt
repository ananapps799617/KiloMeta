// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/entity/PendingDeletionEntity.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Entidad Room para registrar lápidas (tombstones) de registros eliminados localmente
// que deben purgarse en Cloud Firestore para prevenir su resurrección zombi.
// ==============================================================================

package com.ananapps.kilometa.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_deletions")
data class PendingDeletionEntity(
    @PrimaryKey
    @ColumnInfo(name = "entity_id")
    val entityId: String,

    @ColumnInfo(name = "entity_type")
    val entityType: String, // "TRIP", "FUELING", "EXPENSE", "SHIFT"

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long = System.currentTimeMillis()
)
