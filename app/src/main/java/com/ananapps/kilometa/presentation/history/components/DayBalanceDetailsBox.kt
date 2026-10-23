// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/DayBalanceDetailsBox.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Ficha técnica financiera y operativa del día calendario (Orden #041, #068, #069, #070).
// Presentación ergonómica y jerarquizada: horario de ruta, ocupación de carretera,
// balance contable limpio en COP y badges destacados de rendimiento ($/km y $/hora).
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.ananapps.kilometa.domain.model.DayHistorySummary
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun DayBalanceDetailsBox(
    day: DayHistorySummary,
    onEditDayMetrics: (DayHistorySummary) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = DeepObsidianSurfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cabecera de Ficha Técnica
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 BALANCE DEL DÍA",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
                Text(
                    text = "✏️ Ajustar Horas y Km",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary,
                    modifier = Modifier.clickable { onEditDayMetrics(day) }
                )
            }

            // 1. Bloque: Horario y Descansos (Deshacinado Orden #070)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⏱️ Horario:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        text = "${day.startTimeFormatted} ➔ ${day.endTimeFormatted} (${day.elapsedHoursFormatted} brutas)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "⚡ Efectivo: ${day.effectiveHoursFormatted.ifBlank { day.elapsedHoursFormatted }}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = StatusGreen
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusAmber.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "☕ Descanso: ${day.breakHoursFormatted}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = StatusAmber
                        )
                    }
                }
            }

            HorizontalDivider(color = DeepObsidianBorder.copy(alpha = 0.3f))

            // 2. Bloque: Ocupación en Carretera (Odómetro, Km y Tiempos)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Odómetros:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        text = "${day.startOdometer} ➔ ${day.endOdometer} (${day.distanceKm} km)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Distancia:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("🟢 ${day.passengerKm.toInt()} km pas", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = StatusGreen)
                        Text("·", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("⚪ ${day.emptyKm.toInt()} km vac", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tiempos:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("🟢 ${day.passengerTimeFormatted} pas", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = StatusGreen)
                        Text("·", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("⏳ ${day.emptyTimeFormatted} muerto", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = StatusAmber)
                    }
                }
            }

            HorizontalDivider(color = DeepObsidianBorder.copy(alpha = 0.3f))

            // 3. Bloque: Desglose Financiero
            FinancialDetailRow("Producido Bruto:", day.grossProduced, TextPrimary)
            FinancialDetailRow("Comisiones Apps:", -day.appCommissions, StatusRed)
            FinancialDetailRow("Combustible:", -day.fuelSpent, StatusRed)
            FinancialDetailRow("Gastos de Ruta:", -day.expensesSpent, StatusRed)

            day.platformEarnings.forEach { (platform, amount) ->
                FinancialDetailRow("Ingresos ${platform.visualToken.badgeLabel}:", amount, StatusGreen)
            }

            HorizontalDivider(color = DeepObsidianBorder.copy(alpha = 0.3f))

            // 4. Bloque: Ganancia Neta Libre
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ganancia Neta Libre:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Text(
                    "$ %,.0f".format(Locale.US, day.netProfit),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace),
                    color = if (day.netProfit >= 0) StatusGreen else StatusRed
                )
            }

            // 5. Bloque: Badges de Rendimiento ($/km y $/hora)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Rendimiento:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElectricBluePrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🛣️ $ %,.0f / km".format(Locale.US, day.ratePerKm),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricBluePrimary
                        )
                    }
                    if (day.ratePerHour > 0.0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StatusGreen.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⏱️ $ %,.0f / h".format(Locale.US, day.ratePerHour),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialDetailRow(label: String, amount: Double, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            "$ %,.0f".format(Locale.US, amount),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}
