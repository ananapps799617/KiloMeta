package com.ananapps.kilometa.core.util

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Suite de pruebas unitarias para ColorUtils y paleta vehicular (Orden #055).
 * Valida parsing resiliente, certificación de contraste solar WCAG AAA/AA (MND-UI-01).
 */
class ColorUtilsTest {

    @Test
    fun `OFFICIAL_VEHICULAR_PALETTE contiene exactamente 10 colores certificados`() {
        assertEquals(10, ColorUtils.OFFICIAL_VEHICULAR_PALETTE.size)
        val expected = listOf(
            "#1C1C1E", "#FF7A00", "#B4F309", "#7B1FA2", "#FFD600",
            "#FF441F", "#00E676", "#2979FF", "#FFFFFF", "#78909C"
        )
        assertEquals(expected, ColorUtils.OFFICIAL_VEHICULAR_PALETTE)
    }

    @Test
    fun `parseHexColor procesa formatos validos con y sin numeral`() {
        val c1 = ColorUtils.parseHexColor("#1C1C1E")
        assertEquals(Color(0xFF1C1C1E), c1)

        val c2 = ColorUtils.parseHexColor("1C1C1E")
        assertEquals(Color(0xFF1C1C1E), c2)

        val c3 = ColorUtils.parseHexColor("#FFB4F309")
        assertEquals(Color(0xFFB4F309), c3)
    }

    @Test
    fun `parseHexColor tolera cadenas corruptas o nulas retornando fallback`() {
        val fallback = Color(0xFF2979FF)
        assertEquals(fallback, ColorUtils.parseHexColor(null))
        assertEquals(fallback, ColorUtils.parseHexColor(""))
        assertEquals(fallback, ColorUtils.parseHexColor("   "))
        assertEquals(fallback, ColorUtils.parseHexColor("ZZZZZZ"))
        assertEquals(fallback, ColorUtils.parseHexColor("#123"))
    }

    @Test
    fun `getContrastingContentColor certifica contraste WCAG AAA para Uber e inDrive`() {
        // Uber: Negro Asfalto #1C1C1E -> Debe exigir texto Blanco #FFFFFF (WCAG AAA)
        val uberColor = ColorUtils.parseHexColor("#1C1C1E")
        assertEquals(Color.White, ColorUtils.getContrastingContentColor(uberColor))

        // inDrive: Verde Lima #B4F309 -> Debe exigir texto Oscuro #121212 (WCAG AAA)
        val inDriveColor = ColorUtils.parseHexColor("#B4F309")
        assertEquals(Color(0xFF121212), ColorUtils.getContrastingContentColor(inDriveColor))

        // Blanco Puro #FFFFFF -> Debe exigir texto Oscuro #121212
        val whiteColor = ColorUtils.parseHexColor("#FFFFFF")
        assertEquals(Color(0xFF121212), ColorUtils.getContrastingContentColor(whiteColor))
    }
}
