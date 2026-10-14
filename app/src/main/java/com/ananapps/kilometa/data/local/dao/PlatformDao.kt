package com.ananapps.kilometa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ananapps.kilometa.data.local.entity.PlatformEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para el catálogo de plataformas de transporte (Orden #054).
 * Provee reactividad con Flow gobernada por el InvalidationTracker de Room SQLite.
 */
@Dao
interface PlatformDao {

    @Query("SELECT * FROM platforms WHERE is_active = 1 ORDER BY is_custom ASC, name ASC")
    fun observeActivePlatforms(): Flow<List<PlatformEntity>>

    @Query("SELECT * FROM platforms ORDER BY is_custom ASC, name ASC")
    fun observeAllPlatforms(): Flow<List<PlatformEntity>>

    @Query("SELECT * FROM platforms WHERE id = :id LIMIT 1")
    suspend fun getPlatformById(id: String): PlatformEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlatform(platform: PlatformEntity)

    @Query("UPDATE platforms SET is_active = :isActive WHERE id = :id")
    suspend fun updatePlatformActiveStatus(id: String, isActive: Boolean)

    @Query("DELETE FROM platforms WHERE id = :id AND is_custom = 1")
    suspend fun deleteCustomPlatform(id: String)
}
