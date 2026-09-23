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
        // Platform Fee: $0.0 (tarifa neta de plataforma)
        // Fuel Cost (350/km): $1.750
        // Net Earnings: 24000 - 1750 = $22.250
        // $/km: 22250 / 5 = $4.450/km (>= 2500)
        // $/hora: (22250 / 15) * 60 = $89.000/h (>= 26000)

        val eval = evaluateTripOfferUseCase(offer, customFuelCostPerKm = 350.0)

        assertEquals(OfferSemaphoreTier.GREEN_PROFITABLE, eval.semaphoreTier)
        assertEquals(22250.0, eval.estimatedNetEarnings, 0.01)
        assertEquals(4450.0, eval.earningsPerKm, 0.01)
        assertEquals(89000.0, eval.earningsPerHour, 0.01)
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
