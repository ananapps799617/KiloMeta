// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/common/CockpitCloudBadge.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Indicador visual de telemetría de sincronización en la nube para la cabina.
// Proporciona retroalimentación de 4 estados cromáticos y touch target ergonómico (>=48dp).
// ==============================================================================

package com.ananapps.kilometa.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.SyncState
import com.ananapps.kilometa.domain.model.SyncStatus
import com.ananapps.kilometa.domain.model.UserSession
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ObsidianBorder
import com.ananapps.kilometa.presentation.theme.StatusAmber
import com.ananapps.kilometa.presentation.theme.StatusAmberContainer
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusGreenContainer
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.StatusRedContainer
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

// ------------------------------------------------------------------------------
// BLOQUE 1: COMPOSABLE BADGE DE SINCRONIZACIÓN CLOUD
// ------------------------------------------------------------------------------
/**
 * Badge táctil de cabina que exhibe el estado del Cloud Sync Engine.
 */
@Composable
fun CockpitCloudBadge(
    syncStatus: SyncStatus,
    userSession: UserSession?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgColor: Color, textColor: Color, borderColor: Color, iconText: String, statusText: String) = when {
        userSession == null -> {
            Quint(
                DeepObsidianSurfaceVariant,
                TextSecondary,
                ObsidianBorder,
                "☁️",
                "Local"
            )
        }
        syncStatus.state == SyncState.SYNCING -> {
            Quint(
                StatusAmberContainer,
                StatusAmber,
                StatusAmber,
                "🔄",
                "Sincronizando"
            )
        }
        syncStatus.state == SyncState.SUCCESS -> {
            Quint(
                StatusGreenContainer,
                StatusGreen,
                StatusGreen,
                "☁️✓",
                "Nube OK"
            )
        }
        syncStatus.state == SyncState.ERROR -> {
            Quint(
                StatusRedContainer,
                StatusRed,
                StatusRed,
                "⚠️",
                "Error Sync"
            )
        }
        else -> {
            Quint(
                DeepObsidianSurfaceVariant,
                TextSecondary,
                ObsidianBorder,
                "☁️",
                "Al día"
            )
        }
    }

    Surface(
        modifier = modifier
            .defaultMinSize(minWidth = 56.dp, minHeight = 48.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = iconText,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: TUPLA DE COLORES Y ETIQUETAS
// ------------------------------------------------------------------------------
private data class Quint<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
