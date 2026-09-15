// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/ManagePlacesUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para ManagePlacesUseCase.
// Valida Domain Guards de nombre/dirección no vacíos, delegación al repositorio
// y flujo reactivo de lugares frecuentes.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.Place
import com.ananapps.kilometa.domain.model.PlaceCategory
import com.ananapps.kilometa.domain.repository.PlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ManagePlacesUseCaseTest {

    private lateinit var fakeRepository: FakePlaceRepository
    private lateinit var useCase: ManagePlacesUseCase

    @Before
    fun setUp() {
        fakeRepository = FakePlaceRepository()
        useCase = ManagePlacesUseCase(fakeRepository)
    }

    @Test
    fun `savePlace con datos validos guarda exitosamente en repositorio`() = runTest {
        val result = useCase.savePlace(
            name = "Bomba Terpel Calle 80",
            category = PlaceCategory.GAS_STATION_GNV,
            address = "Calle 80 # 68D-20",
            notes = "GNV rapido"
        )

        assertTrue(result.isSuccess)
        val place = result.getOrThrow()
        assertEquals("Bomba Terpel Calle 80", place.name)
        assertEquals(PlaceCategory.GAS_STATION_GNV, place.category)
        assertEquals("Calle 80 # 68D-20", place.address)
        assertEquals("GNV rapido", place.notes)

        val stored = fakeRepository.getAllPlaces().first()
        assertEquals(1, stored.size)
        assertEquals(place.id, stored.first().id)
    }

    @Test
    fun `savePlace con nombre en blanco retorna fallo`() = runTest {
        val result = useCase.savePlace(
            name = "   ",
            category = PlaceCategory.GAS_STATION_GNV,
            address = "Calle 80 # 68D-20"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(result.exceptionOrNull()?.message?.contains("nombre") == true)
    }

    @Test
    fun `savePlace con direccion en blanco retorna fallo`() = runTest {
        val result = useCase.savePlace(
            name = "Taller Don Mario",
            category = PlaceCategory.WORKSHOP_MECHANIC,
            address = "   "
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(result.exceptionOrNull()?.message?.contains("dirección") == true)
    }

    @Test
    fun `savePlace con tarifas de combustible guarda correctamente`() = runTest {
        val result = useCase.savePlace(
            name = "Bomba Gazel Calle 80",
            category = PlaceCategory.GAS_STATION_GNV,
            address = "Calle 80 # 70-10",
            notes = "Buena presión",
            gnvPricePerM3 = 3899.0,
            gasolinePricePerGal = 15600.0
        )

        assertTrue(result.isSuccess)
        val place = result.getOrThrow()
        assertEquals(3899.0, place.gnvPricePerM3)
        assertEquals(15600.0, place.gasolinePricePerGal)
    }

    @Test
    fun `updateStationPrice actualiza la tarifa de la estacion en repositorio`() = runTest {
        val saveResult = useCase.savePlace(
            name = "Bomba Terpel Autonorte",
            category = PlaceCategory.GAS_STATION_GNV,
            address = "Autonorte # 127",
            gnvPricePerM3 = 3800.0
        )
        val placeId = saveResult.getOrThrow().id

        useCase.updateStationPrice(placeId, com.ananapps.kilometa.domain.model.FuelType.GAS, 3950.0)

        val updated = fakeRepository.getAllPlaces().first().first { it.id == placeId }
        assertEquals(3950.0, updated.gnvPricePerM3)
    }

    @Test
    fun `deletePlace elimina el lugar del repositorio`() = runTest {
        val saveResult = useCase.savePlace(
            name = "Lavadero El Chorro",
            category = PlaceCategory.CAR_WASH,
            address = "Carrera 30 # 45-10"
        )
        val placeId = saveResult.getOrThrow().id

        assertEquals(1, fakeRepository.getAllPlaces().first().size)

        useCase.deletePlace(placeId)

        assertEquals(0, fakeRepository.getAllPlaces().first().size)
    }

    // Fake in-memory repository
    private class FakePlaceRepository : PlaceRepository {
        private val places = mutableListOf<Place>()

        override fun getAllPlaces(): Flow<List<Place>> {
            return flowOf(places.toList())
        }

        override suspend fun savePlace(place: Place) {
            places.removeAll { it.id == place.id }
            places.add(0, place)
        }

        override suspend fun updateStationPrice(placeId: String, fuelType: com.ananapps.kilometa.domain.model.FuelType, newPrice: Double) {
            val idx = places.indexOfFirst { it.id == placeId }
            if (idx >= 0) {
                val current = places[idx]
                val updated = if (fuelType == com.ananapps.kilometa.domain.model.FuelType.GAS) {
                    current.copy(gnvPricePerM3 = newPrice)
                } else {
                    current.copy(gasolinePricePerGal = newPrice)
                }
                places[idx] = updated
            }
        }

        override suspend fun deletePlace(placeId: String) {
            places.removeAll { it.id == placeId }
        }
    }
}
