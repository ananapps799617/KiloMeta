// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/theme/Color.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Sistema de tokens de color para la interfaz de cabina 'Deep Obsidian'.
// Diseñado para máximo contraste, legibilidad bajo luz solar y fatiga visual nula de noche.
// ==============================================================================

package com.ananapps.kilometa.presentation.theme

import androidx.compose.ui.graphics.Color

// ------------------------------------------------------------------------------
// BLOQUE 1: SUPERFICIES Y FONDOS OBSIDIAN
// ------------------------------------------------------------------------------
val DeepObsidianBackground: Color = Color(0xFF0D1117)
val DeepObsidianSurface: Color = Color(0xFF161B22)
val DeepObsidianSurfaceVariant: Color = Color(0xFF21262D)
val ObsidianBorder: Color = Color(0xFF30363D)

// ------------------------------------------------------------------------------
// BLOQUE 2: TOKENS DE SEMÁFORO FINANCIERO (ALTO CONTRASTE)
// ------------------------------------------------------------------------------
val StatusRed: Color = Color(0xFFFF4D4D)       // Déficit / Alerta
val StatusAmber: Color = Color(0xFFFFB800)     // En Ruta / Precaución
val StatusGreen: Color = Color(0xFF00E676)     // Meta Cumplida / Ganancia Activa

val StatusRedContainer: Color = Color(0xFF3B1215)
val StatusAmberContainer: Color = Color(0xFF3B2D05)
val StatusGreenContainer: Color = Color(0xFF0B331E)

// ------------------------------------------------------------------------------
// BLOQUE 3: TOKENS DE COMBUSTIBLE Y OPERACIÓN
// ------------------------------------------------------------------------------
val FuelGas: Color = Color(0xFF00B0FF)         // GNV (Gas Natural Vehicular)
val FuelGasoline: Color = Color(0xFFFF9100)    // Gasolina Corriente

// ------------------------------------------------------------------------------
// BLOQUE 4: TIPOGRAFÍA Y TEXTO
// ------------------------------------------------------------------------------
val TextPrimary: Color = Color(0xFFF0F6FC)
val TextSecondary: Color = Color(0xFF8B949E)
val TextTertiary: Color = Color(0xFF6E7681)
