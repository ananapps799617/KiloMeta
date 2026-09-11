// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/ExpenseEditDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal ergonómico para la edición de gastos operativos en ruta.
// Permite modificar el concepto/categoría del gasto y el monto en $ COP.
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

@Composable
fun ExpenseEditDialog(
    expense: Expense,
    onDismiss: () -> Unit,
    onConfirm: (Expense) -> Unit
) {
    var concept by remember { mutableStateOf(expense.concept) }
    var amountText by remember { mutableStateOf(expense.amount.toLong().toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepObsidianSurface,
        title = { Text("Editar Gasto Operativo", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Concepto del Gasto:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)

                // Selectores en 2 filas de 3 conceptos
                val concepts = ExpenseConcept.entries
                val row1 = concepts.take(3)
                val row2 = concepts.drop(3)

                listOf(row1, row2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowItems.forEach { c ->
                            val isSelected = concept == c
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { concept = c },
                                color = if (isSelected) ElectricBluePrimary.copy(alpha = 0.25f) else DeepObsidianSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) ElectricBluePrimary else DeepObsidianSurfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(text = c.icon, fontSize = 16.sp)
                                    Text(
                                        text = c.displayName,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) ElectricBluePrimary else TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Monto del Gasto ($ COP)") },
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
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        errorMessage = "El monto debe ser mayor a cero"
                        return@Button
                    }
                    onConfirm(expense.copy(concept = concept, amount = amount))
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
