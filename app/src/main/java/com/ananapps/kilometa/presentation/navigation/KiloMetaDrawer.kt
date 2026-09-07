// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/navigation/KiloMetaDrawer.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Menú lateral (ModalNavigationDrawer) para navegación rápida en cabina.
// Incluye encabezado vehicular con logo institucional, correo, placa, badge de propulsión,
// accesos directos táctiles (>= 56dp) y confirmación segura de cierre de sesión.
// ==============================================================================

package com.ananapps.kilometa.presentation.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.R
import com.ananapps.kilometa.presentation.theme.CyanSavings
import com.ananapps.kilometa.presentation.theme.CyanSavingsContainer
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.ObsidianBorder
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

// ------------------------------------------------------------------------------
// BLOQUE 1: CONTENIDO DEL MENÚ LATERAL (DRAWER)
// ------------------------------------------------------------------------------
@Composable
fun KiloMetaDrawerContent(
    currentDestination: AppDestination,
    driverEmail: String,
    vehiclePlate: String,
    propulsionName: String,
    onSelectDestination: (AppDestination) -> Unit,
    onSignOut: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showSignOutConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(310.dp)
            .background(DeepObsidianSurface)
            .padding(16.dp)
    ) {
        // Encabezado del Perfil y Vehículo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo KiloMeta",
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Column {
                Text(
                    text = "KiloMeta Cabina",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = driverEmail.ifBlank { "Conductor Registrado" },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Badge Placa
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElectricBluePrimary.copy(alpha = 0.2f))
                            .border(1.dp, ElectricBluePrimary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = vehiclePlate.ifBlank { "WMB-789" },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricBluePrimary
                        )
                    }
                    // Badge Propulsión
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyanSavingsContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = propulsionName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CyanSavings
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = ObsidianBorder, modifier = Modifier.padding(vertical = 12.dp))

        // Lista de Destinos con Touch Target >= 56dp
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppDestination.entries.forEach { destination ->
                val isSelected = destination == currentDestination
                DrawerItem(
                    destination = destination,
                    isSelected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectDestination(destination)
                        onCloseDrawer()
                    }
                )
            }
        }

        HorizontalDivider(color = ObsidianBorder, modifier = Modifier.padding(vertical = 12.dp))

        // Botón Inferior: Cerrar Sesión
        Button(
            onClick = { showSignOutConfirm = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepObsidianSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
        ) {
            Text(
                text = "🚪 Cerrar Sesión",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = StatusRed
            )
        }
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Cerrar Sesión de Cabina") },
            text = { Text("¿Deseas salir de KiloMeta? Tus datos quedan resguardados localmente y en Firestore.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        onCloseDrawer()
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Sí, Salir", color = TextPrimary)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSignOutConfirm = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = DeepObsidianSurface
        )
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: ITEM ERGONÓMICO DEL MENÚ LATERAL (TOUCH TARGET >= 56DP)
// ------------------------------------------------------------------------------
@Composable
private fun DrawerItem(
    destination: AppDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (destination) {
        AppDestination.COCKPIT -> "⚡"
        AppDestination.HISTORY -> "📊"
        AppDestination.PROFILE -> "🚗"
        AppDestination.BACKUP -> "☁️"
    }

    val activeColor = when (destination) {
        AppDestination.COCKPIT -> StatusGreen
        AppDestination.HISTORY -> CyanSavings
        AppDestination.PROFILE -> ElectricBluePrimary
        AppDestination.BACKUP -> Color(0xFFB388FF)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) activeColor.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = icon, style = MaterialTheme.typography.titleMedium)
            Column {
                Text(
                    text = destination.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) activeColor else TextPrimary
                )
                Text(
                    text = destination.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}
