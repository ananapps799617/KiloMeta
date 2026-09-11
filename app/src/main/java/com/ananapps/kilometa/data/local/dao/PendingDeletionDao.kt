// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/dao/PendingDeletionDao.kt
// DESCRIPCIÓN PEDAGÓGICA:
// DAO para gestionar lápidas de registros pendientes de purga remota en la nube.
// ==============================================================================

package com.ananapps.kilometa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ananapps.kilometa.data.local.entity.PendingDeletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingDeletionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pendingDeletion: PendingDeletionEntity)

    @Query("SELECT * FROM pending_deletions ORDER BY deleted_at ASC")
    suspend fun getAllPendingDeletions(): List<PendingDeletionEntity>

    @Query("SELECT * FROM pending_deletions ORDER BY deleted_at ASC")
    fun observePendingDeletions(): Flow<List<PendingDeletionEntity>>

    @Query("SELECT entity_id FROM pending_deletions WHERE entity_type = :type")
    suspend fun getDeletedIdsByType(type: String): List<String>

    @Query("DELETE FROM pending_deletions WHERE entity_id = :entityId")
    suspend fun deleteByEntityId(entityId: String)

    @Query("DELETE FROM pending_deletions WHERE entity_id IN (:entityIds)")
    suspend fun deleteByEntityIds(entityIds: List<String>)

    @Query("DELETE FROM pending_deletions")
    suspend fun clearAll()
}
