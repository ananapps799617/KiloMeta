// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/UpdateTripUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pruebas unitarias para UpdateTripUseCase. Valida la actualización consistente de
// carreras con comisión ordinaria, carreras con bono/subsidio y guardrails contables.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.Trip
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class UpdateTripUseCaseTest {

    private lateinit var tripRepository: FakeTripRepository
    private lateinit var updateTripUseCase: UpdateTripUseCase

    @Before
    fun setUp() {
        tripRepository = FakeTripRepository()
        updateTripUseCase = UpdateTripUseCase(tripRepository)
    }

    @Test
    fun updateTrip_withStandardCommission_calculatesNetAndDestinationCorrectly() = runTest {
        val originalTrip = Trip(
            id = "trip-1",
            shiftId = "shift-1",
            platformType = PlatformType.UBER,
            grossAmount = 41636.0,
            commissionAmount = 3367.0,
            netAmount = 38269.0,
            paymentMethod = PaymentMethod.CASH,
            paymentDestination = PaymentDestination.CASH_IN_HAND,
            timestamp = 1000L
        )
        tripRepository.addTrip(originalTrip)

        val updatedInput = originalTrip.copy(
            paymentMethod = PaymentMethod.APP_WALLET,
            grossAmount = 50000.0,
            commissionAmount = 5000.0,
            netAmount = 0.0 // Debe ser recalculado
        )

        updateTripUseCase(updatedInput)

        val saved = tripRepository.savedTrips.first { it.id == "trip-1" }
        assertEquals(45000.0, saved.netAmount, 0.001)
        assertEquals(PaymentDestination.APP_RETAINED, saved.paymentDestination)
    }

    @Test
    fun updateTrip_withBonusOrSubsidy_addsBonusToNetIncome() = runTest {
        // Caso exacto reportado por Gerencia: cobró $10.000, bono de app +$2.085 -> ganancia limpia $12.085
        val originalTrip = Trip(
            id = "trip-bonus",
            shiftId = "shift-1",
            platformType = PlatformType.UBER,
            grossAmount = 10000.0,
            commissionAmount = 0.0,
            netAmount = 10000.0,
            paymentMethod = PaymentMethod.NEQUI,
            paymentDestination = PaymentDestination.CASH_IN_HAND,
            timestamp = 1000L,
            platformPromoDiscount = 2085.0
        )
        tripRepository.addTrip(originalTrip)

        val editedTrip = originalTrip.copy(
            grossAmount = 10000.0,
            commissionAmount = 0.0,
            platformPromoDiscount = 2085.0,
            netAmount = 0.0 // Asegura que el caso de uso compute 12085
        )

        updateTripUseCase(editedTrip)

        val saved = tripRepository.savedTrips.first { it.id == "trip-bonus" }
        assertEquals(12085.0, saved.netAmount, 0.001)
        assertEquals(PaymentDestination.CASH_IN_HAND, saved.paymentDestination)
    }

    @Test
    fun updateTrip_withNegativePromo_throwsIllegalArgumentException() = runTest {
        val invalidTrip = Trip(
            id = "trip-invalid",
            shiftId = "shift-1",
            platformType = PlatformType.DIDI,
            grossAmount = 15000.0,
            commissionAmount = 2000.0,
            netAmount = 13000.0,
            paymentMethod = PaymentMethod.CASH,
            timestamp = 1000L,
            platformPromoDiscount = -500.0
        )

        try {
            updateTripUseCase(invalidTrip)
            org.junit.Assert.fail("Se esperaba IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("El bono/subsidio no puede ser negativo", e.message)
        }
    }
}
