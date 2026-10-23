// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/TripTelemetrySection.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Sección de telemetría de carrera estilo Cockpit KiloMeta: fecha/hora compacta en 1 toque
// y casillas simétricas de distancia (km) y duración (min) editables (< 280 LOC).
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
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TripTelemetrySection(
    vehicleProfile: VehicleProfile = VehicleProfile(),
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

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 1. Fila Compacta de Fecha & Hora (42dp de alto para máxima ergonomía visual)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Selector Fecha
            Surface(
                modifier = Modifier
                    .weight(1.1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, TextSecondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = ElectricBluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = dateFormatter.format(Date(selectedDateTimeMillis)),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = TextPrimary
                    )
                }
            }

            // Selector Hora
            Surface(
                modifier = Modifier
                    .weight(0.9f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, TextSecondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = ElectricBluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = timeFormatter.format(Date(selectedDateTimeMillis)),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = TextPrimary
                    )
                }
            }
        }

        // 2. Fila Telemetría de Carrera: Distancia (km) y Tiempo (minutos)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = distanceKmText,
                onValueChange = onDistanceKmChange,
                label = { Text("Distancia", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("0.0", color = TextSecondary.copy(alpha = 0.5f)) },
                trailingIcon = {
                    Text(
                        "km",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
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
                trailingIcon = {
                    Text(
                        "min",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
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
