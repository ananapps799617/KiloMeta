// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/dao/FuelingDao.kt
// DESCRIPCIÓN PEDAGÓGICA:
// DAO para la tabla 'fuelings'. Controla el registro y consulta de recargas
// bi-combustible (Gas vs Gasolina) por turno.
// ==============================================================================

package com.ananapps.kilometa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ananapps.kilometa.data.local.entity.FuelingEntity
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------------
// BLOQUE 1: CONTRATO DE ACCESO A DATOS PARA RECARRGAS DE COMBUSTIBLE
// ------------------------------------------------------------------------------
@Dao
interface FuelingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFueling(fueling: FuelingEntity)

    @Query("SELECT * FROM fuelings WHERE shift_id = :shiftId ORDER BY timestamp DESC")
    fun getFuelingsByShift(shiftId: String): Flow<List<FuelingEntity>>

    @Query("SELECT * FROM fuelings WHERE id = :fuelingId LIMIT 1")
    suspend fun getFuelingById(fuelingId: String): FuelingEntity?

    @Query("DELETE FROM fuelings WHERE id = :fuelingId")
    suspend fun deleteFueling(fuelingId: String)

    @androidx.room.Update
    suspend fun updateFueling(fueling: FuelingEntity)

    @Query("SELECT id FROM fuelings WHERE shift_id = :shiftId")
    suspend fun getFuelingIdsByShift(shiftId: String): List<String>

    @Query("SELECT * FROM fuelings ORDER BY timestamp DESC")
    fun getAllFuelings(): Flow<List<FuelingEntity>>
}
