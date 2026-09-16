// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/model/DriverProfileTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para los modelos canónicos DriverProfile y VehicleProfile.
// ==============================================================================

package com.ananapps.kilometa.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DriverProfileTest {

    @Test
    fun `driverProfile por defecto inicia con valores consistentes`() {
        val profile = DriverProfile()
        assertEquals("default_driver", profile.id)
        assertEquals("", profile.driverName)
        assertEquals(Gender.PREFER_NOT_TO_SAY, profile.gender)
        assertEquals("primary_vehicle", profile.activeVehicleId)
        assertEquals("", profile.vehiclePlate)
        assertEquals(PropulsionType.BI_FUEL, profile.propulsionType)
        assertTrue(profile.activePlatforms.contains(PlatformType.UBER))
    }

    @Test
    fun `vehicleProfile por defecto tiene configuracion colombiana estandar`() {
        val vehicle = VehicleProfile()
        assertEquals(VehicleType.PARTICULAR, vehicle.vehicleType)
        assertEquals("WMB-789", vehicle.vehiclePlate)
        assertEquals("Chevrolet", vehicle.brand)
        assertEquals("Joy", vehicle.model)
        assertEquals(TankSystem.DUAL_TANK, vehicle.tankSystem)
        assertEquals(PropulsionType.BI_FUEL, vehicle.propulsionType)
        assertEquals(VehicleTenure.OWNED, vehicle.tenureType)
        assertEquals(500_000.0, vehicle.quotaAmount, 0.001)
        assertEquals(QuotaPeriodicity.WEEKLY, vehicle.quotaPeriodicity)
        assertEquals(6, vehicle.workDaysWeek)
        assertEquals(2, vehicle.quotaCutoffDay) // Martes
        assertEquals(23, vehicle.quotaCutoffHour)
    }

    @Test
    fun `driverProfile copy actualiza campos preservando inmutabilidad`() {
        val original = DriverProfile()
        val modified = original.copy(
            driverName = "Andrés López",
            phone = "3101234567",
            gender = Gender.MALE
        )

        assertEquals("Andrés López", modified.driverName)
        assertEquals("3101234567", modified.phone)
        assertEquals(Gender.MALE, modified.gender)
        assertEquals("", original.driverName)
    }

    @Test
    fun `propulsionType mapea correctamente al combustible por defecto`() {
        assertEquals(FuelType.GASOLINE, PropulsionType.GASOLINE_ONLY.defaultFuel)
        assertEquals(FuelType.GAS, PropulsionType.GAS_ONLY.defaultFuel)
        assertEquals(FuelType.GAS, PropulsionType.BI_FUEL.defaultFuel)
        assertEquals(FuelType.DIESEL, PropulsionType.DIESEL.defaultFuel)
        assertEquals(FuelType.ELECTRICITY, PropulsionType.ELECTRIC_100.defaultFuel)
    }

    @Test
    fun `isReadyForCabina valida flag completado, nombre no vacio y placa no vacia`() {
        val nuevo = DriverProfile()
        org.junit.Assert.assertFalse(nuevo.isReadyForCabina)

        val configurado = nuevo.copy(
            driverName = "Andrés López",
            vehiclePlate = "WMB-789",
            isProfileCompleted = true
        )
        assertTrue(configurado.isReadyForCabina)

        val incompletoSinNombre = configurado.copy(driverName = "")
        org.junit.Assert.assertFalse(incompletoSinNombre.isReadyForCabina)
    }
}
