package com.ananapps.kilometa.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Suite de pruebas unitarias para IconCatalog (Orden #055).
 * Valida la lista blanca vehicular inmutable y las funciones defensivas de sanitización (MND-DG-01).
 */
class IconCatalogTest {

    @Test
    fun `VEHICULAR_ICONS contiene exactamente los 13 glifos tematicos aprobados`() {
        assertEquals(13, IconCatalog.VEHICULAR_ICONS.size)
        val expected = listOf("🚘", "🚕", "🛵", "📦", "💵", "💳", "🏦", "🟣", "🪙", "⛽", "🔧", "⭐", "👤")
        assertEquals(expected, IconCatalog.VEHICULAR_ICONS)
    }

    @Test
    fun `sanitizeIcon retorna el glifo valido sin alteraciones`() {
        assertEquals("🚕", IconCatalog.sanitizeIcon("🚕"))
        assertEquals("🚘", IconCatalog.sanitizeIcon("🚘"))
        assertEquals("💵", IconCatalog.sanitizeIcon("💵"))
        assertEquals("🟣", IconCatalog.sanitizeIcon("🟣"))
    }

    @Test
    fun `sanitizeIcon aplica trim antes de validar`() {
        assertEquals("🚕", IconCatalog.sanitizeIcon("  🚕  "))
    }

    @Test
    fun `sanitizeIcon retorna fallback seguro ante nulos vacios o espacios en blanco`() {
        assertEquals(IconCatalog.DEFAULT_WALLET_ICON, IconCatalog.sanitizeIcon(null))
        assertEquals(IconCatalog.DEFAULT_WALLET_ICON, IconCatalog.sanitizeIcon(""))
        assertEquals(IconCatalog.DEFAULT_WALLET_ICON, IconCatalog.sanitizeIcon("   "))
        assertEquals("🚘", IconCatalog.sanitizeIcon(null, default = "🚘"))
    }

    @Test
    fun `sanitizeIcon bloquea payloads que exceden el limite de caracteres`() {
        val longPayload = "<script>alert('xss')</script>"
        assertEquals(IconCatalog.DEFAULT_WALLET_ICON, IconCatalog.sanitizeIcon(longPayload))
    }

    @Test
    fun `sanitizeIcon rechaza emojis ajenos al catalogo vehicular`() {
        assertEquals(IconCatalog.DEFAULT_WALLET_ICON, IconCatalog.sanitizeIcon("🚀"))
        assertEquals(IconCatalog.DEFAULT_WALLET_ICON, IconCatalog.sanitizeIcon("🍕"))
        assertEquals(IconCatalog.DEFAULT_WALLET_ICON, IconCatalog.sanitizeIcon("💎"))
    }
}
