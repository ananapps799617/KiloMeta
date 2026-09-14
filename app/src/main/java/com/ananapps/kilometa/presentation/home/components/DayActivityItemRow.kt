// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/DayActivityItemRow.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Fila de actividad individual para el Feed de Hoy con arquitectura ergonómica en 2 niveles.
// Nivel 1: Avatar, Identidad de servicio y Cifra monetaria neta.
// Nivel 2: Telemetría de ruta completa (sin truncamientos) y controles táctiles de cabina.
// Regla inquebrantable: Modularidad estricta < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.DayActivityEvent
import com.ananapps.kilometa.presentation.common.PlatformAvatar
import com.ananapps.kilometa.presentation.common.PlatformBadge
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun DayActivityItemRow(
    event: DayActivityEvent,
    timeFormatted: String,
    onEditRequested: () -> Unit,
    onDeleteRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DeepObsidianSurfaceVariant)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- NIVEL 1: IDENTIDAD VISUAL Y FINANZAS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (event is DayActivityEvent.TripItem) {
                    PlatformAvatar(platform = event.trip.platformType, size = 36.dp)
                    PlatformBadge(platform = event.trip.platformType)
                } else {
                    val (nodeColor, iconEmoji) = when (event) {
                        is DayActivityEvent.FuelingItem -> Pair(FuelGasoline, "⛽")
                        is DayActivityEvent.ExpenseItem -> Pair(StatusRed, "🔧")
                        else -> Pair(StatusGreen, "🟢")
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(nodeColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = iconEmoji, fontSize = 16.sp)
                    }
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = "${if (event.isIncome) "+" else "-"} $ ${String.format(Locale.US, "%,.0f", event.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = if (event.isIncome) StatusGreen else StatusRed
            )
        }

        // --- NIVEL 2: TELEMETRÍA DE RUTA Y CONTROLES ERGONÓMICOS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text(
                    text = "⏰ $timeFormatted · ${event.subtitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onEditRequested,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteRequested,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Borrar",
                        tint = StatusRed.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
