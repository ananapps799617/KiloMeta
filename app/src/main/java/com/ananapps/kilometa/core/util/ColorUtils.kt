package com.ananapps.kilometa.core.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Utilidad pura en Kotlin para el procesamiento seguro de colores hexadecimales
 * y contraste ergonómico en cabina vehicular (Ecosistema AnAnApps v2.0).
 * Cero dependencias de android.graphics.* para testing JVM puro e instantáneo.
 */
object ColorUtils {

    private val DefaultFallbackColor: Color = Color(0xFF2979FF)

    /**
     * Parsea un string hexadecimal a [Color] de Jetpack Compose de forma 100% tolerante a fallos.
     * Soporta formatos: "#RRGGBB", "RRGGBB", "#AARRGGBB", "AARRGGBB".
     * Ante cualquier anomalía, string corrupto o nulo, retorna [fallbackColor] sin lanzar excepciones.
     */
    fun parseHexColor(hexString: String?, fallbackColor: Color = DefaultFallbackColor): Color {
        if (hexString.isNullOrBlank()) return fallbackColor
        val clean = hexString.trim().removePrefix("#")
        return try {
            when (clean.length) {
                6 -> {
                    val rgbLong = clean.toLong(radix = 16)
                    Color(color = 0xFF000000L or rgbLong)
                }
                8 -> {
                    val argbLong = clean.toLong(radix = 16)
                    Color(color = argbLong)
                }
                else -> fallbackColor
            }
        } catch (_: NumberFormatException) {
            fallbackColor
        }
    }

    /**
     * Determina el color de contraste óptimo (blanco u oscuro) según la luminancia WCAG 2.1.
     * Si la luminancia es > 0.45f, requiere texto oscuro (#121212) para certificar WCAG AAA.
     */
    fun getContrastingContentColor(backgroundColor: Color): Color {
        return if (backgroundColor.luminance() > 0.45f) Color(0xFF121212) else Color.White
    }
}
