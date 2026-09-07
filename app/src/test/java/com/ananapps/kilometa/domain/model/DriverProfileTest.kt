// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/model/DriverProfileTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para el modelo canónico DriverProfile y sus enums asociados
// (PropulsionType, VehicleTenure, QuotaPeriodicity, FuelType).
// Regla de Oro: Tipado estricto al 100%, inmutabilidad, cero dependencias de Android.
// ==============================================================================

package com.ananapps.kilometa.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DriverProfileTest {

    // --------------------------------------------------------------------------
    // BLOQUE 1: PRUEBAS DE VALORES POR DEFECTO Y CONSTRUCTOR
    // --------------------------------------------------------------------------
    @Test
    fun `driverProfile por defecto inicia con Bi-Combustible, Propio y Cuota Semanal`() {
        val defaultProfile = DriverProfile()

        assertEquals("Chevrolet Joy / Spark GT", defaultProfile.vehicleName)
        assertEquals("WMB-789", defaultProfile.vehiclePlate)
        assertEquals(PropulsionType.BI_FUEL, defaultProfile.propulsionType)
        assertEquals(VehicleTenure.OWNED, defaultProfile.vehicleTenure)
        assertEquals(500_000.0, defaultProfile.targetQuotaAmount, 0.001)
        assertEquals(QuotaPeriodicity.WEEKLY, defaultProfile.quotaPeriodicity)
        assertEquals(6, defaultProfile.workingDaysPerWeek)
        assertTrue(defaultProfile.activePlatforms.contains(PlatformType.UBER))
        assertTrue(defaultProfile.activePlatforms.contains(PlatformType.DIDI))
        assertTrue(defaultProfile.activePlatforms.contains(PlatformType.INDRIVE))
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: ASOCIACIÓN CANÓNICA DE SISTEMAS DE PROPULSIÓN Y COMBUSTIBLE
    // --------------------------------------------------------------------------
    @Test
    fun `propulsionType mapea correctamente al combustible por defecto`() {
        assertEquals(FuelType.GASOLINE, PropulsionType.GASOLINE_ONLY.defaultFuel)
        assertEquals(FuelType.GAS, PropulsionType.GAS_ONLY.defaultFuel)
        assertEquals(FuelType.GAS, PropulsionType.BI_FUEL.defaultFuel)
        assertEquals(FuelType.DIESEL, PropulsionType.DIESEL.defaultFuel)
        assertEquals(FuelType.ELECTRICITY, PropulsionType.ELECTRIC_100.defaultFuel)
    }

    @Test
    fun `fuelType incorpora ACPM y Electricidad con etiquetas correspondientes`() {
        assertEquals("Diésel (ACPM)", FuelType.DIESEL.displayName)
        assertEquals("Gal", FuelType.DIESEL.unitLabel)

        assertEquals("Electricidad (EV)", FuelType.ELECTRICITY.displayName)
        assertEquals("kWh", FuelType.ELECTRICITY.unitLabel)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: INMUTABILIDAD Y OPERACIONES DE COPIA
    // --------------------------------------------------------------------------
    @Test
    fun `driverProfile copy actualiza campos preservando inmutabilidad`() {
        val original = DriverProfile()
        val modified = original.copy(
            vehicleName = "Renault Zoe EV",
            vehiclePlate = "EVX-001",
            propulsionType = PropulsionType.ELECTRIC_100,
            vehicleTenure = VehicleTenure.RENTED,
            targetQuotaAmount = 700_000.0,
            workingDaysPerWeek = 5
        )

        assertEquals("Renault Zoe EV", modified.vehicleName)
        assertEquals("EVX-001", modified.vehiclePlate)
        assertEquals(PropulsionType.ELECTRIC_100, modified.propulsionType)
        assertEquals(VehicleTenure.RENTED, modified.vehicleTenure)
        assertEquals(700_000.0, modified.targetQuotaAmount, 0.001)
        assertEquals(5, modified.workingDaysPerWeek)

        // El original permanece intacto
        assertEquals("Chevrolet Joy / Spark GT", original.vehicleName)
        assertEquals(PropulsionType.BI_FUEL, original.propulsionType)
    }
}
