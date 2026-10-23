// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/theme/LocalProfitabilityConfig.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Proveedor de CompositionLocal para los umbrales de rentabilidad del conductor.
// Permite que cualquier componente Compose re-evalúe semáforos reactivamente al guardar.
// ==============================================================================

package com.ananapps.kilometa.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.ananapps.kilometa.domain.model.HudConfig

val LocalProfitabilityConfig = staticCompositionLocalOf { HudConfig() }
