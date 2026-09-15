// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/ManagePlacesUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para gestionar lugares favoritos con Domain Guards estrictos.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.Place
import com.ananapps.kilometa.domain.model.PlaceCategory
import com.ananapps.kilometa.domain.repository.PlaceRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class ManagePlacesUseCase @Inject constructor(
    private val placeRepository: PlaceRepository
) {
    fun getAllPlaces(): Flow<List<Place>> = placeRepository.getAllPlaces()

    suspend fun savePlace(
        name: String,
        category: PlaceCategory,
        address: String,
        notes: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        gnvPricePerM3: Double? = null,
        gasolinePricePerGal: Double? = null
    ): Result<Place> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre del lugar no puede estar vacío"))
        }
        if (address.isBlank()) {
            return Result.failure(IllegalArgumentException("La dirección o referencia no puede estar vacía"))
        }

        val place = Place(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            category = category,
            address = address.trim(),
            latitude = latitude,
            longitude = longitude,
            notes = notes?.trim(),
            gnvPricePerM3 = gnvPricePerM3,
            gasolinePricePerGal = gasolinePricePerGal,
            createdAt = System.currentTimeMillis()
        )

        placeRepository.savePlace(place)
        return Result.success(place)
    }

    suspend fun updateStationPrice(placeId: String, fuelType: com.ananapps.kilometa.domain.model.FuelType, newPrice: Double) {
        require(placeId.isNotBlank()) { "El ID del lugar no puede estar vacío" }
        require(newPrice > 0.0) { "El precio debe ser mayor a cero" }
        placeRepository.updateStationPrice(placeId, fuelType, newPrice)
    }

    suspend fun deletePlace(placeId: String) {
        require(placeId.isNotBlank()) { "El ID del lugar no puede estar vacío" }
        placeRepository.deletePlace(placeId)
    }
}
