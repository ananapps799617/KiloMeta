// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/DayActivityFeedCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Feed vertical de "Actividad reciente" estilo Cockpit Dark (99.9% fiel a la lámina).
// Encabezado exterior limpio y tarjeta contenedora con separadores sutiles,
// avatares circulares coloreados y acordeón interactivo de edición y borrado (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.DayActivityEvent
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.Trip
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
    onDeleteTransfer: (String) -> Unit = {},
    onDeleteMaintenance: (String) -> Unit = {},
    onEditTrip: (Trip) -> Unit = {},
    onEditExpense: (Expense) -> Unit = {},
    onEditFueling: (Fueling) -> Unit = {},
    onEditMaintenance: (com.ananapps.kilometa.domain.model.MaintenanceRecord) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    var eventPendingDeletion by remember { mutableStateOf<DayActivityEvent?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Encabezado Exterior: "Actividad reciente" y "X registros / Ver todas"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Actividad reciente",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = TextPrimary
            )

            Text(
                text = if (events.isNotEmpty()) "${events.size} registros" else "0 registros",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = StatusGreen
            )
        }

        // 2. Tarjeta contenedora de actividades
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, DeepObsidianBorder, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Toca el botón central (+) para registrar tu primera carrera, tanqueo o gasto.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    var expandedEventId by remember { mutableStateOf<String?>(null) }
                    events.forEachIndexed { index, event ->
                        val isItemExpanded = expandedEventId == event.id
                        DayActivityItemRow(
                            event = event,
                            timeFormatted = timeFormatter.format(Date(event.timestamp)),
                            isExpanded = isItemExpanded,
                            onToggleExpand = {
                                expandedEventId = if (isItemExpanded) null else event.id
                            },
                            onEditRequested = {
                                when (event) {
                                    is DayActivityEvent.TripItem -> onEditTrip(event.trip)
                                    is DayActivityEvent.FuelingItem -> onEditFueling(event.fueling)
                                    is DayActivityEvent.ExpenseItem -> onEditExpense(event.expense)
                                    is DayActivityEvent.MaintenanceItem -> onEditMaintenance(event.maintenance)
                                    else -> Unit
                                }
                            },
                            onDeleteRequested = { eventPendingDeletion = event },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (index < events.size - 1) {
                            HorizontalDivider(
                                color = DeepObsidianBorder.copy(alpha = 0.6f),
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
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
            is DayActivityEvent.TransferItem -> Pair("¿Eliminar Transferencia?", "Esta transferencia inter-bolsillos se revertirá contablemente.")
            is DayActivityEvent.MaintenanceItem -> Pair("¿Eliminar Servicio?", "Este registro de mantenimiento preventivo se eliminará.")
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
                            is DayActivityEvent.TransferItem -> onDeleteTransfer(ev.id)
                            is DayActivityEvent.MaintenanceItem -> onDeleteMaintenance(ev.id)
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
