// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/MaintenanceTypeSelector.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Selector horizontal asistido para catálogo de tipos de servicio y mantenimiento preventivo.
// Regla sagrada: < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.MaintenanceType

private val DrivvoBrown = Color(0xFF8D6E63)

@Composable
fun MaintenanceTypeSelector(
    selectedType: MaintenanceType,
    onTypeSelected: (MaintenanceType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MaintenanceType.entries.forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text("${type.icon} ${type.displayName}") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DrivvoBrown,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}
