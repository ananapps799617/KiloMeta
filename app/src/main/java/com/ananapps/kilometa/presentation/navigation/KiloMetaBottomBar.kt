// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/navigation/KiloMetaBottomBar.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Barra de navegación inferior M3 de 5 destinos inspirada en el benchmark de KiloMeta.
// Incluye botón central flotante (+) para registro express y pestañas ergonómicas
// de pulgar para Ruta, Informes, Recordatorios y Más.
// ==============================================================================

package com.ananapps.kilometa.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import com.ananapps.kilometa.presentation.theme.StatusAmber

// ------------------------------------------------------------------------------
// BLOQUE 1: DEFINICIÓN DE DESTINOS DE LA BARRA
// ------------------------------------------------------------------------------
enum class KiloMetaNavTab(val label: String, val icon: ImageVector) {
    ROUTE("Ruta", Icons.Default.LocationOn),
    HISTORY("Historial", Icons.Default.List),
    REPORTS("Informes", Icons.Default.DateRange),
    MORE("Más", Icons.Default.MoreVert)
}

// ------------------------------------------------------------------------------
// BLOQUE 2: COMPOSABLE DE LA BARRA INFERIOR (5 DESTINOS CANÓNICOS)
// ------------------------------------------------------------------------------
@Composable
fun KiloMetaBottomBar(
    currentTab: KiloMetaNavTab,
    onTabSelected: (KiloMetaNavTab) -> Unit,
    onQuickActionClick: () -> Unit,
    hasPendingAlerts: Boolean = false,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = DeepObsidianSurface,
        tonalElevation = 8.dp
    ) {
        // Tab 1: Ruta / Cabina en Vivo de Hoy
        NavigationBarItem(
            selected = currentTab == KiloMetaNavTab.ROUTE,
            onClick = { onTabSelected(KiloMetaNavTab.ROUTE) },
            icon = { Icon(KiloMetaNavTab.ROUTE.icon, contentDescription = KiloMetaNavTab.ROUTE.label) },
            label = { Text(KiloMetaNavTab.ROUTE.label, fontWeight = FontWeight.SemiBold) },
            colors = navigationItemColors()
        )

        // Tab 2: Historial (Bitácora continua de jornadas anteriores)
        NavigationBarItem(
            selected = currentTab == KiloMetaNavTab.HISTORY,
            onClick = { onTabSelected(KiloMetaNavTab.HISTORY) },
            icon = { Icon(KiloMetaNavTab.HISTORY.icon, contentDescription = KiloMetaNavTab.HISTORY.label) },
            label = { Text(KiloMetaNavTab.HISTORY.label, fontWeight = FontWeight.SemiBold) },
            colors = navigationItemColors()
        )

        // Botón Central (+) Elevado 56dp
        Box(
            modifier = Modifier
                .weight(1f)
                .offset(y = (-4).dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(StatusGreen)
                    .clickable { onQuickActionClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Registro Express",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Tab 3: Informes (Dashboard analítico 2x2 Drivvo)
        NavigationBarItem(
            selected = currentTab == KiloMetaNavTab.REPORTS,
            onClick = { onTabSelected(KiloMetaNavTab.REPORTS) },
            icon = { Icon(KiloMetaNavTab.REPORTS.icon, contentDescription = KiloMetaNavTab.REPORTS.label) },
            label = { Text(KiloMetaNavTab.REPORTS.label, fontWeight = FontWeight.SemiBold) },
            colors = navigationItemColors()
        )

        // Tab 4: Más Opciones (con badge de alerta preventiva)
        NavigationBarItem(
            selected = currentTab == KiloMetaNavTab.MORE,
            onClick = { onTabSelected(KiloMetaNavTab.MORE) },
            icon = {
                if (hasPendingAlerts) {
                    BadgedBox(badge = { Badge(containerColor = StatusAmber) }) {
                        Icon(KiloMetaNavTab.MORE.icon, contentDescription = KiloMetaNavTab.MORE.label)
                    }
                } else {
                    Icon(KiloMetaNavTab.MORE.icon, contentDescription = KiloMetaNavTab.MORE.label)
                }
            },
            label = { Text(KiloMetaNavTab.MORE.label, fontWeight = FontWeight.SemiBold) },
            colors = navigationItemColors()
        )
    }
}

@Composable
private fun navigationItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = StatusGreen,
    selectedTextColor = StatusGreen,
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary,
    indicatorColor = DeepObsidianSurface
)
