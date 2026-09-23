// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/ShiftItemRows.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Filas individuales para renderizar los registros atómicos internos de un turno:
// Carreras (plataforma), Tanqueos (bi-combustible) y Gastos de Ruta.
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripItemRow(
    trip: Trip,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(trip.timestamp))
    val (platColor, platLabel, textCol) = when (trip.platformType) {
        PlatformType.UBER -> Triple(PlatformUber, "UB", TextPrimary)
        PlatformType.DIDI -> Triple(PlatformDiDi, "DD", TextPrimary)
        PlatformType.INDRIVE -> Triple(PlatformInDrive, "IN", Color.Black)
        PlatformType.PRIVATE -> Triple(PlatformPrivate, "PR", TextPrimary)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(platColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(platLabel, fontSize = 12.sp, fontWeight = FontWeight.Black, color = textCol)
                }
                Column {
                    Text("${trip.platformType.displayName} · $timeStr", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    val metricsStr = buildString {
                        append("Bruto: $ %,.0f".format(Locale.US, trip.grossAmount))
                        if (trip.commissionAmount > 0) append(" | Com: -$ %,.0f".format(Locale.US, trip.commissionAmount))
                        if (trip.platformPromoDiscount > 0) append(" | Promo: +$ %,.0f".format(Locale.US, trip.platformPromoDiscount))
                        trip.tripDistanceKm?.let { d -> append(" | ${if (d % 1.0 == 0.0) d.toInt() else "%.1f".format(d)} km") }
                        trip.profitabilityTier?.let { tier -> append(" | ${tier.emoji} ${tier.displayName}") }
                    }
                    Text(metricsStr, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("$ %,.0f".format(Locale.US, trip.netAmount), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace), color = StatusGreen)
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar carrera", tint = StatusRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun FuelingItemRow(
    fueling: Fueling,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(fueling.timestamp))
    val isGas = fueling.fuelType == FuelType.GAS
    val badgeColor = if (isGas) StatusGreen else FuelGasoline
    val badgeText = if (isGas) "GNV" else "GAS"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(badgeColor.copy(alpha = 0.2f)).border(1.dp, badgeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                }
                Column {
                    Text("Tanqueo $badgeText · $timeStr", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    val vol = if (fueling.volume > 0) "%.1f %s · ".format(fueling.volume, fueling.fuelType.unitLabel) else ""
                    Text("${vol}Km: ${fueling.odometer}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("-$ %,.0f".format(Locale.US, fueling.amountPaid), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace), color = badgeColor)
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar tanqueo", tint = StatusRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ExpenseItemRow(
    expense: Expense,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(expense.timestamp))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(StatusRed.copy(alpha = 0.2f)).border(1.dp, StatusRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("EXP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StatusRed)
                }
                Column {
                    Text("${expense.concept.icon} ${expense.concept.displayName} · $timeStr", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Gasto operacional en ruta", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("-$ %,.0f".format(Locale.US, expense.amount), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace), color = StatusRed)
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar gasto", tint = StatusRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
