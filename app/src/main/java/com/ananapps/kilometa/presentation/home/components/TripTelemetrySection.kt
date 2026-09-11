// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/TripTelemetrySection.kt
// DESCRIPCIÃ“N PEDAGÃ“GICA:
// SecciÃ³n de telemetrÃ­a estilo Cockpit KiloMeta: vehÃ­culo activo, fecha/hora interactiva
// con Date/Time pickers, odÃ³metro inicial editable con cÃ¡lculo de km en vacÃ­o
// y odÃ³metro final con cÃ¡lculo de km de carrera con pasajero.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TripTelemetrySection(
    vehicleProfile: VehicleProfile,
    selectedDateTimeMillis: Long,
    onDateTimeSelected: (Long) -> Unit,
    startOdometerText: String,
    onStartOdometerChange: (String) -> Unit,
    endOdometerText: String,
    onEndOdometerChange: (String) -> Unit,
    lastKnownOdometer: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = remember(selectedDateTimeMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedDateTimeMillis }
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MMM/yyyy", Locale("es", "CO")) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale("es", "CO")) }

    val startOdo = startOdometerText.toIntOrNull()
    val endOdo = endOdometerText.toIntOrNull()

    // CÃ¡lculo reactivo de km en vacÃ­o
    val emptyKm = if (startOdo != null && lastKnownOdometer > 0 && startOdo > lastKnownOdometer) {
        startOdo - lastKnownOdometer
    } else null

    // CÃ¡lculo reactivo de km con pasajero
    val passengerKm = if (startOdo != null && endOdo != null && endOdo >= startOdo) {
        endOdo - startOdo
    } else null

    val isOdometerError = startOdo != null && endOdo != null && endOdo < startOdo

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. VehÃ­culo Activo (Formato Flat List Cockpit KiloMeta con icono dinÃ¡mico)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DeepObsidianSurfaceVariant,
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = vehicleProfile.vehicleType.icon, fontSize = 24.sp)
                Column {
                    Text(
                        text = "VehÃ­culo",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = if (vehicleProfile.model.isNotBlank()) "${vehicleProfile.model} (${vehicleProfile.brand}) (${vehicleProfile.vehiclePlate})" else "S6 (BYD) (HQX882)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Base odÃ³metro: ${lastKnownOdometer} km",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // 2. Fila Fecha & Hora Interactivas
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Selector Fecha
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, TextSecondary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                calendar.set(Calendar.YEAR, y)
                                calendar.set(Calendar.MONTH, m)
                                calendar.set(Calendar.DAY_OF_MONTH, d)
                                onDateTimeSelected(calendar.timeInMillis)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                color = DeepObsidianSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = ElectricBluePrimary, modifier = Modifier.size(20.dp))
                    Column {
                        Text("Fecha", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(dateFormatter.format(Date(selectedDateTimeMillis)), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                    }
                }
            }

            // Selector Hora
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, TextSecondary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .clickable {
                        TimePickerDialog(
                            context,
                            { _, h, min ->
                                calendar.set(Calendar.HOUR_OF_DAY, h)
                                calendar.set(Calendar.MINUTE, min)
                                onDateTimeSelected(calendar.timeInMillis)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                        ).show()
                    },
                color = DeepObsidianSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = ElectricBluePrimary, modifier = Modifier.size(20.dp))
                    Column {
                        Text("Hora", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(timeFormatter.format(Date(selectedDateTimeMillis)), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                    }
                }
            }
        }

        // 3. OdÃ³metros: Inicial (editable) y Final (opcional)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = startOdometerText,
                onValueChange = onStartOdometerChange,
                label = { Text("OdÃ³metro Inicial (km)") },
                placeholder = { Text(lastKnownOdometer.toString()) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBluePrimary,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = endOdometerText,
                onValueChange = onEndOdometerChange,
                label = { Text("OdÃ³metro Final") },
                placeholder = { Text("Opcional") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isOdometerError,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBluePrimary,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // 4. Indicadores en vivo de Km en vacÃ­o y Km de carrera
        if (emptyKm != null && emptyKm > 0) {
            Text(
                text = "ðŸ’¨ $emptyKm km rodados en vacÃ­o antes de este viaje",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = StatusAmber
            )
        }

        if (passengerKm != null) {
            Text(
                text = "ðŸš— $passengerKm km recorridos con pasajero en esta carrera",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = StatusGreen
            )
        }

        if (isOdometerError) {
            Text(
                text = "âš ï¸ El odÃ³metro final no puede ser menor al inicial ($startOdo km)",
                style = MaterialTheme.typography.labelSmall,
                color = StatusRed
            )
        }
    }
}

