// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/DayHeroCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tarjeta de cabecera financiera para el Timeline Continuo estilo KiloMeta.
// Muestra el neto en mano de hoy, producido bruto, combustible y gastos,
// junto con el indicador dinámico de la jornada (horas y km recorridos).
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.DayContinuousSummary
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun DayHeroCard(
    summary: DayContinuousSummary,
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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

            // Métrica Principal: Neto Libre en Bolsillo
            Text(
                text = "$ ${String.format(Locale.US, "%,.0f", summary.pocketNet)}",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = if (summary.pocketNet >= 0) StatusGreen else StatusRed
            )

            // Fila de Jornada Dinámica
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepObsidianSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱️ Actividad de Hoy",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ElectricBluePrimary
                    )
                    Text(
                        text = "${summary.elapsedHoursFormatted} · ${summary.distanceTraversedKm} km",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = TextPrimary
                    )
                }
            }

            HorizontalDivider(color = DeepObsidianSurfaceVariant, thickness = 1.dp)

            // Sub-métricas: Producido, Combustible, Gastos
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
