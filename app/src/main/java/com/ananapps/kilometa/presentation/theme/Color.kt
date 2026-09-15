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
val DeepObsidianBorder: Color = ObsidianBorder

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
val ElectricBluePrimary: Color = Color(0xFF2979FF) // Azul Eléctrico Primario

// ------------------------------------------------------------------------------
// BLOQUE 4: TOKENS DE AUDITORÍA Y AHORRO BI-COMBUSTIBLE
// ------------------------------------------------------------------------------
val CyanSavings: Color = Color(0xFF00E5FF)          // Cian Ahorro GNV
val CyanSavingsContainer: Color = Color(0xFF002B33) // Fondo cian obsidiana

// ------------------------------------------------------------------------------
// BLOQUE 5: TOKENS IDENTITARIOS DE PLATAFORMAS DE TRANSPORTE
// ------------------------------------------------------------------------------
val PlatformUber: Color = Color(0xFFFFFFFF)         // Uber Blanco
val PlatformDiDi: Color = Color(0xFFFF7A00)         // DiDi Naranja
val PlatformInDrive: Color = Color(0xFFB4F309)      // InDrive Verde Lima
val PlatformPrivate: Color = Color(0xFFAB47BC)      // Particular / Privado Morado

// ------------------------------------------------------------------------------
// BLOQUE 6: TIPOGRAFÍA Y TEXTO
// ------------------------------------------------------------------------------
val TextPrimary: Color = Color(0xFFF0F6FC)
val TextSecondary: Color = Color(0xFF8B949E)
val TextTertiary: Color = Color(0xFF6E7681)
