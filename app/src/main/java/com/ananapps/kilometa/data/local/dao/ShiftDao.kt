// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/dao/ShiftDao.kt
// DESCRIPCIÓN PEDAGÓGICA:
// DAO para la tabla 'shifts'. Gestiona el ciclo de vida del turno y provee
// consultas atómicas (@Transaction) con agregados completos (ShiftWithDetails).
// ==============================================================================

package com.ananapps.kilometa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ananapps.kilometa.data.local.entity.ShiftEntity
import com.ananapps.kilometa.data.local.relation.ShiftWithDetails
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------------
// BLOQUE 1: CONTRATO DE ACCESO A DATOS PARA TURNOS
// ------------------------------------------------------------------------------
@Dao
interface ShiftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: ShiftEntity)

    @Update
    suspend fun updateShift(shift: ShiftEntity)

    @Query("SELECT * FROM shifts WHERE status = 'ACTIVE' LIMIT 1")
    fun getActiveShift(): Flow<ShiftEntity?>

    @Transaction
    @Query("SELECT * FROM shifts WHERE status = 'ACTIVE' LIMIT 1")
    fun getActiveShiftWithDetails(): Flow<ShiftWithDetails?>

    @Transaction
    @Query("SELECT * FROM shifts WHERE id = :shiftId LIMIT 1")
    fun getShiftWithDetailsById(shiftId: String): Flow<ShiftWithDetails?>

    @Query("SELECT * FROM shifts WHERE id = :shiftId LIMIT 1")
    suspend fun getShiftById(shiftId: String): ShiftEntity?

    @Query("SELECT * FROM shifts ORDER BY start_time DESC")
    fun getAllShifts(): Flow<List<ShiftEntity>>

    @Transaction
    @Query("SELECT * FROM shifts ORDER BY start_time DESC")
    fun getAllShiftsWithDetails(): Flow<List<ShiftWithDetails>>

    @Transaction
    @Query("SELECT * FROM shifts WHERE status = 'COMPLETED' ORDER BY start_time DESC")
    fun getCompletedShiftsWithDetails(): Flow<List<ShiftWithDetails>>

    @Query("DELETE FROM shifts WHERE id = :shiftId")
    suspend fun deleteShift(shiftId: String)

    @Query("DELETE FROM shifts")
    suspend fun clearAllShifts()

    @Query("""
        SELECT MAX(odo) FROM (
            SELECT MAX(COALESCE(end_odometer, start_odometer)) AS odo FROM shifts
            UNION ALL
            SELECT MAX(odometer) AS odo FROM fuelings
            UNION ALL
            SELECT MAX(odometer) AS odo FROM maintenance_records
        )
    """)
    fun getLastKnownOdometer(): Flow<Int?>

    @Query("""
        SELECT * FROM shifts 
        WHERE (start_time BETWEEN :dayStart AND :dayEnd) 
           OR (end_time BETWEEN :dayStart AND :dayEnd)
        ORDER BY CASE WHEN status = 'ACTIVE' THEN 0 ELSE 1 END, start_time ASC
    """)
    suspend fun getShiftsForDay(dayStart: Long, dayEnd: Long): List<ShiftEntity>

    @Query("UPDATE trips SET shift_id = :targetShiftId WHERE shift_id = :sourceShiftId")
    suspend fun reassignTripsShift(sourceShiftId: String, targetShiftId: String)

    @Query("UPDATE fuelings SET shift_id = :targetShiftId WHERE shift_id = :sourceShiftId")
    suspend fun reassignFuelingsShift(sourceShiftId: String, targetShiftId: String)

    @Query("UPDATE expenses SET shift_id = :targetShiftId WHERE shift_id = :sourceShiftId")
    suspend fun reassignExpensesShift(sourceShiftId: String, targetShiftId: String)
}
