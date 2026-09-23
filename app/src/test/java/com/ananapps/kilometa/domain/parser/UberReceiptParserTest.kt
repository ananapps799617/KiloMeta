// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/parser/UberReceiptParserTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para UberReceiptParser.
// Valida que el pago final ajustado prevalezca sobre la tarifa por adelantado.
// ==============================================================================

package com.ananapps.kilometa.domain.parser

import com.ananapps.kilometa.domain.model.PlatformType
import org.junit.Assert.*
import org.junit.Test

class UberReceiptParserTest {

    @Test
    fun parseUberIncrementTrip_prioritizesHeaderOverUpfront() {
        val ocrText = """
            Detalles del viaje
            8.493 COP >
            Tu pago
            Tarifa por adelantado: 8.475 COP
            Tus ganancias: 8.493 COP
            Distancia: 3.15 km
            Duración: 13 min 25 seg
            Comfort - 23 sept 2026 · 11:20
        """.trimIndent()

        val receipt = UberReceiptParser.parse(ocrText)
        assertNotNull(receipt)
        receipt!!

        assertEquals(PlatformType.UBER, receipt.platform)
        assertEquals(8493.0, receipt.netAmount, 0.01)
        assertEquals(8493.0, receipt.grossAmount, 0.01)
        assertEquals(3.15, receipt.distanceKm ?: 0.0, 0.01)
        assertEquals(13, receipt.durationMinutes)
    }
}
