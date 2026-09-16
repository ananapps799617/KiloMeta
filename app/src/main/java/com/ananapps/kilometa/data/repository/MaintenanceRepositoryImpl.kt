// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/MaintenanceRepositoryImpl.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación del repositorio de Mantenimiento conectado a Room SQLite y Coroutines.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.core.dispatcher.CoroutineDispatchers
import com.ananapps.kilometa.data.local.dao.MaintenanceDao
import com.ananapps.kilometa.data.mapper.toDomain
import com.ananapps.kilometa.data.mapper.toEntity
import com.ananapps.kilometa.domain.model.MaintenanceRecord
import com.ananapps.kilometa.domain.model.MaintenanceType
import com.ananapps.kilometa.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: IMPLEMENTACIÓN DEL REPOSITORIO DE MANTENIMIENTO
// ------------------------------------------------------------------------------
@Singleton
class MaintenanceRepositoryImpl @Inject constructor(
    private val maintenanceDao: MaintenanceDao,
    private val dispatchers: CoroutineDispatchers
) : MaintenanceRepository {

    override fun getAllMaintenances(): Flow<List<MaintenanceRecord>> {
        return maintenanceDao.getAllMaintenances()
            .map { it.toDomain() }
            .flowOn(dispatchers.io)
    }

    override fun getMaintenancesByVehicle(vehicleId: String): Flow<List<MaintenanceRecord>> {
        return maintenanceDao.getMaintenancesByVehicle(vehicleId)
            .map { it.toDomain() }
            .flowOn(dispatchers.io)
    }

    override suspend fun getLatestMaintenanceByType(type: MaintenanceType): MaintenanceRecord? {
        return withContext(dispatchers.io) {
            maintenanceDao.getLatestMaintenanceByType(type.name)?.toDomain()
        }
    }

    override fun getTotalMaintenanceCost(): Flow<Double> {
        return maintenanceDao.getTotalMaintenanceCost()
            .map { it ?: 0.0 }
            .flowOn(dispatchers.io)
    }

    override suspend fun addMaintenance(record: MaintenanceRecord) {
        withContext(dispatchers.io) {
            maintenanceDao.insertMaintenance(record.toEntity())
        }
    }

    override suspend fun updateMaintenance(record: MaintenanceRecord) {
        withContext(dispatchers.io) {
            maintenanceDao.updateMaintenance(record.toEntity())
        }
    }

    override suspend fun deleteMaintenance(id: String) {
        withContext(dispatchers.io) {
            maintenanceDao.deleteMaintenance(id)
        }
    }
}
