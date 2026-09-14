// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/reports/components/ExpensesDonutChartCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Gráfico de dona (Donut Chart) 100% nativo en Compose Canvas para la auditoría
// de costos operativos de cabina (Alquiler, Combustible, Mantenimiento y Otros).
// ==============================================================================

package com.ananapps.kilometa.presentation.reports.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

data class ExpenseDonutSlice(
    val label: String,
    val amount: Double,
    val color: Color
)

@Composable
fun ExpensesDonutChartCard(
    slices: List<ExpenseDonutSlice>,
    modifier: Modifier = Modifier
) {
    val totalCost = slices.sumOf { it.amount }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "DISTRIBUCIÓN DE COSTOS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            if (totalCost <= 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Sin costos registrados en este período", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // 1. Dona Gráfica en Canvas Nativo
                    Box(
                        modifier = Modifier.size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(110.dp)) {
                            var startAngle = -90f
                            val strokeWidth = 20.dp.toPx()

                            slices.forEach { slice ->
                                val sweepAngle = ((slice.amount / totalCost) * 360f).toFloat()
                                if (sweepAngle > 0f) {
                                    drawArc(
                                        color = slice.color,
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle - 2f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                        size = Size(size.width, size.height)
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "TOTAL", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)
                            Text(
                                text = "$ ${String.format(Locale.US, "%,.0f", totalCost)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // 2. Leyenda con Porcentajes y Montos
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        slices.filter { it.amount > 0 }.forEach { slice ->
                            val pct = (slice.amount / totalCost) * 100.0
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(slice.color)
                                )
                                Column {
                                    Text(
                                        text = "${slice.label} (${String.format(Locale.US, "%.1f", pct)}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "$ ${String.format(Locale.US, "%,.0f", slice.amount)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
