package com.ananapps.kilometa.domain.parser

import com.ananapps.kilometa.domain.model.PlatformType
import org.junit.Assert.*
import org.junit.Test

class OfferNodeParserTest {

    private val parser = OfferNodeParser()

    @Test
    fun testParseUberTripOffer() {
        val texts = listOf(
            "UberX",
            "$ 14.500",
            "A 1.2 km de tu ubicación",
            "Viaje de 5.4 km",
            "18 min"
        )
        val offer = parser.parse("com.ubercab.driver", texts)

        assertNotNull(offer)
        assertEquals(PlatformType.UBER, offer?.platform)
        assertEquals(14500.0, offer?.offeredFare ?: 0.0, 0.01)
        assertEquals(1.2, offer?.pickupDistanceKm ?: 0.0, 0.01)
        assertEquals(5.4, offer?.tripDistanceKm ?: 0.0, 0.01)
        assertEquals(18, offer?.estimatedMinutes)
    }

    @Test
    fun testParseDidiTripOfferWithMeters() {
        val texts = listOf(
            "DiDi Express",
            "$ 8.200",
            "Recogida a 800 m",
            "3.5 km",
            "11 min"
        )
        val offer = parser.parse("com.didiglobal.driver", texts)

        assertNotNull(offer)
        assertEquals(PlatformType.DIDI, offer?.platform)
        assertEquals(8200.0, offer?.offeredFare ?: 0.0, 0.01)
        assertEquals(0.8, offer?.pickupDistanceKm ?: 0.0, 0.01)
        assertEquals(3.5, offer?.tripDistanceKm ?: 0.0, 0.01)
        assertEquals(11, offer?.estimatedMinutes)
    }

    @Test
    fun testParseInDriveTripOffer() {
        val texts = listOf(
            "inDrive",
            "Oferta de pasajero",
            "$ 22.000",
            "2.0 km",
            "7.8 km",
            "25 min"
        )
        val offer = parser.parse("sinet.startup.inDriver", texts)

        assertNotNull(offer)
        assertEquals(PlatformType.INDRIVE, offer?.platform)
        assertEquals(22000.0, offer?.offeredFare ?: 0.0, 0.01)
        assertEquals(2.0, offer?.pickupDistanceKm ?: 0.0, 0.01)
        assertEquals(7.8, offer?.tripDistanceKm ?: 0.0, 0.01)
        assertEquals(25, offer?.estimatedMinutes)
    }

    @Test
    fun testParseRealDidiOfferFromScreenshot() {
        val texts = listOf(
            "En Tu Precio · Efectivo",
            "$9.053",
            "x1.7 más que Express",
            "$463 de tarifa de gasolina",
            "4,98 · 698 arrendamientos",
            "5 min (1 km)",
            "Cl. 7d # 16-31, Zipaquirá",
            "1 parada(s)",
            "16 min (3,7 km)",
            "Centro Empresarial San Rafael, Zipaquirá",
            "Aceptar $9.053"
        )
        val offer = parser.parse("com.didiglobal.driver", texts)

        assertNotNull(offer)
        assertEquals(PlatformType.DIDI, offer?.platform)
        assertEquals(9053.0, offer?.offeredFare ?: 0.0, 0.01)
        assertEquals(1.0, offer?.pickupDistanceKm ?: 0.0, 0.01)
        assertEquals(3.7, offer?.tripDistanceKm ?: 0.0, 0.01)
        assertEquals(21, offer?.estimatedMinutes)
    }

    @Test
    fun testParseRealUberOfferFromScreenshot() {
        val texts = listOf(
            "UberYa",
            "Exclusivo",
            "COP6,011",
            "COP2,732/km (estimado)",
            "Documento de identidad oficial",
            "5.00 (52)",
            "A 2 min (0.2 km)",
            "Cl 6, Zipaquira",
            "Viaje: 10 min (2.0 km)",
            "Cra. 7 # 4-13, Zipaquira",
            "Aceptar contrato de ren..."
        )
        val offer = parser.parse("com.ubercab.driver", texts)

        assertNotNull(offer)
        assertEquals(PlatformType.UBER, offer?.platform)
        assertEquals(6011.0, offer?.offeredFare ?: 0.0, 0.01)
        assertEquals(0.2, offer?.pickupDistanceKm ?: 0.0, 0.01)
        assertEquals(2.0, offer?.tripDistanceKm ?: 0.0, 0.01)
        assertEquals(12, offer?.estimatedMinutes)
    }

    @Test
    fun testIgnoreUnauthorizedPackage() {
        val texts = listOf("$ 15.000", "5 km")
        val offer = parser.parse("com.whatsapp", texts)
        assertNull(offer)
    }
}
