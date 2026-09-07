// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/navigation/AppNavigation.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Sistema de navegación ergonómica inferior de KiloMeta (KiloMetaBottomBar).
// Diseñado con Touch Targets superiores a 56dp para garantizar conmutación ágil
// entre la 'Cabina en Vivo' (Cockpit) y el módulo de 'Historial & Analíticas'.
// ==============================================================================

package com.ananapps.kilometa.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.presentation.theme.CyanSavings
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.ObsidianBorder
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

// ------------------------------------------------------------------------------
// BLOQUE 1: DESTINOS CANÓNICOS DE LA APLICACIÓN
// ------------------------------------------------------------------------------
/**
 * Pantallas principales accesibles desde la barra ergonómica de cabina.
 */
enum class AppDestination(
    val title: String,
    val subtitle: String
) {
    COCKPIT("Cabina en Vivo", "En Turno"),
    HISTORY("Historial & Analíticas", "Auditoría")
}

// ------------------------------------------------------------------------------
// BLOQUE 2: BARRA DE NAVEGACIÓN INFERIOR ERGONÓMICA (TOUCH TARGET >= 56DP)
// ------------------------------------------------------------------------------
/**
 * Barra inferior persistente de alta accesibilidad para conductores en ruta.
 *
 * @param currentDestination Pantalla activa actualmente en visualización.
 * @param onNavigate Callback invocado al seleccionar un destino.
 * @param modifier Modificador Compose.
 */
@Composable
fun KiloMetaBottomBar(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(Constants.MIN_TOUCH_TARGET_DP.dp + 16.dp),
        color = DeepObsidianSurface,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppDestination.entries.forEach { destination ->
                val isSelected = destination == currentDestination

                val activeColor = when (destination) {
                    AppDestination.COCKPIT -> StatusGreen
                    AppDestination.HISTORY -> CyanSavings
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(Constants.MIN_TOUCH_TARGET_DP.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) DeepObsidianSurface.copy(alpha = 0.5f) else DeepObsidianSurface
                        )
                        .clickable { onNavigate(destination) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = destination.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) activeColor else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                        Text(
                            text = destination.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) TextPrimary else TextSecondary.copy(alpha = 0.7f),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
