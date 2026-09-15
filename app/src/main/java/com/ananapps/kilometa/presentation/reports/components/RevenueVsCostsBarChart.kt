// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/reports/components/RevenueVsCostsBarChart.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Componente de Gráfica de Barras Comparativa nativa en Android Canvas.
// Compara el Producido Bruto frente a los Costos Totales del período seleccionado
// con barras redondeadas, márgenes de ganancia y estética Cockpit Dark.
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: CONSTANTES DE COLOR Y TOKENS VISUALES
// ------------------------------------------------------------------------------
private val BarRevenueColor = Color(0xFF00E676) // Verde Neón Producido
private val BarCostColor = Color(0xFFFF5252)    // Rojo Carmesí Costos
private val BaseTrackColor = Color(0xFF222733)  // Pista de fondo de barra

// ------------------------------------------------------------------------------
// BLOQUE 2: COMPOSABLE DE GRÁFICA DE BARRAS NATIVA
// ------------------------------------------------------------------------------
@Composable
fun RevenueVsCostsBarChart(
    grossIncome: Double,
    totalCosts: Double,
    modifier: Modifier = Modifier
) {
    val netBalance = grossIncome - totalCosts
    val profitMarginPct = if (grossIncome > 0.0) {
        ((netBalance / grossIncome) * 100.0).coerceIn(-100.0, 100.0)
    } else {
        0.0
    }

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
            // Cabecera del Gráfico
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRODUCIDO VS. COSTOS",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = ElectricBluePrimary
                )

                // Badge de Margen Neto
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (profitMarginPct >= 0) StatusGreen.copy(alpha = 0.15f) else StatusRed.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Margen: %.1f%%".format(Locale.US, profitMarginPct),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (profitMarginPct >= 0) StatusGreen else StatusRed
                    )
                }
            }

            // Leyenda de Colores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendIndicator(color = BarRevenueColor, label = "Producido Bruto")
                LegendIndicator(color = BarCostColor, label = "Costos Totales")
            }

            // Renderizado en Android Canvas de las Dos Barras
            val maxAmount = maxOf(grossIncome, totalCosts, 1.0)
            val revenueRatio = (grossIncome / maxAmount).toFloat().coerceIn(0.05f, 1f)
            val costRatio = (totalCosts / maxAmount).toFloat().coerceIn(0.05f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepObsidianBackground)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barWidth = 44.dp.toPx()
                    val spacing = canvasWidth / 3f

                    val revenueX = spacing - (barWidth / 2f)
                    val costX = (spacing * 2f) - (barWidth / 2f)

                    // Línea base del gráfico
                    drawLine(
                        color = BaseTrackColor,
                        start = Offset(0f, canvasHeight),
                        end = Offset(canvasWidth, canvasHeight),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Barra 1: Producido Bruto
                    val revenueBarHeight = canvasHeight * revenueRatio
                    drawRoundRect(
                        color = BarRevenueColor,
                        topLeft = Offset(revenueX, canvasHeight - revenueBarHeight),
                        size = Size(barWidth, revenueBarHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )

                    // Barra 2: Costos Totales
                    val costBarHeight = canvasHeight * costRatio
                    drawRoundRect(
                        color = BarCostColor,
                        topLeft = Offset(costX, canvasHeight - costBarHeight),
                        size = Size(barWidth, costBarHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }

            // Cifras Monetarias alineadas bajo las barras
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$ %,.0f".format(Locale.US, grossIncome),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = BarRevenueColor
                    )
                    Text(
                        text = "Ingreso",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "- $ %,.0f".format(Locale.US, totalCosts),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = BarCostColor
                    )
                    Text(
                        text = "Costos",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendIndicator(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
