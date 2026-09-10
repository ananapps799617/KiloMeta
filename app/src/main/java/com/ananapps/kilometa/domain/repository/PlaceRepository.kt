// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/PlaceRepository.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contrato de dominio para la gestión de lugares favoritos y puntos de servicio.
// ==============================================================================

package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.Place
import kotlinx.coroutines.flow.Flow

interface PlaceRepository {
    fun getAllPlaces(): Flow<List<Place>>
    suspend fun savePlace(place: Place)
    suspend fun deletePlace(placeId: String)
}
