// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/DateTimePickerRow.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Selector ergonómico de Fecha y Hora estilo KiloMeta Cockpit.
// Utiliza diálogos nativos DatePickerDialog y TimePickerDialog de Android,
// formateando la fecha y hora en español colombiano con touch targets de 52dp.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DateTimePickerRow(
    selectedDateTimeMillis: Long,
    onDateTimeSelected: (Long) -> Unit,
    accentColor: Color = ElectricBluePrimary,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = remember(selectedDateTimeMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedDateTimeMillis }
    }
    val dateFormatter = remember { SimpleDateFormat("dd/MMM/yyyy", Locale("es", "CO")) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale("es", "CO")) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Selector de Fecha
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
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Fecha",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text("Fecha", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = dateFormatter.format(Date(selectedDateTimeMillis)),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }
        }

        // Selector de Hora
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
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Hora",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text("Hora", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = timeFormatter.format(Date(selectedDateTimeMillis)),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
