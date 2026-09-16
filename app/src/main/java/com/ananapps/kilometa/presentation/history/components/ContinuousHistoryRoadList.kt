// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/ContinuousHistoryRoadList.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Línea de tiempo vertical continua estilo Drivvo por Día Calendario Soberano (Orden #041).
// Agrupa días por mes y renderiza tarjetas acordeón in-place sin popup de jornadas.
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.*
import com.ananapps.kilometa.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ContinuousHistoryRoadList(
    days: List<DayHistorySummary>,
    onEditTrip: (Trip) -> Unit,
    onEditFueling: (Fueling) -> Unit,
    onEditExpense: (Expense) -> Unit,
    onEditMaintenance: (MaintenanceRecord) -> Unit,
    onDeleteTrip: (String) -> Unit,
    onDeleteFueling: (String) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onDeleteMaintenance: (String) -> Unit,
    onAddTrip: (Long) -> Unit = {},
    onAddFueling: (Long) -> Unit = {},
    onAddExpense: (Long) -> Unit = {},
    onAddMaintenance: (Long) -> Unit = {},
    onEditDayMetrics: (DayHistorySummary) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val monthFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale("es", "CO")) }

    // Agrupación de días por Mes
    val groupedByMonth = remember(days) {
        days.groupBy { day ->
            monthFormatter.format(Date(day.dateMillis)).uppercase(Locale("es", "CO"))
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        groupedByMonth.forEach { (monthHeader, monthDays) ->
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

            // Días calendario del mes
            items(monthDays, key = { it.dateMillis }) { day ->
                DayHistoryCard(
                    day = day,
                    onEditTrip = onEditTrip,
                    onEditFueling = onEditFueling,
                    onEditExpense = onEditExpense,
                    onEditMaintenance = onEditMaintenance,
                    onDeleteTrip = onDeleteTrip,
                    onDeleteFueling = onDeleteFueling,
                    onDeleteExpense = onDeleteExpense,
                    onDeleteMaintenance = onDeleteMaintenance,
                    onAddTrip = onAddTrip,
                    onAddFueling = onAddFueling,
                    onAddExpense = onAddExpense,
                    onAddMaintenance = onAddMaintenance,
                    onEditDayMetrics = onEditDayMetrics
                )
            }
        }
    }
}
