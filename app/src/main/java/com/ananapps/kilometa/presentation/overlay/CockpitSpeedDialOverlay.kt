// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/overlay/CockpitSpeedDialOverlay.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Menú flotante desplegado al tocar la burbuja en reposo (Orden #047).
// Permite registrar Carreras, Tanqueos, Gastos o Talleres sin salir de Waze o Uber.
// ==============================================================================

package com.ananapps.kilometa.presentation.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun CockpitSpeedDialOverlay(
    onSelectTrip: () -> Unit,
    onSelectFueling: () -> Unit,
    onSelectExpense: () -> Unit,
    onSelectMaintenance: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(280.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, StatusGreen, RoundedCornerShape(20.dp)),
        color = DeepObsidianSurface,
        shadowElevation = 14.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ ACCESO RÁPIDO",
                    color = StatusGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C2C2E))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            QuickActionButton(
                icon = Icons.Default.LocationOn,
                label = "Registrar Carrera (+)",
                color = StatusGreen,
                onClick = onSelectTrip
            )

            QuickActionButton(
                icon = Icons.Default.Refresh,
                label = "Registrar Tanqueo (-)",
                color = StatusAmber,
                onClick = onSelectFueling
            )

            QuickActionButton(
                icon = Icons.Default.ShoppingCart,
                label = "Registrar Gasto (-)",
                color = StatusRed,
                onClick = onSelectExpense
            )

            QuickActionButton(
                icon = Icons.Default.Build,
                label = "Taller / Mantenimiento",
                color = Color(0xFF64B5F6),
                onClick = onSelectMaintenance
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color(0xFF1C2230),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(text = label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
