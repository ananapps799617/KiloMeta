// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/PastDateActionPickerDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal para seleccionar qué tipo de actividad registrar en una fecha pasada (Orden #043).
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class HistoryActivityType { TRIP, FUELING, EXPENSE, MAINTENANCE }

@Composable
fun PastDateActionPickerDialog(
    dateMillis: Long,
    onSelectAction: (HistoryActivityType) -> Unit,
    onDismiss: () -> Unit
) {
    val dateStr = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "CO")).format(Date(dateMillis)).replaceFirstChar { it.uppercase() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Registrar Actividad",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Selecciona la operación a registrar para el $dateStr:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Button(
                    onClick = { onSelectAction(HistoryActivityType.TRIP) },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary.copy(alpha = 0.2f), contentColor = ElectricBluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🚗 Registrar Carrera", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onSelectAction(HistoryActivityType.FUELING) },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A00).copy(alpha = 0.2f), contentColor = Color(0xFFFF7A00)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("⛽ Registrar Tanqueo", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onSelectAction(HistoryActivityType.EXPENSE) },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935).copy(alpha = 0.2f), contentColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("💸 Registrar Gasto", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onSelectAction(HistoryActivityType.MAINTENANCE) },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAB47BC).copy(alpha = 0.2f), contentColor = Color(0xFFAB47BC)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🔧 Registrar Mantenimiento", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) }
        },
        containerColor = DeepObsidianSurface
    )
}
