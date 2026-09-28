// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/model/TripProfitabilityTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para el cálculo de rentabilidad ($/km, $/h y semáforo).
// ==============================================================================

package com.ananapps.kilometa.domain.model

import org.junit.Assert.*
import org.junit.Test

class TripProfitabilityTest {

    @Test
    fun highProfitabilityTrip_classifiesAsHighTier() {
        // Viaje Uber: $8.493 netos en 3.15 km y 13 min
        // Rate per km = 8493 / 3.15 = 2696.19 $/km (> 1800 $/km)
        // Rate per hour = (8493 / 13) * 60 = 39200 $/h (> 25000 $/h)
        val trip = Trip(
            id = "1",
            shiftId = "s1",
            platformType = PlatformType.UBER,
            grossAmount = 8493.0,
            commissionAmount = 0.0,
            netAmount = 8493.0,
            paymentMethod = PaymentMethod.APP_WALLET,
            timestamp = System.currentTimeMillis(),
            distanceKm = 3.15,
            durationMinutes = 13
        )

        assertEquals(2696.19, trip.ratePerKm ?: 0.0, 0.1)
        assertEquals(39198.46, trip.ratePerHour ?: 0.0, 1.0)
        assertEquals(TripProfitabilityTier.HIGH, trip.profitabilityTier)
    }

    @Test
    fun mediumProfitabilityTrip_classifiesAsMediumTier() {
        // Viaje: $9.000 netos en 6.0 km y 30 min
        // Rate per km = 9000 / 6.0 = 1500.0 $/km (entre 1400 y 1700)
        // Rate per hour = (9000 / 30) * 60 = 18000 $/h (entre 15000 y 25000)
        val trip = Trip(
            id = "2",
            shiftId = "s1",
            platformType = PlatformType.DIDI,
            grossAmount = 12000.0,
            commissionAmount = 3000.0,
            netAmount = 9000.0,
            paymentMethod = PaymentMethod.CASH,
            timestamp = System.currentTimeMillis(),
            distanceKm = 6.0,
            durationMinutes = 30
        )

        assertEquals(1500.0, trip.ratePerKm ?: 0.0, 0.1)
        assertEquals(18000.0, trip.ratePerHour ?: 0.0, 0.1)
        assertEquals(TripProfitabilityTier.MEDIUM, trip.profitabilityTier)
    }

    @Test
    fun lowProfitabilityTrip_classifiesAsLowTier() {
        // Viaje con bajo rendimiento por kilómetro o trancón excesivo:
        // $10.000 netos en 12 km (833 $/km)
        val trip = Trip(
            id = "3",
            shiftId = "s1",
            platformType = PlatformType.INDRIVE,
            grossAmount = 10000.0,
            commissionAmount = 0.0,
            netAmount = 10000.0,
            paymentMethod = PaymentMethod.CASH,
            timestamp = System.currentTimeMillis(),
            distanceKm = 12.0,
            durationMinutes = 40
        )

        assertEquals(833.33, trip.ratePerKm ?: 0.0, 0.1)
        assertEquals(TripProfitabilityTier.LOW, trip.profitabilityTier)
    }
}
