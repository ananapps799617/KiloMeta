// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/FuelingRepositoryImpl.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación del repositorio de recargas bi-combustible conectado a Room.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.core.dispatcher.CoroutineDispatchers
import com.ananapps.kilometa.data.local.dao.FuelingDao
import com.ananapps.kilometa.data.mapper.toDomain
import com.ananapps.kilometa.data.mapper.toEntity
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.repository.FuelingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: IMPLEMENTACIÓN DEL REPOSITORIO DE COMBUSTIBLE
// ------------------------------------------------------------------------------
@Singleton
class FuelingRepositoryImpl @Inject constructor(
    private val fuelingDao: FuelingDao,
    private val dispatchers: CoroutineDispatchers
) : FuelingRepository {

    override fun getFuelingsByShift(shiftId: String): Flow<List<Fueling>> {
        return fuelingDao.getFuelingsByShift(shiftId)
            .map { it.toDomain() }
            .flowOn(dispatchers.io)
    }

    override suspend fun addFueling(fueling: Fueling) {
        withContext(dispatchers.io) {
            fuelingDao.insertFueling(fueling.toEntity())
        }
    }

    override suspend fun deleteFueling(fuelingId: String) {
        withContext(dispatchers.io) {
            fuelingDao.deleteFueling(fuelingId)
        }
    }
}
