// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/DayBalanceDetailsBox.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Ficha técnica financiera y operativa del día calendario (Orden #041, #068, #069).
// Muestra balances limpios en COP, telemetría de odometría, km con pasajero/vacíos,
// horas efectivas descontando descansos y tiempo con pasajero vs tiempo muerto.
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
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

            // Odómetros
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Odómetros:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(
                    text = "${day.startOdometer} ➔ ${day.endOdometer} (${day.distanceKm} km)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }

            // Kilómetros de Ruta (Pasajero vs Vacíos)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Km de Ruta:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                val kmDetail = if (day.startOdometer > 0 && day.endOdometer > day.startOdometer) {
                    "Pasajero: ${day.passengerKm.toInt()} km · Vacíos: ${day.emptyKm.toInt()} km"
                } else {
                    "Pasajero: ${day.passengerKm.toInt()} km"
                }
                Text(kmDetail, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            }

            // Horas de Ruta (Inicio ➔ Fin y Descuento de Descanso)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Horas de Ruta:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                val hoursDetail = if (day.breakMinutes > 0) {
                    "${day.startTimeFormatted} ➔ ${day.endTimeFormatted} (${day.effectiveHoursFormatted} efec · ${day.breakHoursFormatted} desc)"
                } else {
                    "${day.startTimeFormatted} ➔ ${day.endTimeFormatted} (${day.elapsedHoursFormatted})"
                }
                Text(hoursDetail, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            }

            // Tiempo de Ruta (Con Pasajero vs Tiempo Muerto)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tiempo de Ruta:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(
                    text = "Pasajero: ${day.passengerTimeFormatted} · Muerto: ${day.emptyTimeFormatted}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary
                )
            }

            HorizontalDivider(color = DeepObsidianBorder.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 2.dp))

            FinancialDetailRow("Producido Bruto:", day.grossProduced, TextPrimary)
            FinancialDetailRow("Comisiones Apps:", -day.appCommissions, StatusRed)
            FinancialDetailRow("Combustible:", -day.fuelSpent, StatusRed)
            FinancialDetailRow("Gastos de Ruta:", -day.expensesSpent, StatusRed)

            day.platformEarnings.forEach { (platform, amount) ->
                FinancialDetailRow("Ingresos ${platform.visualToken.badgeLabel}:", amount, StatusGreen)
            }

            HorizontalDivider(color = DeepObsidianBorder.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 2.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Ganancia Neta Libre:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Text(
                    "$ %,.0f".format(Locale.US, day.netProfit),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace),
                    color = if (day.netProfit >= 0) StatusGreen else StatusRed
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Rendimiento:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                val perfText = if (day.ratePerHour > 0.0) {
                    "$ %,.0f / km  ·  $ %,.0f / h".format(Locale.US, day.ratePerKm, day.ratePerHour)
                } else {
                    "$ %,.0f / km".format(Locale.US, day.ratePerKm)
                }
                Text(perfText, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = ElectricBluePrimary)
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
