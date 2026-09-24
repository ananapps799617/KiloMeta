// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/parser/DidiReceiptParserAdvancedTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pruebas unitarias avanzadas para DidiReceiptParser bajo fragmentación de bloques
// y saltos de línea de Google ML Kit (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.domain.parser

import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import org.junit.Assert.*
import org.junit.Test

class DidiReceiptParserAdvancedTest {

    @Test
    fun parsePromoSubsidyTrip_danielaCase_withMlKitBlockSeparation() {
        val ocrText = """
            < Tus solicitudes
            Tus ganancias
            Duración
            Distancia
            Tipo de vehículo
            Método de pago
            Efectivo recibido
            23m17s
            7,3 km
            DiDi Confort
            Bancolombia
            ${'$'}16.720
            ${'$'}19.019
            Tus ganancias
            Total ${'$'}19.019
            Pagado por el arrendatario
            Se pagó por la solic.
            Tarifa de la solic.
            Descuento del usuario
            Total
            ${'$'}16.720
            ${'$'}20.900
            -${'$'}4.180
            ${'$'}16.720
            Recibido por DiDi
            Tarifa servicio estimada
            Recarga de DiDi
            Total
            -${'$'}1.881
            ${'$'}4.180
            ${'$'}2.299
            Detalles
            daniela
            23/09/2026, 12:31:26 pm
        """.trimIndent()

        val receipt = DidiReceiptParser.parse(ocrText)
        assertNotNull(receipt)
        receipt!!

        assertEquals(PlatformType.DIDI, receipt.platform)
        assertEquals(19019.0, receipt.netAmount, 0.01)
        assertEquals(16720.0, receipt.grossAmount, 0.01)
        assertEquals(0.0, receipt.commissionAmount, 0.01)
        assertEquals(2299.0, receipt.platformPromoDiscount, 0.01)
        assertEquals(PaymentMethod.NEQUI, receipt.paymentMethod)
        assertEquals(7.3, receipt.distanceKm ?: 0.0, 0.01)
        assertEquals(23, receipt.durationMinutes)
        assertEquals("daniela", receipt.passengerName)
    }

    @Test
    fun parseStandardCashTrip_withMlKitNewlines_extractsCorrectly() {
        val ocrText = """
            < Tus solicitudes
            KM ${'$'}7.966
            Detalles
            Mery
            23/09/2026, 10:32:35 am
            Total
            ${'$'}11.000
            Tus ganancias
            Total
            ${'$'}7.966
            Recibido por
            DiDi
            Total
            -${'$'}3.034
            Pagado por
            el arrendatario
            Total
            ${'$'}11.000
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
    }
}
