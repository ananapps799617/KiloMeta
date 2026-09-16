// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/HistoryFilterSelector.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Selector ergonómico de período temporal y vista de estado vacío para el Historial.
// Modularizado para preservar la Regla Sagrada (< 280 líneas de código).
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.HistoryFilterPeriod
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

@Composable
fun FilterPeriodSelector(
    selectedFilter: HistoryFilterPeriod,
    onSelectFilter: (HistoryFilterPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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

@Composable
fun EmptyHistoryView(
    selectedFilter: HistoryFilterPeriod,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Sin registros para este período",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "No se encontraron actividades en '${selectedFilter.displayName}'.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
