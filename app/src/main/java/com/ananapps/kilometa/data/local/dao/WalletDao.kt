package com.ananapps.kilometa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ananapps.kilometa.data.local.entity.WalletEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la gestión de bolsillos y billeteras soberanas (Orden #054).
 * Provee reactividad con Flow gobernada por el InvalidationTracker de Room SQLite.
 */
@Dao
interface WalletDao {

    @Query("SELECT * FROM wallets WHERE is_active = 1 ORDER BY is_system_protected DESC, name ASC")
    fun observeActiveWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets ORDER BY is_system_protected DESC, name ASC")
    fun observeAllWallets(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    suspend fun getWalletById(id: String): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Query("UPDATE wallets SET is_active = :isActive WHERE id = :id AND is_system_protected = 0")
    suspend fun updateWalletActiveStatus(id: String, isActive: Boolean)

    @Query("UPDATE wallets SET balance = balance + :delta WHERE id = :id")
    suspend fun updateWalletBalance(id: String, delta: Double)

    @Query("UPDATE wallets SET balance = :balance WHERE id = :id")
    suspend fun setWalletBalance(id: String, balance: Double)

    @Query("DELETE FROM wallets WHERE id = :id AND is_system_protected = 0")
    suspend fun deleteCustomWallet(id: String)
}
