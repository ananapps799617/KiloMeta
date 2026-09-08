// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/expenses/components/RegisterRouteExpenseSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet ergonómico para registrar egresos incidentales en ruta.
// Permite clasificar lavadas, despinchadas, alimentos, peajes con touch targets >= 56dp.
// ==============================================================================

package com.ananapps.kilometa.presentation.expenses.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterRouteExpenseSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirm: (concept: ExpenseConcept, amount: Double) -> Unit
) {
    var selectedConcept by remember { mutableStateOf(ExpenseConcept.FOOD) }
    var amountText by remember { mutableStateOf("10000") }
    val amountNumeric = amountText.toDoubleOrNull() ?: 0.0

    val copFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidianSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "REGISTRAR GASTO EN RUTA",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = StatusRed
            )

            Text(text = "Concepto del Gasto", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val concepts = ExpenseConcept.entries
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    concepts.take(3).forEach { concept ->
                        FilterChip(
                            selected = selectedConcept == concept,
                            onClick = { selectedConcept = concept },
                            label = { Text("${concept.icon} ${concept.displayName}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusRed,
                                selectedLabelColor = DeepObsidianBackground
                            )
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    concepts.drop(3).forEach { concept ->
                        FilterChip(
                            selected = selectedConcept == concept,
                            onClick = { selectedConcept = concept },
                            label = { Text("${concept.icon} ${concept.displayName}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusRed,
                                selectedLabelColor = DeepObsidianBackground
                            )
                        )
                    }
                }
            }

            // Monto editable
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Text("$", color = StatusRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                label = { Text("Valor del Gasto ($ COP)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Chips rápidos (+5k, +10k, +15k, +20k)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5_000, 10_000, 15_000, 20_000).forEach { chipVal ->
                    SuggestionChip(
                        onClick = { amountText = chipVal.toString() },
                        label = { Text("+${chipVal / 1000}k", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    if (amountNumeric > 0.0) {
                        onConfirm(selectedConcept, amountNumeric)
                        onDismiss()
                    }
                },
                enabled = amountNumeric > 0.0,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
            ) {
                Text(
                    text = "Guardar Gasto (-${copFormat.format(amountNumeric)})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = DeepObsidianBackground
                )
            }
        }
    }
}
