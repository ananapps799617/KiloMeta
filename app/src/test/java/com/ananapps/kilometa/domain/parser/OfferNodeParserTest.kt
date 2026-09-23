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

    @Test
    fun testIgnoreDidiReferralBannerOnHomeScreen() {
        // Screenshot media_1790129199986.jpg
        val texts = listOf(
            "$0",
            "Parque Principal Cogua",
            "1,3X-1,9X",
            "1,5X-2,1X",
            "ZIPAQUIRÁ",
            "¡Oportunidad exclusiva para nuevos conductores!",
            "Genera $600,000 por referir a amigos por tiempo limitado",
            "Conoce más",
            "¡Nueva zona de conexión!",
            "Con 0% de tasa de servicio para que te quede más",
            "Conectarse"
        )
        val offer = parser.parse("com.didiglobal.driver", texts)
        assertNull("Pantalla de inicio/referidos de DiDi jamás debe detectarse como oferta", offer)
    }

    @Test
    fun testParseDidiOfferWithMetersAndCounterOffers() {
        // Screenshot media_1790129200131.jpg
        val texts = listOf(
            "Pon Tu Precio · Efectivo",
            "$6.763 ⚡1,7x",
            "1 punto",
            "$313 de tarifa de gasolina",
            "4,85 · 112 arrendamientos",
            "Tarjeta bancaria verificada",
            "4 min (789 m)",
            "Carrera 17 # 6B-12, Zipaquirá",
            "8 min (2,5 km)",
            "Transversal 7 #23-25, Zipaquirá",
            "Aceptar $6.763",
            "$7.263",
            "$7.563",
            "$7.763"
        )
        val offer = parser.parse("com.didiglobal.driver", texts)

        assertNotNull(offer)
        assertEquals(PlatformType.DIDI, offer?.platform)
        assertEquals(6763.0, offer?.offeredFare ?: 0.0, 0.01)
        assertEquals(0.789, offer?.pickupDistanceKm ?: 0.0, 0.001)
        assertEquals(2.5, offer?.tripDistanceKm ?: 0.0, 0.01)
        assertEquals(12, offer?.estimatedMinutes)
    }

    @Test
    fun testParseDidiOfferMislabeledAsUber() {
        // Screenshot media_1790129199891.jpg: packageName era uber pero contenido era DiDi
        val texts = listOf(
            "Efectivo",
            "$5.447 ⚡1,1x",
            "1 punto",
            "$1.232 de tarifa base dinámica",
            "$325 de tarifa de gasolina",
            "4,92 · 177 arrendamientos",
            "Perfil avanzado",
            "6 min (1,6 km)",
            "Carrera 10-97, Zipaquirá",
            "1 parada(s)",
            "9 min (2,6 km)",
            "Cl. 22b # 5-9, Zipaquirá",
            "Aceptar $5.447"
        )
        val offer = parser.parse("com.ubercab.driver", texts)

        assertNotNull(offer)
        // Debe resolver a DiDi por el contenido "arrendamientos" y formato
        assertEquals(PlatformType.DIDI, offer?.platform)
        assertEquals(5447.0, offer?.offeredFare ?: 0.0, 0.01)
        assertEquals(1.6, offer?.pickupDistanceKm ?: 0.0, 0.01)
        assertEquals(2.6, offer?.tripDistanceKm ?: 0.0, 0.01)
        assertEquals(15, offer?.estimatedMinutes)
    }

    @Test
    fun testParseDirectStandardUberTripWithAcceptButton() {
        // Viaje directo de Uber con botón de aceptación
        val texts = listOf("UberX", "COP12,500", "Calle 10 # 5-20", "4.5 km", "15 min", "Aceptar")
        val offer = parser.parse("com.ubercab.driver", texts)

        assertNotNull(offer)
        assertEquals(PlatformType.UBER, offer?.platform)
        assertEquals(12500.0, offer?.offeredFare ?: 0.0, 0.01)
        assertEquals(4.5, offer?.tripDistanceKm ?: 0.0, 0.01)
        assertEquals(15, offer?.estimatedMinutes)
    }

    @Test
    fun testParseDidiCardOfferWithoutAcceptButton() {
        // Screenshot media_1790170305529.jpg: Oferta de DiDi táctil sin botón 'Aceptar'
        val texts = listOf(
            "Pago con tarjeta",
            "$12.494 ⚡1,2x",
            "1 punto",
            "$1.604 de tarifa base dinámica",
            "$525 de tarifa de gasolina",
            "4,98 · 615 arrendamientos",
            "Tarjeta bancaria verificada",
            "7 min (1,5 km)",
            "Calle 126 #52A-60, Suba",
            "25 min (4,2 km)",
            "Torre Samsung, Usaquén"
        )
        val offer = parser.parse("com.didiglobal.driver", texts)

        assertNotNull("Oferta de DiDi táctil DEBE detectarse aunque no tenga botón Aceptar", offer)
        assertEquals(PlatformType.DIDI, offer?.platform)
        assertEquals(12494.0, offer?.offeredFare ?: 0.0, 0.01)
        assertEquals(1.5, offer?.pickupDistanceKm ?: 0.0, 0.01)
        assertEquals(4.2, offer?.tripDistanceKm ?: 0.0, 0.01)
        assertEquals(32, offer?.estimatedMinutes)
    }

    @Test
    fun testParseUberOfferWithGananciasInBottomNav() {
        // Caso exacto screenshot media_1790130763597.jpg:
        // Contiene la pestaña fija 'Ganancias' en la barra inferior, pero es una oferta real con 'Aceptar'
        val texts = listOf(
            "Mañana en 5:50 AM",
            "Viaje de 38 min (16.5 km)",
            "Uber Reserve",
            "Cra. 14 # 4-144, Cajicá",
            "Aceptar • COP37,055.00",
            "Inicio",
            "Descubrir",
            "Ganancias",
            "Bandeja de entrada",
            "Menú"
        )
        val offer = parser.parse("com.ubercab.driver", texts)

        assertNotNull("Uber con pestaña Ganancias en bottom nav DEBE detectarse si tiene botón Aceptar", offer)
        assertEquals(PlatformType.UBER, offer?.platform)
        assertEquals(37055.0, offer?.offeredFare ?: 0.0, 0.01)
        assertEquals(16.5, offer?.tripDistanceKm ?: 0.0, 0.01)
        assertEquals(38, offer?.estimatedMinutes)
    }

    @Test
    fun testRejectionForInternalMenusWithoutAcceptButton() {
        val earningsScreen = listOf("Uber", "Ganancias de la semana", "$ 219.205", "38.6 km")
        assertNull(parser.parse("com.ubercab.driver", earningsScreen))

        val walletScreen = listOf("DiDi", "Billetera", "Balance disponible", "$ 50.000", "10 km")
        assertNull(parser.parse("com.didiglobal.driver", walletScreen))
    }

    @Test
    fun testRejectionWhenNoDistancePresent() {
        val texts = listOf("UberX", "COP12,500", "Centro Comercial Plaza", "Aceptar")
        assertNull(parser.parse("com.ubercab.driver", texts))
    }
}
