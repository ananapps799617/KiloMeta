// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/dao/TripDao.kt
// DESCRIPCIÓN PEDAGÓGICA:
// DAO para la tabla 'trips'. Controla el registro y consulta reactiva de carreras
// asociadas a un turno de trabajo.
// ==============================================================================

package com.ananapps.kilometa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ananapps.kilometa.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------------
// BLOQUE 1: CONTRATO DE ACCESO A DATOS PARA CARRERAS
// ------------------------------------------------------------------------------
@Dao
interface TripDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Query("SELECT * FROM trips WHERE shift_id = :shiftId ORDER BY timestamp DESC")
    fun getTripsByShift(shiftId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    suspend fun getTripById(tripId: String): TripEntity?

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTrip(tripId: String)

    @Query("SELECT COUNT(*) FROM trips WHERE shift_id = :shiftId")
    suspend fun getTripCountByShift(shiftId: String): Int

    @Query("SELECT * FROM trips ORDER BY timestamp DESC")
    fun getAllTrips(): Flow<List<TripEntity>>
}
