// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/QuickActionSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modal de acciones rápidas (Speed Dial) desplegado al tocar el botón central (+)
// de la barra de navegación. Permite registrar en 1 tap Carreras, Tanqueos o Gastos.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSelectTrip: () -> Unit,
    onSelectFueling: () -> Unit,
    onSelectExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidianSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "¿Qué deseas registrar?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            QuickActionOption(
                icon = Icons.Default.LocationOn,
                iconBg = StatusGreen,
                iconTint = Color.Black,
                title = "Registrar Carrera",
                subtitle = "Uber, DiDi, InDrive o Particular",
                onClick = {
                    onDismiss()
                    onSelectTrip()
                }
            )

            QuickActionOption(
                icon = Icons.Default.Refresh,
                iconBg = FuelGasoline,
                iconTint = Color.Black,
                title = "Registrar Tanqueo",
                subtitle = "Gas GNV o Gasolina (precio y estación)",
                onClick = {
                    onDismiss()
                    onSelectFueling()
                }
            )

            QuickActionOption(
                icon = Icons.Default.ShoppingCart,
                iconBg = StatusRed,
                iconTint = Color.White,
                title = "Registrar Gasto",
                subtitle = "Alimentación, peajes, parqueadero o lavada",
                onClick = {
                    onDismiss()
                    onSelectExpense()
                }
            )
        }
    }
}

@Composable
private fun QuickActionOption(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = DeepObsidianBackground,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
