// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/DayHistoryCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tarjeta de Día Calendario en Cascada para el Historial Soberano (Orden #041).
// Plegado: Resumen financiero compacto.
// Desplegado (in-place): Ficha técnica completa de cabina + lista de actividades con edición/borrado.
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.*
import com.ananapps.kilometa.presentation.home.components.DayActivityItemRow
import com.ananapps.kilometa.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DayHistoryCard(
    day: DayHistorySummary,
    onEditTrip: (Trip) -> Unit,
    onEditFueling: (Fueling) -> Unit,
    onEditExpense: (Expense) -> Unit,
    onEditMaintenance: (MaintenanceRecord) -> Unit,
    onDeleteTrip: (String) -> Unit,
    onDeleteFueling: (String) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onDeleteMaintenance: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // CABECERA DEL DÍA (SIEMPRE VISIBLE)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = day.dateFormatted,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "${day.distanceKm} km · ${day.events.count { it is DayActivityEvent.TripItem }} carreras · ${day.events.size} registros",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$ %,.0f".format(Locale.US, day.netProfit),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (day.netProfit >= 0) StatusGreen else StatusRed
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Colapsar" else "Desplegar",
                        tint = ElectricBluePrimary
                    )
                }
            }

            // CONTENIDO EXPANDIBLE IN-PLACE
            if (isExpanded) {
                HorizontalDivider(color = DeepObsidianBorder.copy(alpha = 0.5f))

                // 1. FICHA TÉCNICA FINANCIERA Y OPERATIVA DEL DÍA
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = DeepObsidianSurfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📊 BALANCE COMPLETO DEL DÍA",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricBluePrimary
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Odómetros:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("${day.startOdometer} ➔ ${day.endOdometer} (${day.distanceKm} km)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Horas de Ruta:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("${day.startTimeFormatted} ➔ ${day.endTimeFormatted} (${day.elapsedHoursFormatted})", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }

                        HorizontalDivider(color = DeepObsidianBorder.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 2.dp))

                        FinancialRow("Producido Bruto:", day.grossProduced, TextPrimary)
                        FinancialRow("Comisiones Apps:", -day.appCommissions, StatusRed)
                        FinancialRow("Combustible:", -day.fuelSpent, StatusRed)
                        FinancialRow("Gastos de Ruta:", -day.expensesSpent, StatusRed)

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
                            Text("$ %,.0f / km".format(Locale.US, day.ratePerKm), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = ElectricBluePrimary)
                        }
                    }
                }

                // 2. LÍNEA DE ACTIVIDADES CRONOLÓGICAS DEL DÍA
                Text(
                    text = "ACTIVIDADES EN CARRETERA (${day.events.size})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    day.events.forEach { event ->
                        DayActivityItemRow(
                            event = event,
                            timeFormatted = timeFormatter.format(Date(event.timestamp)),
                            onEditRequested = {
                                when (event) {
                                    is DayActivityEvent.TripItem -> onEditTrip(event.trip)
                                    is DayActivityEvent.FuelingItem -> onEditFueling(event.fueling)
                                    is DayActivityEvent.ExpenseItem -> onEditExpense(event.expense)
                                    is DayActivityEvent.MaintenanceItem -> onEditMaintenance(event.maintenance)
                                    else -> Unit
                                }
                            },
                            onDeleteRequested = {
                                when (event) {
                                    is DayActivityEvent.TripItem -> onDeleteTrip(event.trip.id)
                                    is DayActivityEvent.FuelingItem -> onDeleteFueling(event.fueling.id)
                                    is DayActivityEvent.ExpenseItem -> onDeleteExpense(event.expense.id)
                                    is DayActivityEvent.MaintenanceItem -> onDeleteMaintenance(event.maintenance.id)
                                    else -> Unit
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialRow(label: String, amount: Double, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            "$ %,.0f".format(Locale.US, amount),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}
