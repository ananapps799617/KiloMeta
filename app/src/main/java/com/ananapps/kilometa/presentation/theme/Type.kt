// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/theme/Type.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Escala tipográfica adaptada para visualización de instrumentos de cabina.
// Integra números tabulares ('tnum') para evitar saltos visuales al cambiar dígitos
// de dinero o kilometraje mientras se conduce.
// ==============================================================================

package com.ananapps.kilometa.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ------------------------------------------------------------------------------
// BLOQUE 1: CONFIGURACIÓN DE TIPOGRAFÍA DE CABINA
// ------------------------------------------------------------------------------
val Typography = Typography(
    // Titular gigante para montos del semáforo financiero (legible a distancia)
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = "tnum"
    ),
    // Titular secundario de indicadores clave (KPIs de combustible/cuota)
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = "tnum"
    ),
    // Títulos de tarjetas y secciones
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    // Cuerpo de texto principal
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Etiquetas de botones y badges de cabina
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    // Indicadores pequeños de soporte
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
