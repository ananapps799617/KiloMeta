// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/data/repository/DriverProfileRepositoryTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para el contrato y comportamiento de DriverProfileRepository.
// Verifica la emisión reactiva de perfiles y la persistencia de cambios en memoria.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.domain.model.DriverProfile
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.QuotaPeriodicity
import com.ananapps.kilometa.domain.model.VehicleTenure
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// ------------------------------------------------------------------------------
// BLOQUE 1: REPOSITORIO FAKE PARA PRUEBAS UNITARIAS
// ------------------------------------------------------------------------------
private class FakeDriverProfileRepository : DriverProfileRepository {
    private val profileFlow = MutableStateFlow(DriverProfile())

    override fun getProfile(): Flow<DriverProfile> = profileFlow.asStateFlow()

    override suspend fun saveProfile(profile: DriverProfile) {
        profileFlow.value = profile.copy(updatedAt = System.currentTimeMillis())
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: SUITE DE PRUEBAS DEL REPOSITORIO DE PERFIL
// ------------------------------------------------------------------------------
class DriverProfileRepositoryTest {

    private lateinit var repository: DriverProfileRepository

    @Before
    fun setUp() {
        repository = FakeDriverProfileRepository()
    }

    @Test
    fun `getProfile emite perfil por defecto inicial`() = runBlocking {
        val initialProfile = repository.getProfile().first()

        assertEquals("Chevrolet Joy / Spark GT", initialProfile.vehicleName)
        assertEquals(PropulsionType.BI_FUEL, initialProfile.propulsionType)
        assertEquals(VehicleTenure.OWNED, initialProfile.vehicleTenure)
    }

    @Test
    fun `saveProfile actualiza y emite reactivamente la nueva configuracion del vehiculo`() = runBlocking {
        val newProfile = DriverProfile(
            vehicleName = "Kia Niro EV",
            vehiclePlate = "ZAB-123",
            propulsionType = PropulsionType.ELECTRIC_100,
            vehicleTenure = VehicleTenure.RENTED,
            targetQuotaAmount = 600_000.0,
            quotaPeriodicity = QuotaPeriodicity.WEEKLY,
            workingDaysPerWeek = 6
        )

        repository.saveProfile(newProfile)

        val updated = repository.getProfile().first()
        assertEquals("Kia Niro EV", updated.vehicleName)
        assertEquals("ZAB-123", updated.vehiclePlate)
        assertEquals(PropulsionType.ELECTRIC_100, updated.propulsionType)
        assertEquals(VehicleTenure.RENTED, updated.vehicleTenure)
        assertEquals(600_000.0, updated.targetQuotaAmount, 0.001)
    }
}
