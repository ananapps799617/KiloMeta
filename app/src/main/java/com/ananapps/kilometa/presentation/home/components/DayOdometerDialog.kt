// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/DayOdometerDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante de cabina para configurar y registrar los odómetros
// de arranque y cierre del día (Opción 4, Orden #043).
// Provee cálculo en vivo de km rodados, validaciones de coherencia y pre-llenado inteligente.
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
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.common.CockpitFloatingScaffold
import com.ananapps.kilometa.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DayOdometerDialog(
    dayStartOdometer: Int?,
    dayEndOdometer: Int?,
    lastKnownOdometer: Int,
    onDismiss: () -> Unit,
    onConfirmSave: (startOdo: Int, endOdo: Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialStart = dayStartOdometer?.takeIf { it > 0 } ?: lastKnownOdometer
    var startOdoText by remember { mutableStateOf(if (initialStart > 0) initialStart.toString() else "") }
    var endOdoText by remember { mutableStateOf(dayEndOdometer?.takeIf { it > 0 }?.toString() ?: "") }

    val startOdo = startOdoText.filter { it.isDigit() }.toIntOrNull() ?: 0
    val endOdo = endOdoText.filter { it.isDigit() }.toIntOrNull() ?: 0

    val isEndOdoValid = endOdo == 0 || endOdo >= startOdo
    val isFormValid = startOdo > 0 && isEndOdoValid
    val totalKmDriven = if (endOdo >= startOdo && startOdo > 0 && endOdo > 0) endOdo - startOdo else 0

    val hasDirtyData = startOdo != (dayStartOdometer ?: lastKnownOdometer) ||
            endOdo != (dayEndOdometer ?: 0)

    val todayFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "CO"))
        sdf.format(Date()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "CO")) else it.toString() }
    }

    CockpitFloatingScaffold(
        title = "🏁 Odómetro del Día",
        headerColor = ElectricBluePrimary,
        onDismiss = onDismiss,
        confirmButtonText = if (endOdo > 0 && totalKmDriven > 0) "GUARDAR DÍA ($totalKmDriven KM)" else "GUARDAR ODÓMETRO",
        confirmButtonColor = if (endOdo > 0) StatusGreen else ElectricBluePrimary,
        isConfirmEnabled = isFormValid,
        onConfirm = {
            if (isFormValid) {
                onConfirmSave(startOdo, if (endOdo > 0) endOdo else null)
                onDismiss()
            }
        },
        hasDirtyData = hasDirtyData,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Cabecera con fecha de la jornada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepObsidianSurfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "JORNADA DE HOY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                        Text(
                            text = todayFormatted,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricBluePrimary
                        )
                    }
                }
            }

            // Campo: Odómetro Inicial
            OutlinedTextField(
                value = startOdoText,
                onValueChange = { startOdoText = it },
                label = { Text("Odómetro Inicial (Arrancar el día)", color = TextSecondary) },
                placeholder = { Text("Ej: 208500", color = TextSecondary.copy(alpha = 0.5f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = ElectricBluePrimary,
                    unfocusedBorderColor = DeepObsidianBorder,
                    cursorColor = ElectricBluePrimary
                )
            )

            // Campo: Odómetro Final (Cierre del día)
            OutlinedTextField(
                value = endOdoText,
                onValueChange = { endOdoText = it },
                label = { Text("Odómetro Final (Cierre de jornada)", color = TextSecondary) },
                placeholder = { Text("Opcional hasta terminar el día", color = TextSecondary.copy(alpha = 0.5f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = endOdo > 0 && endOdo < startOdo,
                supportingText = {
                    if (endOdo > 0 && endOdo < startOdo) {
                        Text("El odómetro final no puede ser menor al inicial ($startOdo km)", color = StatusRed)
                    } else if (endOdo == 0) {
                        Text("Puedes dejarlo en blanco y registrarlo al cerrar tu turno", color = TextSecondary)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = if (endOdo > 0) StatusGreen else ElectricBluePrimary,
                    unfocusedBorderColor = DeepObsidianBorder,
                    cursorColor = ElectricBluePrimary,
                    errorBorderColor = StatusRed
                )
            )

            // Feedback en vivo de telemetría del día
            if (startOdo > 0 && endOdo >= startOdo && endOdo > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StatusGreen.copy(alpha = 0.12f))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DISTANCIA RECORRIDA HOY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusGreen
                            )
                            Text(
                                text = "$totalKmDriven Kilómetros",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = StatusGreen
                            )
                        }
                        Text(
                            text = "🏁",
                            fontSize = 28.sp
                        )
                    }
                }
            }
        }
    }
}
