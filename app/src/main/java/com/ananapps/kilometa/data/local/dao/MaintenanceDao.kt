// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/dao/MaintenanceDao.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Data Access Object (DAO) para operaciones sobre la tabla 'maintenance_records'.
// Provee flujos reactivos de historial de servicios e inserción/eliminación atómica.
// ==============================================================================

package com.ananapps.kilometa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ananapps.kilometa.data.local.entity.MaintenanceEntity
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------------
// BLOQUE 1: CONTRATO DE CONSULTAS Y PERSISTENCIA DE MANTENIMIENTO
// ------------------------------------------------------------------------------
@Dao
interface MaintenanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenance(maintenance: MaintenanceEntity)

    @Update
    suspend fun updateMaintenance(maintenance: MaintenanceEntity)

    @Query("SELECT * FROM maintenance_records ORDER BY odometer DESC, timestamp DESC")
    fun getAllMaintenances(): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM maintenance_records WHERE vehicle_id = :vehicleId ORDER BY odometer DESC")
    fun getMaintenancesByVehicle(vehicleId: String): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM maintenance_records WHERE maintenance_type = :type ORDER BY odometer DESC LIMIT 1")
    suspend fun getLatestMaintenanceByType(type: String): MaintenanceEntity?

    @Query("SELECT SUM(cost_cop) FROM maintenance_records")
    fun getTotalMaintenanceCost(): Flow<Double?>

    @Query("DELETE FROM maintenance_records WHERE id = :id")
    suspend fun deleteMaintenance(id: String)
}
