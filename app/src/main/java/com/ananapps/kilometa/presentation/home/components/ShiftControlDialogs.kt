// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/ShiftControlDialogs.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogos modales para el control del ciclo de vida de los turnos de trabajo:
// apertura de jornada con odómetro inicial y cierre con odómetro final y validación de odometría.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: DIÁLOGO DE INICIO DE TURNO (CON FECHA/HORA Y ODÓMETRO)
// ------------------------------------------------------------------------------
@Composable
fun StartShiftDialog(
    initialOdometer: Int,
    initialStartTime: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    onConfirm: (startOdometer: Int, startTime: Long) -> Unit
) {
    var odoText by remember { mutableStateOf(if (initialOdometer > 0) initialOdometer.toString() else "") }
    var selectedTime by remember { mutableLongStateOf(initialStartTime) }

    val startOdo = odoText.filter { it.isDigit() }.toIntOrNull() ?: 0
    val isValid = startOdo > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepObsidianSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "🚀 Iniciar Turno de Trabajo",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Ajusta la fecha, hora de arranque y kilometraje de tu vehículo:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                DateTimePickerRow(
                    selectedDateTimeMillis = selectedTime,
                    onDateTimeSelected = { selectedTime = it },
                    accentColor = StatusGreen
                )

                OutlinedTextField(
                    value = odoText,
                    onValueChange = { odoText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Odómetro de Arranque (km)") },
                    placeholder = { Text("Ej: 145200") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StatusGreen,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
                        focusedLabelColor = StatusGreen,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (isValid) onConfirm(startOdo, selectedTime) },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusGreen,
                    disabledContainerColor = StatusGreen.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Arrancar Turno 🚀", color = DeepObsidianBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

// ------------------------------------------------------------------------------
// BLOQUE 2: DIÁLOGO DE CIERRE DE TURNO (CON FECHA/HORA Y ODÓMETRO)
// ------------------------------------------------------------------------------
@Composable
fun CloseShiftDialog(
    startOdometer: Int,
    initialEndTime: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    onConfirm: (endOdometer: Int, endTime: Long) -> Unit
) {
    var endOdoText by remember { mutableStateOf(if (startOdometer > 0) startOdometer.toString() else "") }
    var selectedEndTime by remember { mutableLongStateOf(initialEndTime) }

    val endOdo = endOdoText.filter { it.isDigit() }.toIntOrNull() ?: 0
    val isValid = endOdo >= startOdometer && endOdo > 0
    val totalKmDriven = if (endOdo >= startOdometer) endOdo - startOdometer else 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepObsidianSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "🏁 Finalizar Jornada de Trabajo",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Odómetro inicial: ${String.format(Locale.US, "%,d", startOdometer)} km",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ElectricBluePrimary
                )

                DateTimePickerRow(
                    selectedDateTimeMillis = selectedEndTime,
                    onDateTimeSelected = { selectedEndTime = it },
                    accentColor = StatusRed
                )

                OutlinedTextField(
                    value = endOdoText,
                    onValueChange = { endOdoText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Odómetro Final (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = endOdo > 0 && endOdo < startOdometer,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StatusRed,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.4f),
                        focusedLabelColor = StatusRed,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (isValid && totalKmDriven > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Recorrido Total: $totalKmDriven km",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = StatusGreen
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (isValid) onConfirm(endOdo, selectedEndTime) },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusRed,
                    disabledContainerColor = StatusRed.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cerrar Turno 🏁", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continuar en Turno", color = TextSecondary)
            }
        }
    )
}
