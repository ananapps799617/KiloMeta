package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.OfferSemaphoreTier
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.TripOffer
import org.junit.Assert.*
import org.junit.Test

class EvaluateTripOfferUseCaseTest {

    private val evaluateTripOfferUseCase = EvaluateTripOfferUseCase()

    @Test
    fun testProfitableOfferReturnsGreen() {
        val offer = TripOffer(
            platform = PlatformType.UBER,
            offeredFare = 24000.0,
            pickupDistanceKm = 1.0,
            tripDistanceKm = 4.0,
            estimatedMinutes = 15
        )
        // Total Dist: 5.0 km
        // Net Earnings = Tarifa Ofertada = $24.000 (cero deducción de gasolina/comisión)
        // $/km: 24000 / 5 = $4.800/km (>= 2500)
        // $/hora: (24000 / 15) * 60 = $96.000/h (>= 26000)

        val eval = evaluateTripOfferUseCase(offer, customFuelCostPerKm = 350.0)

        assertEquals(OfferSemaphoreTier.GREEN_PROFITABLE, eval.semaphoreTier)
        assertEquals(24000.0, eval.estimatedNetEarnings, 0.01)
        assertEquals(4800.0, eval.earningsPerKm, 0.01)
        assertEquals(96000.0, eval.earningsPerHour, 0.01)
    }

    @Test
    fun testUnfavorableFarPickupReturnsRed() {
        val offer = TripOffer(
            platform = PlatformType.DIDI,
            offeredFare = 9000.0,
            pickupDistanceKm = 3.8, // Recogida excesiva > 3.0 km
            tripDistanceKm = 2.0,
            estimatedMinutes = 20
        )

        val eval = evaluateTripOfferUseCase(offer, customFuelCostPerKm = 350.0)

        assertEquals(OfferSemaphoreTier.RED_UNFAVORABLE, eval.semaphoreTier)
        assertTrue(eval.alerts.any { it.contains("Recogida lejana") })
    }
}
