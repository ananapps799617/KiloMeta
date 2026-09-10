// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/DeleteTripUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pruebas unitarias para el caso de uso DeleteTripUseCase.
// Valida la eliminación correcta por ID y el rechazo ante identificadores vacíos.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.Trip
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteTripUseCaseTest {

    private lateinit var tripRepository: FakeTripRepository
    private lateinit var deleteTripUseCase: DeleteTripUseCase

    @Before
    fun setUp() {
        tripRepository = FakeTripRepository()
        deleteTripUseCase = DeleteTripUseCase(tripRepository)
    }

    @Test
    fun deleteTrip_withValidId_removesTripFromRepository() = runTest {
        val trip1 = Trip(
            id = "trip-1",
            shiftId = "shift-100",
            platformType = PlatformType.UBER,
            grossAmount = 25000.0,
            commissionAmount = 5000.0,
            netAmount = 20000.0,
            paymentMethod = PaymentMethod.CASH,
            paymentDestination = PaymentDestination.CASH_IN_HAND,
            timestamp = 1000L
        )
        val trip2 = Trip(
            id = "trip-2",
            shiftId = "shift-100",
            platformType = PlatformType.DIDI,
            grossAmount = 18000.0,
            commissionAmount = 3000.0,
            netAmount = 15000.0,
            paymentMethod = PaymentMethod.APP_WALLET,
            paymentDestination = PaymentDestination.APP_RETAINED,
            timestamp = 2000L
        )
        tripRepository.addTrip(trip1)
        tripRepository.addTrip(trip2)

        assertEquals(2, tripRepository.savedTrips.size)

        deleteTripUseCase("trip-1")

        assertEquals(1, tripRepository.savedTrips.size)
        assertFalse(tripRepository.savedTrips.any { it.id == "trip-1" })
        assertTrue(tripRepository.savedTrips.any { it.id == "trip-2" })
    }

    @Test(expected = IllegalArgumentException::class)
    fun deleteTrip_withBlankId_throwsException() = runTest {
        deleteTripUseCase("   ")
    }
}
