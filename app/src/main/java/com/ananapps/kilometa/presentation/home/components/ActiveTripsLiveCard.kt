// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/ActiveTripsLiveCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Feed reactivo de carreras del turno activo con badges oficiales por plataforma,
// hora, método de cobro y eliminación vehicular protegida por diálogo modal.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActiveTripsLiveCard(
    trips: List<Trip>,
    onDeleteTrip: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var tripPendingDeletion by remember { mutableStateOf<Trip?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CARRERAS DEL TURNO",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
                Text(
                    text = "${trips.size} servicios",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
            }

            if (trips.isEmpty()) {
                EmptyTripsState()
            } else {
                trips.forEach { trip ->
                    ActiveTripItemRow(trip = trip, onDeleteClick = { tripPendingDeletion = trip })
                }
            }
        }
    }

    tripPendingDeletion?.let { trip ->
        AlertDialog(
            onDismissRequest = { tripPendingDeletion = null },
            title = { Text(text = "Eliminar Carrera", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "¿Deseas eliminar la carrera de ${formatCop(trip.grossAmount)} en " +
                            "${trip.platformType.displayName} de las ${formatTime(trip.timestamp)}?\n\n" +
                            "Se descontará del Producido y se recalcularán los totales de la jornada.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTrip(trip.id)
                        tripPendingDeletion = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = Color.White)
                ) { Text(text = "Eliminar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { tripPendingDeletion = null }) { Text(text = "Cancelar") }
            }
        )
    }
}

@Composable
private fun ActiveTripItemRow(trip: Trip, onDeleteClick: () -> Unit) {
    val (badgeBg, badgeText) = getPlatformColors(trip.platformType)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TextSecondary.copy(alpha = 0.06f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(badgeBg).padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = trip.platformType.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = badgeText
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = formatTime(trip.timestamp), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                val isCash = trip.paymentDestination == PaymentDestination.CASH_IN_HAND
                Text(
                    text = if (isCash) "💵 Efectivo" else "💳 App",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = if (isCash) StatusGreen else ElectricBluePrimary
                )
            }

            if (trip.commissionAmount > 0.0) {
                Text(
                    text = "Bruto: ${formatCop(trip.grossAmount)} • Com: -${formatCop(trip.commissionAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatCop(trip.netAmount),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = StatusGreen
            )
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar carrera",
                    tint = StatusRed.copy(alpha = 0.75f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyTripsState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = TextSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = "Sin carreras registradas aún en este turno.\nToca el botón ➕ para registrar tu primer servicio.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

private fun getPlatformColors(platform: PlatformType): Pair<Color, Color> {
    return when (platform) {
        PlatformType.UBER -> Color(0xFF1E1E1E) to Color.White
        PlatformType.DIDI -> Color(0xFFFF7A00) to Color.White
        PlatformType.INDRIVE -> Color(0xFFB4F309) to Color.Black
        PlatformType.PRIVATE -> ElectricBluePrimary to Color.White
    }
}

private fun formatTime(millis: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(millis))
}

private fun formatCop(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return String.format(Locale.GERMANY, "$%,d", rounded)
}
