// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/reports/components/KpiSummaryMatrix.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Matriz de Indicadores Clave de Desempeño (KPIs 2x2) estilo Drivvo.
// Desglosa Balance Neto, Distancia, Costos Totales e Ingreso Bruto con promedios
// por día y por kilómetro para auditoría de cabina.
// ==============================================================================

package com.ananapps.kilometa.presentation.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

data class KpiReportData(
    val netBalance: Double = 0.0,
    val totalDistanceKm: Int = 0,
    val totalCosts: Double = 0.0,
    val grossIncome: Double = 0.0,
    val activeDaysCount: Int = 1
)

@Composable
fun KpiSummaryMatrix(
    data: KpiReportData,
    modifier: Modifier = Modifier
) {
    val days = maxOf(1, data.activeDaysCount)
    val km = maxOf(1, data.totalDistanceKm)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Fila 1: Balance Neto y Distancia
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiTile(
                title = "BALANCE NETO",
                icon = "📉",
                primaryValue = "$ ${String.format(Locale.US, "%,.0f", data.netBalance)}",
                primaryColor = if (data.netBalance >= 0) StatusGreen else StatusRed,
                subValue1 = "$ ${String.format(Locale.US, "%,.0f", data.netBalance / days)} / día",
                subValue2 = "$ ${String.format(Locale.US, "%,.0f", data.netBalance / km)} / km",
                modifier = Modifier.weight(1f)
            )

            KpiTile(
                title = "DISTANCIA",
                icon = "📍",
                primaryValue = "${String.format(Locale.US, "%,d", data.totalDistanceKm)} km",
                primaryColor = ElectricBluePrimary,
                subValue1 = "${data.totalDistanceKm / days} km / día",
                subValue2 = "$days días con actividad",
                modifier = Modifier.weight(1f)
            )
        }

        // Fila 2: Costos Totales e Ingreso Bruto
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiTile(
                title = "COSTOS TOTALES",
                icon = "🔴",
                primaryValue = "$ ${String.format(Locale.US, "%,.0f", data.totalCosts)}",
                primaryColor = StatusRed,
                subValue1 = "$ ${String.format(Locale.US, "%,.0f", data.totalCosts / days)} / día",
                subValue2 = "$ ${String.format(Locale.US, "%,.0f", data.totalCosts / km)} / km",
                modifier = Modifier.weight(1f)
            )

            KpiTile(
                title = "INGRESO BRUTO",
                icon = "🟢",
                primaryValue = "$ ${String.format(Locale.US, "%,.0f", data.grossIncome)}",
                primaryColor = StatusGreen,
                subValue1 = "$ ${String.format(Locale.US, "%,.0f", data.grossIncome / days)} / día",
                subValue2 = "$ ${String.format(Locale.US, "%,.0f", data.grossIncome / km)} / km",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiTile(
    title: String,
    icon: String,
    primaryValue: String,
    primaryColor: Color,
    subValue1: String,
    subValue2: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = icon, fontSize = 14.sp)
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Text(
                text = primaryValue,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                ),
                color = primaryColor,
                fontSize = 18.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = subValue1,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = TextPrimary.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
                Text(
                    text = subValue2,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}
