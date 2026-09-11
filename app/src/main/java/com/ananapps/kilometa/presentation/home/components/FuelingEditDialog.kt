// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/FuelingEditDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal ergonómico para la edición de registros de abastecimiento de combustible.
// Permite modificar tipo de combustible (Gas vs Gasolina), costo total, volumen y odómetro.
// Regla inquebrantable: Archivo pedagógico < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

@Composable
fun FuelingEditDialog(
    fueling: Fueling,
    onDismiss: () -> Unit,
    onConfirm: (Fueling) -> Unit
) {
    var fuelType by remember { mutableStateOf(fueling.fuelType) }
    var costText by remember { mutableStateOf(fueling.amountPaid.toLong().toString()) }
    var volumeText by remember { mutableStateOf(fueling.volume.toString()) }
    var odoText by remember { mutableStateOf(fueling.odometer.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepObsidianSurface,
        title = { Text("Editar Tanqueo", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Selector de Tipo de Combustible
                Text("Combustible:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf(
                        FuelType.GAS to "🟢 GNV (m³)",
                        FuelType.GASOLINE to "⛽ Gasolina (Gal)"
                    )
                    types.forEach { (type, label) ->
                        val isSelected = fuelType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { fuelType = type },
                            color = if (isSelected) ElectricBluePrimary.copy(alpha = 0.25f) else DeepObsidianSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) ElectricBluePrimary else DeepObsidianSurfaceVariant
                            )
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) ElectricBluePrimary else TextPrimary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it },
                    label = { Text("Total Pagado ($ COP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricBluePrimary
                    )
                )

                OutlinedTextField(
                    value = volumeText,
                    onValueChange = { volumeText = it },
                    label = { Text("Volumen (${fuelType.unitLabel})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricBluePrimary
                    )
                )

                OutlinedTextField(
                    value = odoText,
                    onValueChange = { odoText = it },
                    label = { Text("Odómetro (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricBluePrimary
                    )
                )

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = StatusRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = costText.toDoubleOrNull()
                    val vol = volumeText.toDoubleOrNull()
                    val odo = odoText.toIntOrNull()
                    if (cost == null || cost <= 0.0) {
                        errorMessage = "El costo debe ser mayor a cero"
                        return@Button
                    }
                    if (vol == null || vol <= 0.0) {
                        errorMessage = "El volumen debe ser mayor a cero"
                        return@Button
                    }
                    if (odo == null || odo < 0) {
                        errorMessage = "El odómetro no puede ser negativo"
                        return@Button
                    }
                    onConfirm(fueling.copy(fuelType = fuelType, amountPaid = cost, volume = vol, odometer = odo))
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
            ) {
                Text("GUARDAR", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = TextSecondary) }
        }
    )
}
