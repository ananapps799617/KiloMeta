// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/MaintenanceRepository.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contrato de Dominio para la gestión de registros de mantenimiento preventivo.
// ==============================================================================

package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.MaintenanceRecord
import com.ananapps.kilometa.domain.model.MaintenanceType
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------------
// BLOQUE 1: INTERFAZ DEL REPOSITORIO DE MANTENIMIENTO
// ------------------------------------------------------------------------------
interface MaintenanceRepository {

    fun getAllMaintenances(): Flow<List<MaintenanceRecord>>

    fun getMaintenancesByVehicle(vehicleId: String): Flow<List<MaintenanceRecord>>

    suspend fun getLatestMaintenanceByType(type: MaintenanceType): MaintenanceRecord?

    fun getTotalMaintenanceCost(): Flow<Double>

    suspend fun addMaintenance(record: MaintenanceRecord)

    suspend fun deleteMaintenance(id: String)
}
