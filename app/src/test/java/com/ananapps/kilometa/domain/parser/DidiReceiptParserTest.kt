// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/parser/DidiReceiptParserTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para DidiReceiptParser.
// Valida viajes reales con comisión estándar y con subsidio/promoción a favor (Daniela).
// ==============================================================================

package com.ananapps.kilometa.domain.parser

import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import org.junit.Assert.*
import org.junit.Test

class DidiReceiptParserTest {

    @Test
    fun parseStandardCashTrip_extractsNetGrossCommissionAndTelemetry() {
        val ocrText = """
            < Tus solicitudes
            ${'$'}7.966
            i Tus ganancias finales se han ajustado...
            Detalles
            Mery
            23/09/2026, 10:32:35 am
            Didi Confort
            Total
            ${'$'}11.000
            Efectivo recibido
            ${'$'}11.000
            Tus ganancias
            Total
            ${'$'}7.966
            Recibido por DiDi
            Total
            -${'$'}3.034
            Distancia
            6,1 km
            Duración
            25m23s
        """.trimIndent()

        val receipt = DidiReceiptParser.parse(ocrText)
        assertNotNull(receipt)
        receipt!!

        assertEquals(PlatformType.DIDI, receipt.platform)
        assertEquals(7966.0, receipt.netAmount, 0.01)
        assertEquals(11000.0, receipt.grossAmount, 0.01)
        assertEquals(3034.0, receipt.commissionAmount, 0.01)
        assertEquals(0.0, receipt.platformPromoDiscount, 0.01)
        assertEquals(PaymentMethod.CASH, receipt.paymentMethod)
        assertEquals(PaymentDestination.CASH_IN_HAND, receipt.paymentDestination)
        assertEquals(6.1, receipt.distanceKm ?: 0.0, 0.01)
        assertEquals(25, receipt.durationMinutes)
        assertEquals("Mery", receipt.passengerName)
    }

    @Test
    fun parsePromoSubsidyTrip_danielaCase_extractsPositiveWalletSubsidy() {
        val ocrText = """
            < Tus solicitudes
            Tus ganancias
            ${'$'}19.019
            Duración
            23m17s
            Distancia
            7,3 km
            Tipo de vehículo DiDi Confort
            Método de pago Bancolombia
            Efectivo recibido
            ${'$'}16.720
            Tus ganancias
            Tarifa por distancia ${'$'}7.599
            Tarifa por tiempo ${'$'}3.234
            Tarifa base ${'$'}1.482
            Precio diferencial ${'$'}4.926
            Reembolso tar. de serv. ${'$'}878
            Monto adicional por costo de combustible ${'$'}900
            Total ${'$'}19.019
            Pagado por el arrendatario
            Se pagó por la solic. ${'$'}16.720
            Tarifa de la solic. ${'$'}20.900
            Descuento del usuario -${'$'}4.180
            Total ${'$'}16.720
            Recibido por DiDi
            Tarifa servicio estimada -${'$'}1.881
            Recarga de DiDi ${'$'}4.180
            Total ${'$'}2.299
            Detalles
            daniela
            23/09/2026, 12:31:26 pm
        """.trimIndent()

        val receipt = DidiReceiptParser.parse(ocrText)
        assertNotNull(receipt)
        receipt!!

        assertEquals(PlatformType.DIDI, receipt.platform)
        assertEquals(19019.0, receipt.netAmount, 0.01)
        assertEquals(19019.0, receipt.grossAmount, 0.01)
        assertEquals(0.0, receipt.commissionAmount, 0.01)
        assertEquals(2299.0, receipt.platformPromoDiscount, 0.01)
        assertEquals(PaymentMethod.NEQUI, receipt.paymentMethod)
        assertEquals(7.3, receipt.distanceKm ?: 0.0, 0.01)
        assertEquals(23, receipt.durationMinutes)
        assertEquals("daniela", receipt.passengerName)
        assertTrue(receipt.confidenceSummary?.contains("+") == true)
    }

