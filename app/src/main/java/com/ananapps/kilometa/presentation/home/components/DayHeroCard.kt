// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/DayHeroCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tarjeta maestra unificada de cabecera financiera para el Timeline Continuo (Orden #038).
// Consolida en una sola tarjeta:
// 1. Ganancia neta libre en bolsillo hoy (34sp).
// 2. Termómetro de meta diaria prorrateada con indicador de "Turno Libre".
// 3. Métricas de rendimiento reactivo ($/km y $/hora).
// 4. Subtotales de operación (Producido, Combustible, Gastos Ruta).
// 5. Matriz compacta 2x2 de los 4 Bolsillos Soberanos con botón [ ⇄ Transferir ].
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
import com.ananapps.kilometa.domain.model.CashSettlementSummary
import com.ananapps.kilometa.domain.model.DayContinuousSummary
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun DayHeroCard(
    summary: DayContinuousSummary,
    vehicle: VehicleProfile,
    cashSettlement: CashSettlementSummary?,
    onTransferClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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

            // Bloque 1: Termómetro de Meta Diaria Prorrateada (Orden #038)
            HeroDailyQuotaThermometer(
                pocketNet = summary.pocketNet,
                vehicle = vehicle
            )

            // Bloque 2: Telemetría de Rendimiento en Vivo ($/km y $/h)
            HeroPerformanceMetricsRow(
                pocketNet = summary.pocketNet,
                distanceKm = summary.distanceTraversedKm,
                elapsedHoursFormatted = summary.elapsedHoursFormatted,
                firstTimestamp = summary.firstEventTimestamp,
                lastTimestamp = summary.lastEventTimestamp
            )

            HorizontalDivider(color = DeepObsidianSurfaceVariant, thickness = 1.dp)

            // Bloque 3: Sub-métricas de Operación (Producido, Combustible, Gastos Ruta)
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

            HorizontalDivider(color = DeepObsidianSurfaceVariant, thickness = 1.dp)

            // Bloque 4: Matriz Compacta de los 4 Bolsillos Soberanos con botón [ ⇄ Transferir ]
            HeroFourPocketsGrid(
                cashSettlement = cashSettlement ?: summary.cashSettlement,
                onTransferClick = onTransferClick
            )
        }
    }
}
