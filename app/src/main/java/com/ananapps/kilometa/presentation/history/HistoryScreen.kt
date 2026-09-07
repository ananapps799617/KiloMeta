// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/HistoryScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla principal del módulo de Historial & Analíticas Bi-Combustible.
// Orquesta los filtros temporales ergonómicos (Touch Targets >= 48dp),
// la tarjeta de ahorro GNV, la barra por plataforma y el LazyColumn de turnos.
// ==============================================================================

package com.ananapps.kilometa.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ananapps.kilometa.presentation.history.components.BiFuelSavingsCard
import com.ananapps.kilometa.presentation.history.components.PlatformBreakdownCard
import com.ananapps.kilometa.presentation.history.components.ShiftHistoryCard
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: PANTALLA PRINCIPAL DE HISTORIAL Y AUDITORÍA
// ------------------------------------------------------------------------------
/**
 * Composable que orquesta la vista de historial de turnos y analíticas contables.
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(modifier = modifier.fillMaxSize(), color = DeepObsidianBackground) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Historial & Analíticas",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val activeFilter = when (val state = uiState) {
                is HistoryUiState.Success -> state.selectedFilter
                is HistoryUiState.Empty -> state.selectedFilter
                else -> HistoryFilterPeriod.ALL
            }

            FilterPeriodSelector(
                selectedFilter = activeFilter,
                onSelectFilter = { viewModel.onFilterSelected(it) }
            )
            Spacer(modifier = Modifier.height(14.dp))

            when (val state = uiState) {
                is HistoryUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ElectricBluePrimary)
                    }
                }
                is HistoryUiState.Empty -> EmptyHistoryView(selectedFilter = state.selectedFilter)
                is HistoryUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error: ${state.message}", color = TextSecondary)
                    }
                }
                is HistoryUiState.Success -> HistoryContentList(state = state)
            }
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: SELECTOR DE PERÍODO TEMPORAL ERGONÓMICO
// ------------------------------------------------------------------------------
@Composable
private fun FilterPeriodSelector(
    selectedFilter: HistoryFilterPeriod,
    onSelectFilter: (HistoryFilterPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepObsidianSurface, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HistoryFilterPeriod.entries.forEach { period ->
            val isSelected = period == selectedFilter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) ElectricBluePrimary else DeepObsidianSurface)
                    .clickable { onSelectFilter(period) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 3: LISTA DE CONTENIDO CON ANALÍTICAS Y TURNOS
// ------------------------------------------------------------------------------
@Composable
private fun HistoryContentList(state: HistoryUiState.Success) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                    Text(text = "Ganancia Total Neta", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(text = currencyFormat.format(state.totalNetProfit), style = MaterialTheme.typography.titleMedium, color = StatusGreen, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Distancia", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(text = "${state.totalKilometers} km", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Carreras", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(text = "${state.totalTrips}", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
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

// ------------------------------------------------------------------------------
// BLOQUE 4: VISTA DE ESTADO VACÍO
// ------------------------------------------------------------------------------
@Composable
private fun EmptyHistoryView(selectedFilter: HistoryFilterPeriod) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Sin jornadas registradas",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "No se encontraron turnos cerrados en el filtro '${selectedFilter.displayName}'.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
