// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/LiveProductionHeroCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tarjeta Hero de alta visibilidad para cabina vehicular (Copiloto en Ruta).
// Presenta con tipografía gigante de 34sp el balance neto real ("EN TU BOLSILLO HOY"),
// el desglose directo de Producido Bruto vs Combustible Real Pagado vs Gastos de Ruta,
// y el termómetro de meta diaria sin fórmulas teóricas enredadas.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.FinancialStatus
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.FuelGasoline
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: COMPOSABLE HERO DE PRODUCCIÓN EN VIVO
// ------------------------------------------------------------------------------
/**
 * Renderiza el tablero de control primario del conductor en ruta con números de alto contraste.
 */
@Composable
fun LiveProductionHeroCard(
    summary: ShiftFinancialSummary,
    status: FinancialStatus,
    dailyQuotaTarget: Double,
    modifier: Modifier = Modifier
) {
    val isQuotaMet = summary.netProfit >= dailyQuotaTarget && dailyQuotaTarget > 0.0
    val quotaProgress = if (dailyQuotaTarget > 0.0) {
        (summary.netProfit / dailyQuotaTarget).toFloat().coerceIn(0f, 1.5f)
    } else 1f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
        border = BorderStroke(
            1.5.dp,
            if (isQuotaMet) StatusGreen.copy(alpha = 0.6f) else ElectricBluePrimary.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Encabezado de Estado de Bolsillo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EN TU BOLSILLO HOY",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    ),
                    color = if (isQuotaMet) StatusGreen else ElectricBluePrimary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isQuotaMet) StatusGreen.copy(alpha = 0.2f) else StatusRed.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isQuotaMet) "¡TURNO LIBRE!" else "CUBRIENDO CUOTA",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isQuotaMet) StatusGreen else StatusRed
                    )
                }
            }

            // Cifra Rey Gigante (Legible a 70 cm en marcha)
            Text(
                text = formatCop(summary.netProfit),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = if (summary.netProfit >= 0.0) StatusGreen else StatusRed,
                letterSpacing = (-0.5).sp
            )

            // Subtexto contextual pedagógico
            Text(
                text = if (isQuotaMet) {
                    val extra = summary.netProfit - dailyQuotaTarget
                    "¡Meta superada! +${formatCop(extra)} de ganancia neta pura"
                } else {
                    val remaining = (dailyQuotaTarget - summary.netProfit).coerceAtLeast(0.0)
                    "Faltan ${formatCop(remaining)} para cubrir la cuota del día"
                },
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = if (isQuotaMet) StatusGreen else TextSecondary
            )

            // Barra horizontal de cumplimiento de meta
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { quotaProgress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (isQuotaMet) StatusGreen else ElectricBluePrimary,
                    trackColor = TextSecondary.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Meta diaria: ${formatCop(dailyQuotaTarget)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${(quotaProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isQuotaMet) StatusGreen else TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Desglose de Caja Real en Ruta (3 Columnas de Alto Contraste)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TextSecondary.copy(alpha = 0.08f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Columna 1: Producido Bruto
                Column(horizontalAlignment = Alignment.Start) {
                    Text(text = "(+) Producido", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = formatCop(summary.totalGrossIncome),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(text = "${summary.tripsCount} viajes", style = MaterialTheme.typography.labelSmall, color = ElectricBluePrimary)
                }

                // Columna 2: Combustible Real Pagado
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "(-) Combustible", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = formatCop(summary.totalFuelExpense),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = FuelGasoline
                    )
                    Text(text = "Real en caja", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }

                // Columna 3: Gastos Operativos de Ruta
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "(-) Gastos Ruta", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = formatCop(summary.totalOperationalExpenses),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = StatusRed
                    )
                    Text(text = "Peajes/Otros", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

private fun formatCop(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return String.format(Locale.GERMANY, "$%,d", rounded)
}
