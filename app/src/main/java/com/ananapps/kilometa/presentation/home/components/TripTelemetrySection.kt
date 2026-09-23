// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/TripTelemetrySection.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Sección de telemetría de carrera estilo Cockpit KiloMeta: vehículo activo, fecha/hora y
// casillas de distancia (km) y duración (min) editables para cálculos de rentabilidad (Orden #050).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
    distanceKmText: String = "",
    onDistanceKmChange: (String) -> Unit = {},
    durationMinutesText: String = "",
    onDurationMinutesChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = remember(selectedDateTimeMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedDateTimeMillis }
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MMM/yyyy", Locale("es", "CO")) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale("es", "CO")) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 1. Vehículo Activo (Formato Flat List Cockpit KiloMeta con icono dinámico)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DeepObsidianSurfaceVariant,
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = vehicleProfile.vehicleType.icon, fontSize = 22.sp)
                Column {
                    Text(
                        text = "Vehículo",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = if (vehicleProfile.model.isNotBlank()) "${vehicleProfile.model} (${vehicleProfile.brand}) (${vehicleProfile.vehiclePlate})" else "S6 (BYD) (HQX882)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
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
                    .height(48.dp)
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
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = ElectricBluePrimary, modifier = Modifier.size(18.dp))
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
                    .height(48.dp)
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
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = ElectricBluePrimary, modifier = Modifier.size(18.dp))
                    Column {
                        Text("Hora", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(timeFormatter.format(Date(selectedDateTimeMillis)), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                    }
                }
            }
        }

        // 3. Fila Telemetría de Carrera: Distancia (km) y Tiempo (minutos)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = distanceKmText,
                onValueChange = onDistanceKmChange,
                label = { Text("Distancia", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("0.0", color = TextSecondary.copy(alpha = 0.5f)) },
                trailingIcon = { Text("km", color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(end = 8.dp)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DeepObsidianSurfaceVariant,
                    unfocusedContainerColor = DeepObsidianSurfaceVariant,
                    focusedBorderColor = ElectricBluePrimary,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = durationMinutesText,
                onValueChange = onDurationMinutesChange,
                label = { Text("Duración", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("0", color = TextSecondary.copy(alpha = 0.5f)) },
                trailingIcon = { Text("min", color = TextSecondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(end = 8.dp)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DeepObsidianSurfaceVariant,
                    unfocusedContainerColor = DeepObsidianSurfaceVariant,
                    focusedBorderColor = ElectricBluePrimary,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
