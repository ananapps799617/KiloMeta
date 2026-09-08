// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/CalculateDynamicQuotaUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pruebas unitarias para el motor de cuota dinámica de vehículos y arriendos.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.RentalAgreement
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.domain.model.VehicleTenure
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateDynamicQuotaUseCaseTest {

    private lateinit var useCase: CalculateDynamicQuotaUseCase

    @Before
    fun setup() {
        useCase = CalculateDynamicQuotaUseCase()
    }

    @Test
    fun whenVehicleHasQuotaAndWorkDays_calculatesDailyQuotaCorrectly() {
        val vehicle = VehicleProfile(
            quotaAmount = 600_000.0,
            workDaysWeek = 6,
            tenureType = VehicleTenure.RENTED
        )
        val result = useCase(vehicle)
        assertEquals(100_000.0, result, 0.001)
    }

    @Test
    fun whenVehicleHasZeroQuota_returnsZero() {
        val vehicle = VehicleProfile(
            quotaAmount = 0.0,
            workDaysWeek = 6,
            tenureType = VehicleTenure.OWNED
        )
        val result = useCase(vehicle)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun whenWorkDaysWeekIsCoercedToValidRange() {
        val vehicleZeroDays = VehicleProfile(
            quotaAmount = 500_000.0,
            workDaysWeek = 0,
            tenureType = VehicleTenure.RENTED
        )
        assertEquals(500_000.0, useCase(vehicleZeroDays), 0.001)

        val vehicleEightDays = VehicleProfile(
            quotaAmount = 700_000.0,
            workDaysWeek = 10,
            tenureType = VehicleTenure.RENTED
        )
        assertEquals(100_000.0, useCase(vehicleEightDays), 0.001)
    }

    @Test
    fun rentalAgreementCalculatesDailyQuotaRequiredCorrectly() {
        val agreement = RentalAgreement(
            quotaAmount = 420_000.0,
            workDaysWeek = 6,
            quotaCutoffDay = 2,
            quotaCutoffHour = 18
        )
        assertEquals(70_000.0, agreement.dailyQuotaRequired, 0.001)
    }
}
