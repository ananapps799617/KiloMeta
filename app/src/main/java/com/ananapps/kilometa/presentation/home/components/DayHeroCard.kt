// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/DayHeroCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tarjeta maestra unificada de cabecera financiera para el Timeline Continuo (Orden #038).
// Consolida en una sola tarjeta:
// 1. Ganancia neta libre en bolsillo hoy (34sp).
// 2. Meta de hoy con termómetro en vivo (sin colisiones).
// 3. Métricas de rendimiento reactivo ($/km y $/hora) con aire visual.
// 4. Subtotales de operación (Producido, Combustible, Gastos Ruta).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.DayContinuousSummary
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun DayHeroCard(
    summary: DayContinuousSummary,
    vehicle: VehicleProfile,
    modifier: Modifier = Modifier
) {
    val dailyTarget = if (vehicle.workDaysWeek > 0) vehicle.quotaAmount / vehicle.workDaysWeek else 0.0
    val isDailyQuotaMet = summary.pocketNet >= dailyTarget && dailyTarget > 0.0
    val quotaProgress = if (dailyTarget > 0.0) (summary.pocketNet / dailyTarget).toFloat().coerceIn(0f, 1f) else 0f
    val remainingDaily = if (dailyTarget > summary.pocketNet) dailyTarget - summary.pocketNet else 0.0

    val hoursDecimal = if (summary.firstEventTimestamp != null && summary.lastEventTimestamp != null && summary.lastEventTimestamp >= summary.firstEventTimestamp) {
        val diffMs = summary.lastEventTimestamp - summary.firstEventTimestamp
        maxOf(diffMs.toDouble() / 3600000.0, 0.1)
    } else {
        0.0
    }
    val ratePerKm = if (summary.distanceTraversedKm > 0) summary.pocketNet / summary.distanceTraversedKm else 0.0
    val ratePerHour = if (hoursDecimal > 0.0) summary.pocketNet / hoursDecimal else 0.0

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
            // Fila de Encabezado con Indicador de Estado de Cabina
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EN TU BOLSILLO HOY",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextSecondary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (summary.totalTripsCount > 0 || summary.events.isNotEmpty()) StatusGreen.copy(alpha = 0.15f) else DeepObsidianSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (summary.totalTripsCount > 0 || summary.events.isNotEmpty()) "🟢 En Ruta" else "⚪ Cabina Lista",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (summary.totalTripsCount > 0 || summary.events.isNotEmpty()) StatusGreen else TextSecondary
                    )
                }
            }

            // Métrica Principal: Neto Libre en Bolsillo (34sp)
            Text(
                text = "$ ${String.format(Locale.US, "%,.0f", summary.pocketNet)}",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = if (summary.pocketNet >= 0) StatusGreen else StatusRed
            )

            // Fila de Meta de Hoy (Sin Colisiones)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 Meta Hoy: $ ${String.format(Locale.US, "%,.0f", dailyTarget)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = if (isDailyQuotaMet) "¡Cumplida! (Libre)" else "Faltan: $ ${String.format(Locale.US, "%,.0f", remainingDaily)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isDailyQuotaMet) StatusGreen else StatusAmber
                )
            }

            LinearProgressIndicator(
                progress = { quotaProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isDailyQuotaMet) StatusGreen else StatusAmber,
                trackColor = DeepObsidianSurfaceVariant
            )

            // Fila de Cobertura y Rendimiento Espaciosa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏱️ ${summary.elapsedHoursFormatted} · 🚗 ${summary.distanceTraversedKm} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "📈 $ ${String.format(Locale.US, "%,.0f", ratePerKm)}/km  ·  $ ${String.format(Locale.US, "%,.0f", ratePerHour)}/h",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = StatusGreen
                )
            }

            HorizontalDivider(color = DeepObsidianSurfaceVariant, thickness = 1.dp)

            // Subtotales de Hoy: PRODUCIDO, COMBUSTIBLE, GASTOS RUTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(text = "PRODUCIDO", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = "$ ${String.format(Locale.US, "%,.0f", summary.totalGrossProduced)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "COMBUSTIBLE", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = "-$ ${String.format(Locale.US, "%,.0f", summary.totalFuelSpent)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = FuelGasoline
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "GASTOS RUTA", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = "-$ ${String.format(Locale.US, "%,.0f", summary.totalExpenses)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = StatusRed
                    )
                }
            }
        }
    }
}
