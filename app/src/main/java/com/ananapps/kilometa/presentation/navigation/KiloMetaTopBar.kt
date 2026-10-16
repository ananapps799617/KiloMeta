// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/navigation/KiloMetaTopBar.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Barra superior canónica de KiloMeta (Orden #060).
// Delega en CockpitHorizonTopBanner, presentando la identidad unificada
// Cockpit Horizon Skyline con textura automotriz difuminada, logotipo oficial
// KiloMeta y avatar de perfil con anillo verde esmeralda StatusGreen.
// Erradica los textos azules y elementos decorativos sin función.
// ==============================================================================

package com.ananapps.kilometa.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ananapps.kilometa.presentation.navigation.components.CockpitHorizonTopBanner

@Composable
fun KiloMetaTopBar(
    currentDestination: AppDestination = AppDestination.COCKPIT,
    onOpenDrawer: (() -> Unit)? = null,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    CockpitHorizonTopBanner(
        onNavigateToProfile = onNavigateToProfile,
        modifier = modifier
    )
}
