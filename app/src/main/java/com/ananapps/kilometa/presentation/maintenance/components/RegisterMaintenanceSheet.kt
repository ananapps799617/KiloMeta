// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/maintenance/components/RegisterMaintenanceSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet ergonómico para registrar un nuevo mantenimiento vehicular.
// Touch targets >= 56dp, selector de servicio asistido y cálculo de próximo cambio.
// ==============================================================================

package com.ananapps.kilometa.presentation.maintenance.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.MaintenanceType
import com.ananapps.kilometa.presentation.home.components.DateTimePickerRow
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterMaintenanceSheet(
    currentOdometer: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirm: (type: MaintenanceType, odo: Int, cost: Double, notes: String?, nextOdo: Int?, timestamp: Long) -> Unit
) {
    var selectedTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedType by remember { mutableStateOf(MaintenanceType.OIL_CHANGE) }
    var odoText by remember { mutableStateOf(currentOdometer.toString()) }
    var costText by remember { mutableStateOf("120000") }
    var notesText by remember { mutableStateOf("") }

    val odoNumeric = odoText.toIntOrNull() ?: currentOdometer
    val costNumeric = costText.toDoubleOrNull() ?: 0.0
    val calculatedNextOdo = odoNumeric + selectedType.defaultIntervalKm

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidianSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "REGISTRAR MANTENIMIENTO",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ElectricBluePrimary
            )

            DateTimePickerRow(
                selectedDateTimeMillis = selectedTimestamp,
                onDateTimeSelected = { selectedTimestamp = it },
                accentColor = ElectricBluePrimary
            )

            // Selector horizontal de tipo de mantenimiento
            Text(text = "Tipo de Servicio", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MaintenanceType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text("${type.icon} ${type.displayName}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBluePrimary,
                            selectedLabelColor = DeepObsidianBackground
                        )
                    )
                }
            }

            // Odómetro
            OutlinedTextField(
                value = odoText,
                onValueChange = { odoText = it.filter { ch -> ch.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Odómetro Actual (km)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Costo en COP
            OutlinedTextField(
                value = costText,
                onValueChange = { costText = it.filter { ch -> ch.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Text("$", color = ElectricBluePrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                label = { Text("Costo del Mantenimiento ($ COP)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Próximo cambio proyectado
            Text(
                text = "💡 Próximo cambio estimado a los %,d km (+%,d km)".format(calculatedNextOdo, selectedType.defaultIntervalKm),
                style = MaterialTheme.typography.bodySmall,
                color = StatusGreen
            )

            // Notas opcionales
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Taller / Repuestos / Observaciones (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Botón ergonómico de guardado (56dp)
            Button(
                onClick = {
                    if (costNumeric >= 0.0 && odoNumeric >= 0) {
                        onConfirm(selectedType, odoNumeric, costNumeric, notesText.ifBlank { null }, calculatedNextOdo, selectedTimestamp)
                        onDismiss()
                    }
                },
                enabled = costNumeric > 0.0 && odoNumeric > 0,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
            ) {
                Text(
                    text = "Guardar Mantenimiento 🛠️",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = DeepObsidianBackground
                )
            }
        }
    }
}
