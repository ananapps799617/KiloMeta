// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/DayActivityItemRow.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Fila de actividad individual con arquitectura de acordeón interactivo estilo Drivvo.
// Estado Colapsado: 2 filas limpias de borde a borde sin botones (cero texto truncado).
// Estado Expandido: Ficha técnica tipo tabla clave-valor con botonera protegida en esquina inferior.
// Regla inquebrantable: Modularidad estricta < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DeepObsidianSurfaceVariant)
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // --- 1. VISTA COLAPSADA: LÍNEA 1 (IDENTIDAD Y FINANZAS) ---
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
                        is DayActivityEvent.ExpenseItem -> Pair(StatusRed, "🔴")
                        is DayActivityEvent.TransferItem -> Pair(Color(0xFF7E57C2), "⇄")
                        is DayActivityEvent.MaintenanceItem -> Pair(Color(0xFF8D6E63), "🔧")
                        else -> Pair(StatusGreen, "🚗")
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

        // --- 2. VISTA COLAPSADA: LÍNEA 2 (MÉTRICAS Y HORA — BORDE A BORDE) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = event.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)
            )
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
        }

        // --- 3. PANEL DESPLEGADO: FICHA TÉCNICA CLAVE-VALOR ---
        if (expanded) {
            HorizontalDivider(
                color = DeepObsidianSurface.copy(alpha = 0.7f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            when (event) {
                is DayActivityEvent.TripItem -> {
                    val trip = event.trip
                    DetailRow("TARIFA BRUTA", "$ ${String.format(Locale.US, "%,.0f", trip.grossAmount)}")
                    DetailRow("COMISIÓN DESCONTADA", "- $ ${String.format(Locale.US, "%,.0f", trip.commissionAmount)}")
                    DetailRow("GANANCIA LIMPIA", "$ ${String.format(Locale.US, "%,.0f", trip.netAmount)}", isHighlight = true)
                    DetailRow("MEDIO DE PAGO", trip.paymentMethod.displayName)
                    if (trip.startOdometer != null || trip.endOdometer != null) {
                        val odoText = "${trip.startOdometer ?: "-"} km ➔ ${trip.endOdometer ?: "-"} km"
                        val distText = trip.tripDistanceKm?.let { " (${it.toInt()} km)" } ?: ""
                        DetailRow("ODÓMETRO", "$odoText$distText")
                    }
                    DetailRow("HORA REGISTRO", timeFormatted)
                }
                is DayActivityEvent.FuelingItem -> {
                    val fueling = event.fueling
                    val unitPrice = if (fueling.volume > 0) fueling.amountPaid / fueling.volume else 0.0
                    DetailRow("COMBUSTIBLE", fueling.fuelType.displayName)
                    DetailRow("VOLUMEN CARGADO", "${fueling.volume} ${fueling.fuelType.unitLabel}")
                    DetailRow("PRECIO UNITARIO", "$ ${String.format(Locale.US, "%,.0f", unitPrice)} / ${fueling.fuelType.unitLabel}")
                    DetailRow("TOTAL PAGADO", "$ ${String.format(Locale.US, "%,.0f", fueling.amountPaid)}")
                    DetailRow("ODÓMETRO SURTIDOR", "${fueling.odometer} km")
                    DetailRow("HORA REGISTRO", timeFormatted)
                }
                is DayActivityEvent.ExpenseItem -> {
                    val expense = event.expense
                    DetailRow("CATEGORÍA", "${expense.concept.icon} ${expense.concept.displayName}")
                    DetailRow("TOTAL GASTO", "$ ${String.format(Locale.US, "%,.0f", expense.amount)}")
                    DetailRow("HORA REGISTRO", timeFormatted)
                }
                is DayActivityEvent.TransferItem -> {
                    val transfer = event.transfer
                    DetailRow("TIPO MOVIMIENTO", transfer.transferType.displayName)
                    DetailRow("ORIGEN", transfer.sourceWallet.displayName)
                    DetailRow("DESTINO", transfer.destinationWallet.displayName)
                    DetailRow("MONTO TRANSFERIDO", "$ ${String.format(Locale.US, "%,.0f", transfer.amount)}", isHighlight = true)
                    transfer.notes?.takeIf { it.isNotBlank() }?.let { DetailRow("NOTAS", it) }
                    DetailRow("HORA REGISTRO", timeFormatted)
                }
                is DayActivityEvent.MaintenanceItem -> {
                    val m = event.maintenance
                    DetailRow("SERVICIO", m.serviceTitle ?: m.type.displayName)
                    DetailRow("TOTAL SERVICIO", "$ ${String.format(Locale.US, "%,.0f", m.costCop)}", isHighlight = true)
                    DetailRow("ODÓMETRO", "${m.odometer} km")
                    m.location?.let { DetailRow("TALLER", it) }
                    DetailRow("MEDIO DE PAGO", m.paymentSource.displayName)
                    DetailRow("HORA REGISTRO", timeFormatted)
                }
            }

            // --- 4. BOTONERA INFERIOR DERECHA PROTEGIDA (≥ 40dp) ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDeleteRequested, modifier = Modifier.size(44.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Borrar", tint = StatusRed.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
                }
                if (event !is DayActivityEvent.TransferItem && event !is DayActivityEvent.MaintenanceItem) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onEditRequested, modifier = Modifier.size(44.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = ElectricBluePrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 11.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace
            ),
            color = if (isHighlight) StatusGreen else TextPrimary,
            fontSize = 12.sp
        )
    }
}
