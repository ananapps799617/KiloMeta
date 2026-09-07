// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/TripRepositoryImpl.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación del repositorio de Carreras multi-plataforma conectado a Room.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.core.dispatcher.CoroutineDispatchers
import com.ananapps.kilometa.data.local.dao.TripDao
import com.ananapps.kilometa.data.mapper.toDomain
import com.ananapps.kilometa.data.mapper.toEntity
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: IMPLEMENTACIÓN DEL REPOSITORIO DE CARRERAS
// ------------------------------------------------------------------------------
@Singleton
class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao,
    private val dispatchers: CoroutineDispatchers
) : TripRepository {

    override fun getTripsByShift(shiftId: String): Flow<List<Trip>> {
        return tripDao.getTripsByShift(shiftId)
            .map { it.toDomain() }
            .flowOn(dispatchers.io)
    }

    override suspend fun addTrip(trip: Trip) {
        withContext(dispatchers.io) {
            tripDao.insertTrip(trip.toEntity())
        }
    }

    override suspend fun deleteTrip(tripId: String) {
        withContext(dispatchers.io) {
            tripDao.deleteTrip(tripId)
        }
    }
}
