// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/dao/WalletTransferDao.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Data Access Object para operaciones reactivas sobre transferencias inter-bolsillos (Orden #033).
// Regla inquebrantable: Modularidad estricta < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.data.local.dao

import androidx.room.*
import com.ananapps.kilometa.data.local.entity.WalletTransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletTransferDao {

    @Query("SELECT * FROM wallet_transfers ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<WalletTransferEntity>>

    @Query("SELECT * FROM wallet_transfers WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun getTransfersByDateRange(from: Long, to: Long): Flow<List<WalletTransferEntity>>

    @Query("SELECT * FROM wallet_transfers WHERE id = :id LIMIT 1")
    suspend fun getTransferById(id: String): WalletTransferEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: WalletTransferEntity)

    @Update
    suspend fun updateTransfer(transfer: WalletTransferEntity)

    @Delete
    suspend fun deleteTransfer(transfer: WalletTransferEntity)

    @Query("DELETE FROM wallet_transfers WHERE id = :id")
    suspend fun deleteTransferById(id: String)
}
