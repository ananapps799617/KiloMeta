// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/TripCommissionExactCalculationTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para validación del cálculo exacto de comisiones
// con base en telemetría real de producción (Uber y DiDi).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformConfig
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TripCommissionExactCalculationTest {

    private lateinit var tripRepository: FakeTripRepository
    private lateinit var registerTripUseCase: RegisterTripUseCase

    @Before
    fun setUp() {
        tripRepository = FakeTripRepository()
        registerTripUseCase = RegisterTripUseCase(tripRepository)
    }

    @Test
    fun uberNormal_exactCalculation_matchesRealTelemetry() {
        runBlocking {
            // Caso Real Uber 1: Bruto $10.938, Comisión $2.907, Ganancia Neta $8.031
            val gross = 10938.0
            val commission = 2907.0
            val expectedNet = 8031.0

            val trip = registerTripUseCase(
                shiftId = "shift-001",
                platformType = PlatformType.UBER,
                grossAmount = gross,
                commissionAmount = commission,
                paymentMethod = PaymentMethod.CASH,
                paymentDestination = PaymentDestination.CASH_IN_HAND
            )

            assertEquals(expectedNet, trip.netAmount, 0.001)
            assertEquals(gross, trip.grossAmount, 0.001)
            assertEquals(commission, trip.commissionAmount, 0.001)
        }
    }

    @Test
    fun uberSurge_exactCalculation_matchesRealTelemetry() {
        runBlocking {
            // Caso Real Uber 2 (Dinámica): Bruto $9.132, Comisión $2.906, Ganancia Neta $6.226
            val gross = 9132.0
            val commission = 2906.0
            val expectedNet = 6226.0

            val trip = registerTripUseCase(
                shiftId = "shift-001",
                platformType = PlatformType.UBER,
                grossAmount = gross,
                commissionAmount = commission,
                paymentMethod = PaymentMethod.CASH,
                paymentDestination = PaymentDestination.CASH_IN_HAND
            )

            assertEquals(expectedNet, trip.netAmount, 0.001)
        }
    }

    @Test
    fun didiCase1_ponTuPrecio_matchesRealTelemetry() {
        runBlocking {
            // Caso Real DiDi 1: Bruto $8.700, Comisión $1.983, Ganancia Neta $6.717
            val gross = 8700.0
            val commission = 1983.0
            val expectedNet = 6717.0

            val trip = registerTripUseCase(
                shiftId = "shift-001",
                platformType = PlatformType.DIDI,
                grossAmount = gross,
                commissionAmount = commission,
                paymentMethod = PaymentMethod.CASH,
                paymentDestination = PaymentDestination.CASH_IN_HAND
            )

            assertEquals(expectedNet, trip.netAmount, 0.001)
        }
    }

    @Test
    fun didiCase2_ponTuPrecio_matchesRealTelemetry() {
        runBlocking {
            // Caso Real DiDi 2: Bruto $8.500, Comisión $1.708, Ganancia Neta $6.792
            val gross = 8500.0
            val commission = 1708.0
            val expectedNet = 6792.0

            val trip = registerTripUseCase(
                shiftId = "shift-001",
                platformType = PlatformType.DIDI,
                grossAmount = gross,
                commissionAmount = commission,
                paymentMethod = PaymentMethod.CASH,
                paymentDestination = PaymentDestination.CASH_IN_HAND
            )

            assertEquals(expectedNet, trip.netAmount, 0.001)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun commissionExceedingGross_throwsException() {
        runBlocking {
            registerTripUseCase(
                shiftId = "shift-001",
                platformType = PlatformType.UBER,
                grossAmount = 10000.0,
                commissionAmount = 12000.0,
                paymentMethod = PaymentMethod.CASH
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeGross_throwsException() {
        runBlocking {
            registerTripUseCase(
                shiftId = "shift-001",
                platformType = PlatformType.UBER,
                grossAmount = -5000.0,
                commissionAmount = 0.0,
                paymentMethod = PaymentMethod.CASH
            )
        }
    }

    @Test
    fun referenceRates_areConfiguredCorrectly() {
        assertEquals(28.0, PlatformConfig.getRule(PlatformType.UBER).referenceCommissionPct, 0.01)
        assertEquals(21.5, PlatformConfig.getRule(PlatformType.DIDI).referenceCommissionPct, 0.01)
        assertEquals(10.0, PlatformConfig.getRule(PlatformType.INDRIVE).referenceCommissionPct, 0.01)
        assertEquals(0.0, PlatformConfig.getRule(PlatformType.PRIVATE).referenceCommissionPct, 0.01)
    }

    @Test
    fun tripWithOdometers_calculatesDistanceCorrectly() {
        runBlocking {
            val trip = registerTripUseCase(
                shiftId = "shift-001",
                platformType = PlatformType.UBER,
                grossAmount = 15000.0,
                commissionAmount = 4200.0,
                paymentMethod = PaymentMethod.CASH,
                startOdometer = 7980,
                endOdometer = 7993
            )

            assertEquals(7980, trip.startOdometer)
            assertEquals(7993, trip.endOdometer)
            assertEquals(13.0, trip.tripDistanceKm ?: 0.0, 0.001)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun endOdometerLessThanStart_throwsException() {
        runBlocking {
            registerTripUseCase(
                shiftId = "shift-001",
                platformType = PlatformType.UBER,
                grossAmount = 15000.0,
                commissionAmount = 4200.0,
                paymentMethod = PaymentMethod.CASH,
                startOdometer = 8000,
                endOdometer = 7950
            )
        }
    }

    private class FakeTripRepository : TripRepository {
        val savedTrips = mutableListOf<Trip>()

        override suspend fun addTrip(trip: Trip) {
            savedTrips.add(trip)
        }

        override suspend fun deleteTrip(tripId: String) {
            savedTrips.removeAll { it.id == tripId }
        }

        override fun getTripsByShift(shiftId: String): Flow<List<Trip>> = emptyFlow()
    }
}
