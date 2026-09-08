// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/navigation/KiloMetaTopBar.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Barra superior canónica (TopAppBar) de KiloMeta con botón de Menú Hamburguesa,
// título institucional y badge de telemetría de nube (CockpitCloudBadge).
// ==============================================================================

package com.ananapps.kilometa.presentation.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.SyncStatus
import com.ananapps.kilometa.domain.model.UserSession
import com.ananapps.kilometa.presentation.common.CockpitCloudBadge
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.StatusRedContainer
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

// ------------------------------------------------------------------------------
// BLOQUE 1: TOP APP BAR CANÓNICA CON MENÚ HAMBURGUESA Y CIERRE DE TURNO
// ------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiloMetaTopBar(
    currentDestination: AppDestination,
    syncStatus: SyncStatus,
    userSession: UserSession?,
    isShiftActive: Boolean = false,
    onCloseShift: (() -> Unit)? = null,
    onOpenDrawer: () -> Unit,
    onOpenSyncSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    TopAppBar(
        title = {
            Column {
                Text(
                    text = "KiloMeta",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = currentDestination.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricBluePrimary
                )
            }
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(start = 12.dp, end = 6.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenDrawer()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "☰",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        },
        actions = {
            if (isShiftActive && onCloseShift != null) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCloseShift()
                    },
                    border = BorderStroke(1.dp, StatusRed),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = StatusRedContainer.copy(alpha = 0.6f),
                        contentColor = StatusRed
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "⏹ Cerrar Turno",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = StatusRed
                    )
                }
            }
            CockpitCloudBadge(
                syncStatus = syncStatus,
                userSession = userSession,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenSyncSheet()
                },
                modifier = Modifier.padding(end = 12.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepObsidianSurface),
        modifier = modifier
    )
}
