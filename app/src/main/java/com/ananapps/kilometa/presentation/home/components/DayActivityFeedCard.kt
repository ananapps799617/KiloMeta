// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/DayActivityFeedCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Feed vertical de actividades cronológicas de hoy estilo KiloMeta (carreras,
// tanqueos y gastos) con nodos visuales diferenciados por color y opción de borrado.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.DayActivityEvent
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.presentation.common.PlatformAvatar
import com.ananapps.kilometa.presentation.common.PlatformBadge
import com.ananapps.kilometa.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DayActivityFeedCard(
    events: List<DayActivityEvent>,
    onDeleteTrip: (String) -> Unit = {},
    onDeleteExpense: (String) -> Unit = {},
    onDeleteFueling: (String) -> Unit = {},
    onEditTrip: (Trip) -> Unit = {},
    onEditExpense: (Expense) -> Unit = {},
    onEditFueling: (Fueling) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    var eventPendingDeletion by remember { mutableStateOf<DayActivityEvent?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVIDADES EN RUTA HOY",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
                Text(
                    text = "${events.size} registros",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            if (events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🛣️ Tu ruta de hoy está limpia",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Toca el botón central (+) para registrar tu primera carrera, tanqueo o gasto.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    events.forEach { event ->
                        DayActivityItemRow(
                            event = event,
                            timeFormatted = timeFormatter.format(Date(event.timestamp)),
                            onEditRequested = {
                                when (event) {
                                    is DayActivityEvent.TripItem -> onEditTrip(event.trip)
                                    is DayActivityEvent.FuelingItem -> onEditFueling(event.fueling)
                                    is DayActivityEvent.ExpenseItem -> onEditExpense(event.expense)
                                }
                            },
                            onDeleteRequested = { eventPendingDeletion = event }
                        )
                    }
                }
            }
        }
    }

    if (eventPendingDeletion != null) {
        val (dialogTitle, dialogDesc) = when (eventPendingDeletion) {
            is DayActivityEvent.TripItem -> Pair("¿Eliminar Carrera?", "Esta carrera se descontará del producido de hoy y de la nube.")
            is DayActivityEvent.FuelingItem -> Pair("¿Eliminar Tanqueo?", "Este gasto de combustible se reintegrará a tu caja de hoy.")
            is DayActivityEvent.ExpenseItem -> Pair("¿Eliminar Gasto?", "Este gasto operativo se reintegrará a tu caja disponible.")
            null -> Pair("", "")
        }

        AlertDialog(
            onDismissRequest = { eventPendingDeletion = null },
            title = { Text(dialogTitle, color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(dialogDesc, color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        when (val ev = eventPendingDeletion) {
                            is DayActivityEvent.TripItem -> onDeleteTrip(ev.id)
                            is DayActivityEvent.FuelingItem -> onDeleteFueling(ev.id)
                            is DayActivityEvent.ExpenseItem -> onDeleteExpense(ev.id)
                            null -> Unit
                        }
                        eventPendingDeletion = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { eventPendingDeletion = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = DeepObsidianSurface
        )
    }
}

@Composable
private fun DayActivityItemRow(
    event: DayActivityEvent,
    timeFormatted: String,
    onEditRequested: () -> Unit,
    onDeleteRequested: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DeepObsidianSurfaceVariant)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (event is DayActivityEvent.TripItem) {
                PlatformAvatar(platform = event.trip.platformType, size = 36.dp)
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
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (event is DayActivityEvent.TripItem) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PlatformBadge(platform = event.trip.platformType)
                        Text(
                            text = "Carrera",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                } else {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
                Text(
                    text = "$timeFormatted · ${event.subtitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${if (event.isIncome) "+" else "-"} $ ${String.format(Locale.US, "%,.0f", event.amount)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = if (event.isIncome) StatusGreen else StatusRed
            )

            IconButton(onClick = onEditRequested, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(onClick = onDeleteRequested, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar",
                    tint = StatusRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