    @Test
    fun parseCardTrip_andresCase_extractsDigitalWalletAndTelemetry() {
        val ocrText = """
            3:45 ...
            < Tus solicitudes
            Tus ganancias
            ${'$'}18.000
            Duración
            28m50s
            Distancia
            10,1 km
            Tipo de vehículo Más Rápido
            Método de pago Pago con tarjeta
            Tus ganancias
            Tarifa ${'$'}12.129
            Tarifa de Más Rápido ${'$'}3.639
            Reembolso tar. de serv. ${'$'}857
            Monto adicional por costo de combustible ${'$'}1.375
            Total ${'$'}18.000
            Pagado por el arrendatario
            Se pagó por la solic. ${'$'}24.000
            Tarifa de la solic. ${'$'}24.000
            Total ${'$'}24.000
            Recibido por DiDi
            Tarifa servicio estimada -${'$'}6.000
            Total -${'$'}6.000
            Detalles
            Andrés
            21/09/2026, 1:34:09 pm
            Cl. 132 , Bogotá, Suba
            Chapinero, Bogotá, Suba
        """.trimIndent()

        val receipt = DidiReceiptParser.parse(ocrText)
        assertNotNull(receipt)
        receipt!!

        assertEquals(PlatformType.DIDI, receipt.platform)
        assertEquals(18000.0, receipt.netAmount, 0.01)
        assertEquals(24000.0, receipt.grossAmount, 0.01)
        assertEquals(6000.0, receipt.commissionAmount, 0.01)
        assertEquals(0.0, receipt.platformPromoDiscount, 0.01)
        assertEquals(PaymentMethod.APP_WALLET, receipt.paymentMethod)
        assertEquals(PaymentDestination.APP_RETAINED, receipt.paymentDestination)
        assertEquals(10.1, receipt.distanceKm ?: 0.0, 0.01)
        assertEquals(29, receipt.durationMinutes) // 28m 50s redondeado a 29 min (50s >= 30s)
        assertEquals("Andrés", receipt.passengerName)
        assertTrue(receipt.confidenceSummary?.contains("Tarjeta") == true)
    }

    @Test
    fun parseCardTrip_andresCase_withUnicodeNonBreakingSpacesAndIcons() {
        // Simula la salida real de Google ML Kit con caracteres especiales y espacios no separables (\u00A0)
        val ocrText = "3:45 ...\n" +
                "< Tus solicitudes\n" +
                "KM\u00A0\u00A0$18.000\n" +
                "Duración 28m50s   Distancia 10,1 km\n" +
                "Tipo de vehículo Más Rápido\n" +
                "Método de pago 💳 Pago con tarjeta\n" +
                "Tus ganancias\n" +
                "Total\u00A0\u00A0$18.000\n" +
                "Pagado por el arrendatario\n" +
                "Se pagó por la solic.\u00A0\u00A0$24.000\n" +
                "Total 💳 $24.000\n" +
                "Recibido por DiDi\n" +
                "Tarifa servicio estimada -$6.000\n" +
                "Total -$6.000\n" +
                "Detalles\n" +
                "Andrés\n" +
                "21/09/2026, 1:34:09 pm"

        val receipt = DidiReceiptParser.parse(ocrText)
        assertNotNull(receipt)
        receipt!!

        assertEquals(PlatformType.DIDI, receipt.platform)
        assertEquals(18000.0, receipt.netAmount, 0.01)
        assertEquals(24000.0, receipt.grossAmount, 0.01)
        assertEquals(6000.0, receipt.commissionAmount, 0.01)
        assertEquals(PaymentMethod.APP_WALLET, receipt.paymentMethod)
        assertEquals(10.1, receipt.distanceKm ?: 0.0, 0.01)
        assertEquals(29, receipt.durationMinutes)
        assertEquals("Andrés", receipt.passengerName)
    }
}

