// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/PlaceRepositoryImpl.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación de persistencia local en Room SQLite para lugares favoritos.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.core.dispatcher.IoDispatcher
import com.ananapps.kilometa.data.local.dao.PlaceDao
import com.ananapps.kilometa.data.local.entity.PlaceEntity
import com.ananapps.kilometa.domain.model.Place
import com.ananapps.kilometa.domain.model.PlaceCategory
import com.ananapps.kilometa.domain.repository.PlaceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceRepositoryImpl @Inject constructor(
    private val placeDao: PlaceDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PlaceRepository {

    override fun getAllPlaces(): Flow<List<Place>> {
        return placeDao.getAllPlaces().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override suspend fun savePlace(place: Place) {
        withContext(ioDispatcher) {
            placeDao.insertPlace(place.toEntity())
        }
    }

    override suspend fun deletePlace(placeId: String) {
        withContext(ioDispatcher) {
            placeDao.deletePlace(placeId)
        }
    }

    private fun PlaceEntity.toDomain(): Place {
        val cat = runCatching { PlaceCategory.valueOf(category) }.getOrDefault(PlaceCategory.OTHER)
        return Place(
            id = id,
            name = name,
            category = cat,
            address = address,
            latitude = latitude,
            longitude = longitude,
            notes = notes,
            createdAt = createdAt
        )
    }

    private fun Place.toEntity(): PlaceEntity {
        return PlaceEntity(
            id = id,
            name = name,
            category = category.name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            notes = notes,
            createdAt = createdAt
        )
    }
}
