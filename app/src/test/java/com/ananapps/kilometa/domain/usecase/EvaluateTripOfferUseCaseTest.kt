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
        // Uber Fee (25%): $6.000
        // Fuel Cost (350/km): $1.750
        // Net Earnings: 24000 - 6000 - 1750 = $16.250
        // $/km: 16250 / 5 = $3.250/km (>= 2500)
        // $/hora: (16250 / 15) * 60 = $65.000/h (>= 26000)

        val eval = evaluateTripOfferUseCase(offer, customFuelCostPerKm = 350.0)

        assertEquals(OfferSemaphoreTier.GREEN_PROFITABLE, eval.semaphoreTier)
        assertEquals(16250.0, eval.estimatedNetEarnings, 0.01)
        assertEquals(3250.0, eval.earningsPerKm, 0.01)
        assertEquals(65000.0, eval.earningsPerHour, 0.01)
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
