// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/EditDayMetricsDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo de edición integral de horario (inicio/fin) y odometría (inicial/final)
// de un día calendario (Orden #043). Permite corregir registros erróneos o incompletos.
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.DayHistorySummary
import com.ananapps.kilometa.presentation.common.CockpitFloatingScaffold
import com.ananapps.kilometa.presentation.home.components.DateTimePickerRow
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun EditDayMetricsDialog(
    day: DayHistorySummary,
    onDismiss: () -> Unit,
    onConfirmSave: (startOdo: Int, endOdo: Int, startTimeMillis: Long, endTimeMillis: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var startOdoText by remember(day) { mutableStateOf(if (day.startOdometer > 0) day.startOdometer.toString() else "") }
    var endOdoText by remember(day) { mutableStateOf(if (day.endOdometer > 0) day.endOdometer.toString() else "") }
    var startTime by remember(day) { mutableStateOf(day.startTimeMillis) }
    var endTime by remember(day) { mutableStateOf(day.endTimeMillis) }

    val startOdo = startOdoText.filter { it.isDigit() }.toIntOrNull() ?: 0
    val endOdo = endOdoText.filter { it.isDigit() }.toIntOrNull() ?: startOdo
    val totalKm = (endOdo - startOdo).coerceAtLeast(0)
    val isFormValid = startOdo > 0 && endOdo >= startOdo

    val hasDirtyData = startOdo != day.startOdometer ||
            endOdo != day.endOdometer ||
            startTime != day.startTimeMillis ||
            endTime != day.endTimeMillis

    CockpitFloatingScaffold(
        title = "📅 Ajustar Horario y Km",
        headerColor = ElectricBluePrimary,
        onDismiss = onDismiss,
        confirmButtonText = "GUARDAR DÍA ($totalKm KM)",
        confirmButtonColor = ElectricBluePrimary,
        isConfirmEnabled = isFormValid,
        onConfirm = {
            if (isFormValid) {
                onConfirmSave(startOdo, endOdo, startTime, endTime)
                onDismiss()
            }
        },
        hasDirtyData = hasDirtyData,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Cabecera con fecha del día
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepObsidianSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = day.dateFormatted,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
            }

            // Sección Odómetros
            Text(
                text = "KILOMETRAJE DEL DÍA",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = startOdoText,
                    onValueChange = { startOdoText = it },
                    label = { Text("Odo Inicial", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricBluePrimary,
                        unfocusedBorderColor = DeepObsidianBorder
                    )
                )

                OutlinedTextField(
                    value = endOdoText,
                    onValueChange = { endOdoText = it },
                    label = { Text("Odo Final", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricBluePrimary,
                        unfocusedBorderColor = DeepObsidianBorder
                    )
                )
            }

            // Resumen de Km
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Distancia Calculada:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(
                    text = "$totalKm km",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = if (endOdo >= startOdo) StatusGreen else StatusRed
                )
            }

            HorizontalDivider(color = DeepObsidianBorder.copy(alpha = 0.5f))

            // Sección Horarios
            Text(
                text = "HORA DE INICIO DE RUTA",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
            DateTimePickerRow(
                selectedDateTimeMillis = startTime,
                onDateTimeSelected = { startTime = it },
                accentColor = ElectricBluePrimary
            )

            Text(
                text = "HORA DE FINALIZACIÓN DE RUTA",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
            DateTimePickerRow(
                selectedDateTimeMillis = endTime,
                onDateTimeSelected = { endTime = it },
                accentColor = ElectricBluePrimary
            )
        }
    }
}
