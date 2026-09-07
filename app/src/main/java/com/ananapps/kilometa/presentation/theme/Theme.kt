// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/theme/Theme.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tema principal de KiloMeta con política 'Dark Theme First'.
// Forzado a modo oscuro para maximizar la duración de batería OLED y ergonomía visual
// en turnos prolongados de conducción diurna y nocturna.
// ==============================================================================

package com.ananapps.kilometa.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ------------------------------------------------------------------------------
// BLOQUE 1: ESQUEMA DE COLOR OBSIDIAN DE CABINA
// ------------------------------------------------------------------------------
private val KiloMetaDarkColorScheme = darkColorScheme(
    primary = StatusGreen,
    onPrimary = DeepObsidianBackground,
    primaryContainer = StatusGreenContainer,
    onPrimaryContainer = StatusGreen,
    secondary = FuelGas,
    onSecondary = DeepObsidianBackground,
    tertiary = StatusAmber,
    onTertiary = DeepObsidianBackground,
    background = DeepObsidianBackground,
    onBackground = TextPrimary,
    surface = DeepObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = DeepObsidianSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = StatusRed,
    onError = TextPrimary,
    errorContainer = StatusRedContainer,
    onErrorContainer = StatusRed
)

// ------------------------------------------------------------------------------
// BLOQUE 2: COMPOSABLE DE TEMA PRINCIPAL
// ------------------------------------------------------------------------------
/**
 * Aplica el tema visual de KiloMeta.
 * Forzado a Dark Mode permanente para garantizar consistencia en cabina de taxi/app.
 */
@Composable
fun KiloMetaTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = KiloMetaDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
