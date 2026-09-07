// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/CockpitKpisCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Componente de cabina que presenta el desglose consolidado de métricas operativas:
// ingreso neto, gasto de combustible bi-modal (GNV vs Gasolina), costo unitario por kilómetro
// y volumen de carreras realizadas durante la jornada activa.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.BiFuelCostMetrics
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
// BLOQUE 1: TARJETA DE KPIS OPERACIONALES EN RUTA
// ------------------------------------------------------------------------------
/**
 * Renderiza el resumen métrico de la jornada activa para monitoreo en cabina.
 *
 * @param summary Resumen contable consolidado del turno actual.
 * @param metrics Métricas de costo kilométrico del sistema bi-combustible.
 * @param modifier Modificador Compose de diseño.
 */
@Composable
fun CockpitKpisCard(
    summary: ShiftFinancialSummary,
    metrics: BiFuelCostMetrics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "MÉTRICAS DE CABINA EN RUTA",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = ElectricBluePrimary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Ingreso Neto Total:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Text(text = formatCop(summary.totalNetIncome), color = StatusGreen, fontWeight = FontWeight.Bold)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Combustible Total:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Text(text = formatCop(summary.totalFuelExpense), color = StatusRed, fontWeight = FontWeight.Bold)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "• GNV / Gasolina:", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${formatCop(summary.totalGasExpense)} GNV | ${formatCop(summary.totalGasolineExpense)} Gas",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Costo por Km Mixto:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${formatCop(metrics.blendedCostPerKm)} / km",
                    color = FuelGasoline,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Carreras Realizadas:", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Text(text = "${summary.tripsCount} servicios", color = TextPrimary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private fun formatCop(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return String.format(Locale.GERMANY, "$%,d", rounded)
}
