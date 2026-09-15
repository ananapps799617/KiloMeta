// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/HistoryRoadNode.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Nodo individual en la pista de asfalto continua (#14171E) con ficha técnica
// desplegable en acordeón y acciones protegidas de edición y eliminación.
// Subcomponente desacoplado de ContinuousHistoryRoadList para respetar < 280 LOC.
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AsphaltBg = Color(0xFF14171E)
private val RoadLaneDashColor = Color(0xFFE6EDF3).copy(alpha = 0.35f)

@Composable
fun HistoryRoadNode(
    shift: ShiftFinancialSummary,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val startTimeStr = timeFormatter.format(Date(shift.startTime))
    val endTimeStr = shift.endTime?.let { timeFormatter.format(Date(it)) } ?: "En curso"

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Carril de Asfalto con Pin Temático
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(if (isExpanded) 180.dp else 68.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.width(14.dp).fillMaxHeight().background(AsphaltBg)) {
                drawLine(
                    color = RoadLaneDashColor,
                    start = Offset(size.width / 2f, 0f),
                    end = Offset(size.width / 2f, size.height),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(StatusGreen)
                    .border(1.5.dp, Color.Black.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🚗", fontSize = 11.sp)
            }
        }

        // Tarjeta Desplegable en Acordeón
        Card(
            modifier = Modifier
                .weight(1f)
                .animateContentSize()
                .clickable { isExpanded = !isExpanded },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DeepObsidianSurfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jornada $startTimeStr ➔ $endTimeStr",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "$ %,.0f".format(Locale.US, shift.netProfit),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (shift.netProfit >= 0) StatusGreen else StatusRed
                    )
                }

                Text(
                    text = "${shift.totalKilometers} km · ${shift.tripsCount} carreras · ${shift.startOdometer} ➔ ${shift.endOdometer ?: "..."} km",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                // Ficha Técnica Expandida
                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HorizontalDivider(color = DeepObsidianSurface, thickness = 1.dp)
                        MetricRow(label = "Producido Bruto:", value = "$ %,.0f".format(Locale.US, shift.totalGrossIncome), color = StatusGreen)
                        MetricRow(label = "Comisiones Apps:", value = "-$ %,.0f".format(Locale.US, shift.totalCommissions), color = TextSecondary)
                        MetricRow(label = "Combustible (GNV/Gas):", value = "-$ %,.0f".format(Locale.US, shift.totalFuelExpense), color = FuelGasoline)
                        MetricRow(label = "Gastos de Ruta:", value = "-$ %,.0f".format(Locale.US, shift.totalOperationalExpenses), color = StatusRed)
                        MetricRow(label = "Rendimiento:", value = "$ %,.0f / km".format(Locale.US, shift.netIncomePerKilometer), color = ElectricBluePrimary)

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = ElectricBluePrimary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = StatusRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), color = color)
    }
}
