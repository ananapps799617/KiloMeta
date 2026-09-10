// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/data/repository/DriverProfileRepositoryTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para el contrato y comportamiento de DriverProfileRepository.
// Verifica la emisión reactiva de perfiles de conductor y vehículo segregados.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.domain.model.DriverProfile
import com.ananapps.kilometa.domain.model.Gender
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.QuotaPeriodicity
import com.ananapps.kilometa.domain.model.VehicleProfile
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
    private val driverFlow = MutableStateFlow(DriverProfile())
    private val vehicleFlow = MutableStateFlow(VehicleProfile())

    override fun getProfile(): Flow<DriverProfile> = driverFlow.asStateFlow()

    override suspend fun saveProfile(profile: DriverProfile) {
        driverFlow.value = profile.copy(updatedAt = System.currentTimeMillis())
    }

    override fun getVehicleProfile(): Flow<VehicleProfile> = vehicleFlow.asStateFlow()

    override suspend fun saveVehicleProfile(vehicle: VehicleProfile) {
        vehicleFlow.value = vehicle.copy(updatedAt = System.currentTimeMillis())
    }

    override suspend fun saveFullProfile(driver: DriverProfile, vehicle: VehicleProfile) {
        saveVehicleProfile(vehicle)
        saveProfile(driver.copy(vehiclePlate = vehicle.vehiclePlate, propulsionType = vehicle.propulsionType))
    }

    override suspend fun resetProfileForTesting() {
        driverFlow.value = DriverProfile()
        vehicleFlow.value = VehicleProfile()
    }

    override suspend fun clearProfile() {
        driverFlow.value = DriverProfile(id = "", driverName = "", vehiclePlate = "", isProfileCompleted = false)
        vehicleFlow.value = VehicleProfile(id = "", vehiclePlate = "")
    }

    override suspend fun fetchAndHydrateRemoteProfile(userId: String): Result<Boolean> {
        return Result.success(false)
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
    fun `getProfile y getVehicleProfile emiten perfiles iniciales consistentes`() = runBlocking {
        val initialDriver = repository.getProfile().first()
        val initialVehicle = repository.getVehicleProfile().first()

        assertEquals("", initialDriver.vehiclePlate)
        assertEquals(PropulsionType.BI_FUEL, initialDriver.propulsionType)
        assertEquals("Chevrolet", initialVehicle.brand)
        assertEquals("Joy", initialVehicle.model)
        assertEquals(VehicleTenure.OWNED, initialVehicle.tenureType)
    }

    @Test
    fun `saveFullProfile actualiza y emite reactivamente ambos perfiles`() = runBlocking {
        val newDriver = DriverProfile(
            driverName = "Andrés López",
            phone = "3101234567",
            gender = Gender.MALE
        )
        val newVehicle = VehicleProfile(
            vehiclePlate = "ZAB-123",
            brand = "Renault",
            model = "Kwid",
            propulsionType = PropulsionType.GASOLINE_ONLY,
            tenureType = VehicleTenure.RENTED,
            quotaAmount = 600_000.0,
            quotaPeriodicity = QuotaPeriodicity.WEEKLY,
            workDaysWeek = 6
        )

        repository.saveFullProfile(newDriver, newVehicle)

        val updatedDriver = repository.getProfile().first()
        val updatedVehicle = repository.getVehicleProfile().first()

        assertEquals("Andrés López", updatedDriver.driverName)
        assertEquals(Gender.MALE, updatedDriver.gender)
        assertEquals("ZAB-123", updatedDriver.vehiclePlate)

        assertEquals("Renault", updatedVehicle.brand)
        assertEquals("Kwid", updatedVehicle.model)
        assertEquals(VehicleTenure.RENTED, updatedVehicle.tenureType)
        assertEquals(600_000.0, updatedVehicle.quotaAmount, 0.001)
    }
}
