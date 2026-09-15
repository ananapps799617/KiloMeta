// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/ShiftDetailsSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Hoja modal inferior para inspeccionar y editar los registros internos de una jornada:
// Odómetros de cabina, Carreras registradas, Tanqueos y Gastos operativos.
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.presentation.theme.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftDetailsSheet(
    shift: ShiftFinancialSummary,
    tripsFlow: Flow<List<Trip>>,
    fuelingsFlow: Flow<List<Fueling>>,
    expensesFlow: Flow<List<Expense>>,
    onDismiss: () -> Unit,
    onDeleteTrip: (String) -> Unit,
    onDeleteFueling: (String) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onUpdateShift: (startOdo: Int, endOdo: Int?, notes: String?) -> Unit
) {
    val trips by tripsFlow.collectAsState(initial = emptyList())
    val fuelings by fuelingsFlow.collectAsState(initial = emptyList())
    val expenses by expensesFlow.collectAsState(initial = emptyList())

    var startOdoText by remember(shift) { mutableStateOf(shift.startOdometer.toString()) }
    var endOdoText by remember(shift) { mutableStateOf(shift.endOdometer?.toString() ?: "") }
    var itemToDelete by remember { mutableStateOf<Triple<String, String, () -> Unit>?>(null) }

    val dateFormatter = remember { SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "CO")) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateStr = dateFormatter.format(Date(shift.startTime)).replaceFirstChar { it.uppercase() }
    val startStr = timeFormatter.format(Date(shift.startTime))
    val endStr = shift.endTime?.let { timeFormatter.format(Date(it)) } ?: "En curso"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DeepObsidianSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextSecondary.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            // Cabecera
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Detalle de Jornada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("$dateStr ($startStr ➔ $endStr)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sección Odómetros
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Odómetros de Cabina", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ElectricBluePrimary)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = startOdoText,
                                    onValueChange = { startOdoText = it },
                                    label = { Text("Inicial (km)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = endOdoText,
                                    onValueChange = { endOdoText = it },
                                    label = { Text("Final (km)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Button(
                                onClick = {
                                    val s = startOdoText.toIntOrNull() ?: shift.startOdometer
                                    val e = endOdoText.toIntOrNull()
                                    onUpdateShift(s, e, null)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                            ) {
                                Text("GUARDAR ODÓMETROS", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Carreras
                item {
                    Text("🚗 Carreras Registradas (${trips.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                if (trips.isEmpty()) {
                    item { Text("No hay carreras registradas en este turno.", style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
                } else {
                    items(trips, key = { "trip_${it.id}" }) { trip ->
                        TripItemRow(trip = trip, onDeleteClick = {
                            itemToDelete = Triple("Carrera", "$ %,.0f (${trip.platformType.displayName})".format(Locale.US, trip.netAmount)) { onDeleteTrip(trip.id) }
                        })
                    }
                }

                // Tanqueos
                item {
                    Text("⛽ Tanqueos de Combustible (${fuelings.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                if (fuelings.isEmpty()) {
                    item { Text("No hay abastecimientos registrados en este turno.", style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
                } else {
                    items(fuelings, key = { "fuel_${it.id}" }) { fueling ->
                        FuelingItemRow(fueling = fueling, onDeleteClick = {
                            itemToDelete = Triple("Tanqueo", "$ %,.0f (${fueling.fuelType.displayName})".format(Locale.US, fueling.amountPaid)) { onDeleteFueling(fueling.id) }
                        })
                    }
                }

                // Gastos
                item {
                    Text("🧾 Gastos Operativos (${expenses.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                if (expenses.isEmpty()) {
                    item { Text("No hay gastos registrados en este turno.", style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
                } else {
                    items(expenses, key = { "exp_${it.id}" }) { expense ->
                        ExpenseItemRow(expense = expense, onDeleteClick = {
                            itemToDelete = Triple("Gasto", "$ %,.0f (${expense.concept.displayName})".format(Locale.US, expense.amount)) { onDeleteExpense(expense.id) }
                        })
                    }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }

    itemToDelete?.let { (type, desc, action) ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            containerColor = DeepObsidianSurface,
            title = { Text("¿Eliminar $type?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas eliminar permanentemente $desc? Los totales del turno se recalcularán automáticamente.", color = TextSecondary) },
            confirmButton = {
                Button(onClick = { action(); itemToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = StatusRed)) {
                    Text("ELIMINAR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("CANCELAR", color = TextSecondary) }
            }
        )
    }
}
