// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/ContinuousHistoryRoadList.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Línea de tiempo vertical continua estilo Drivvo para el Historial de cabina.
// Agrupa actividades pasadas por mes y día, con encabezados compactos de jornada,
// y delega el renderizado de cada nodo a HistoryRoadNode.kt.
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: LISTA PRINCIPAL AGRUPADA POR MES Y DÍA
// ------------------------------------------------------------------------------
@Composable
fun ContinuousHistoryRoadList(
    shifts: List<ShiftFinancialSummary>,
    onDeleteShift: (String) -> Unit,
    onInspectShift: (ShiftFinancialSummary) -> Unit,
    onConsolidateShifts: (List<ShiftFinancialSummary>) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale("es", "CO")) }
    val dayFormatter = remember { SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "CO")) }
    val dayKeyFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    // Agrupación de jornadas por Mes -> Día
    val groupedByMonth = remember(shifts) {
        shifts.groupBy { shift ->
            monthFormatter.format(Date(shift.startTime)).uppercase(Locale("es", "CO"))
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedByMonth.forEach { (monthHeader, monthShifts) ->
            // Cabecera de Mes estilo Drivvo
            item(key = "header_$monthHeader") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepObsidianSurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "📅 $monthHeader",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = ElectricBluePrimary
                    )
                }
            }

            // Sub-agrupación por Día dentro del Mes
            val groupedByDay = monthShifts.groupBy { dayKeyFormatter.format(Date(it.startTime)) }

            groupedByDay.forEach { (_, dayShifts) ->
                val firstShift = dayShifts.first()
                val dayTitle = dayFormatter.format(Date(firstShift.startTime)).replaceFirstChar { it.uppercase() }
                val dayNet = dayShifts.sumOf { it.netProfit }
                val dayKm = dayShifts.sumOf { it.totalKilometers }
                val dayTrips = dayShifts.sumOf { it.tripsCount }
                val dayEventsCount = dayShifts.sumOf { it.tripsCount + it.fuelingsCount + it.expensesCount }

                item(key = "day_card_${firstShift.shiftId}") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Encabezado Compacto del Día
                        DaySummaryBanner(
                            dayTitle = dayTitle,
                            dayNet = dayNet,
                            dayKm = dayKm,
                            dayTrips = dayTrips,
                            dayEventsCount = dayEventsCount,
                            canConsolidate = dayShifts.size > 1,
                            onConsolidateClick = { onConsolidateShifts(dayShifts) }
                        )

                        // Nodos en Pista de Asfalto para cada jornada del día
                        dayShifts.forEach { shift ->
                            HistoryRoadNode(
                                shift = shift,
                                onDelete = { onDeleteShift(shift.shiftId) },
                                onEdit = { onInspectShift(shift) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: BANNER RESUMEN DIARIO COMPACTO
// ------------------------------------------------------------------------------
@Composable
private fun DaySummaryBanner(
    dayTitle: String,
    dayNet: Double,
    dayKm: Int,
    dayTrips: Int,
    dayEventsCount: Int,
    canConsolidate: Boolean,
    onConsolidateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dayTitle,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "$dayKm km · $dayTrips carreras · $dayEventsCount registros",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (canConsolidate) {
                    TextButton(
                        onClick = onConsolidateClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("⚡ Unir", fontSize = 12.sp, color = ElectricBluePrimary)
                    }
                }
                Text(
                    text = "$ %,.0f".format(Locale.US, dayNet),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = if (dayNet >= 0) StatusGreen else StatusRed
                )
            }
        }
    }
}
