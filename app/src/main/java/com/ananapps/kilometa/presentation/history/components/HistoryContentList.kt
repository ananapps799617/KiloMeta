// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/HistoryContentList.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Lista perezosa (LazyColumn) con KPIs consolidados del período, tarjetas de ahorro
// bi-combustible, desglose de plataformas y catálogo de jornadas finalizadas.
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.presentation.history.HistoryUiState
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: LISTA DE CONTENIDO CON ANALÍTICAS Y TURNOS
// ------------------------------------------------------------------------------
/**
 * Renderiza el feed estructurado de analíticas y auditoría de jornadas para el período.
 */
@Composable
fun HistoryContentList(
    state: HistoryUiState.Success,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepObsidianSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Ganancia Total Neta",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = currencyFormat.format(state.totalNetProfit),
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusGreen,
                        fontWeight = FontWeight.Black
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Distancia",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${state.totalKilometers} km",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Carreras",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${state.totalTrips}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item { BiFuelSavingsCard(metrics = state.biFuelSavings) }
        item { PlatformBreakdownCard(breakdowns = state.platformBreakdowns) }
        item {
            Text(
                text = "Jornadas Finalizadas (${state.shifts.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        items(items = state.shifts, key = { it.shiftId }) { shift ->
            ShiftHistoryCard(shift = shift)
        }
    }
}
