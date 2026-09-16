// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterExpenseDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante unificado para Registro y Edición simétrica de Gastos (Orden #041).
// Utiliza CockpitFloatingScaffold (3 capas) garantizando paridad total y < 280 LOC.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.domain.model.Place
import com.ananapps.kilometa.domain.model.PlaceCategory
import com.ananapps.kilometa.domain.model.WalletType
import com.ananapps.kilometa.presentation.common.CockpitFloatingScaffold
import com.ananapps.kilometa.presentation.common.PlaceSelectorField
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

private val ExpenseRedHeader = Color(0xFFC62828)

@Composable
fun RegisterExpenseDialog(
    onDismiss: () -> Unit,
    savedPlaces: List<Place> = emptyList(),
    existingExpense: Expense? = null,
    onConfirmExpense: (concept: ExpenseConcept, amount: Double, paymentSource: WalletType, timestamp: Long) -> Unit = { _, _, _, _ -> },
    onConfirmUpdate: (Expense) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTimestamp by remember(existingExpense) {
        mutableStateOf(existingExpense?.timestamp ?: System.currentTimeMillis())
    }
    var selectedConcept by remember(existingExpense) {
        mutableStateOf(existingExpense?.concept ?: ExpenseConcept.RENT)
    }
    var selectedCustomName by remember { mutableStateOf<String?>(null) }
    var customCategories by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var paymentSource by remember(existingExpense) {
        mutableStateOf(existingExpense?.paymentSource ?: WalletType.CASH_PHYSICAL)
    }
    var amountText by remember(existingExpense) {
        mutableStateOf(existingExpense?.amount?.toLong()?.toString() ?: "")
    }
    var placeLocationText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    val amountNumeric = amountText.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
    val isFormValid = amountNumeric > 0.0
    val isEditing = existingExpense != null
    val hasDirtyData = if (isEditing) {
        amountNumeric != existingExpense.amount ||
                selectedConcept != existingExpense.concept ||
                paymentSource != existingExpense.paymentSource ||
                selectedTimestamp != existingExpense.timestamp ||
                notesText.isNotBlank()
    } else {
        amountText.isNotBlank() || notesText.isNotBlank() || placeLocationText.isNotBlank()
    }

    val dialogTitle = if (isEditing) "💸 Editar Gasto Operativo" else "💸 Registrar Gasto"
    val buttonText = if (isEditing) {
        "GUARDAR CAMBIOS"
    } else if (amountNumeric > 0.0) {
        "GUARDAR GASTO (- $ %,.0f)".format(Locale.US, amountNumeric)
    } else {
        "GUARDAR GASTO"
    }

    CockpitFloatingScaffold(
        title = dialogTitle,
        headerColor = ExpenseRedHeader,
        onDismiss = onDismiss,
        confirmButtonText = buttonText,
        confirmButtonColor = ExpenseRedHeader,
        isConfirmEnabled = isFormValid,
        onConfirm = {
            if (isFormValid) {
                if (isEditing) {
                    onConfirmUpdate(
                        existingExpense.copy(
                            concept = selectedConcept,
                            amount = amountNumeric,
                            paymentSource = paymentSource,
                            timestamp = selectedTimestamp
                        )
                    )
                } else {
                    onConfirmExpense(selectedConcept, amountNumeric, paymentSource, selectedTimestamp)
                }
                onDismiss()
            }
        },
        hasDirtyData = hasDirtyData,
        modifier = modifier
    ) {
        DateTimePickerRow(
            selectedDateTimeMillis = selectedTimestamp,
            onDateTimeSelected = { selectedTimestamp = it },
            accentColor = ExpenseRedHeader
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Text("$", color = ExpenseRedHeader, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
            },
            label = { Text("Valor del Gasto ($ COP)") },
            placeholder = { Text("0") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(text = "Concepto de Egreso", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        ExpenseCategoryGrid(
            selectedConcept = selectedConcept,
            selectedCustomName = selectedCustomName,
            customCategories = customCategories,
            onSelectConcept = { concept, customName ->
                selectedConcept = concept
                selectedCustomName = customName
                if (customName != null) notesText = customName
            },
            onAddNewClicked = { showAddCategoryDialog = true },
            accentColor = ExpenseRedHeader
        )

        Text(text = "Origen de Fondos", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = paymentSource == WalletType.CASH_PHYSICAL,
                onClick = { paymentSource = WalletType.CASH_PHYSICAL },
                label = { Text("💵 Efectivo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusGreen, selectedLabelColor = Color.Black)
            )
            FilterChip(
                selected = paymentSource == WalletType.DIGITAL_WALLET,
                onClick = { paymentSource = WalletType.DIGITAL_WALLET },
                label = { Text("🟣 Digital", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF7B1FA2), selectedLabelColor = Color.White)
            )
        }

        PlaceSelectorField(
            label = "Lugar / Establecimiento (Opcional)",
            currentValue = placeLocationText,
            places = savedPlaces,
            categoryFilter = listOf(PlaceCategory.CAR_WASH, PlaceCategory.HOME, PlaceCategory.RESTAURANT, PlaceCategory.OTHER),
            onPlaceSelected = { place ->
                placeLocationText = place.name
                if (notesText.isBlank()) notesText = place.name
            },
            onValueChanged = { placeLocationText = it },
            placeholder = "Ej: Lavadero Express, Parqueadero Calle 72"
        )

        OutlinedTextField(
            value = notesText,
            onValueChange = { notesText = it },
            label = { Text("Detalle / Observaciones (Opcional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Nueva Categoría de Gasto", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    label = { Text("Nombre del concepto") },
                    placeholder = { Text("Ej: Parqueadero, Lavado...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sanitized = newCatName.trim()
                        if (sanitized.isNotBlank()) {
                            customCategories = customCategories + (sanitized to "🏷️")
                            selectedConcept = ExpenseConcept.OTHER
                            selectedCustomName = sanitized
                            notesText = sanitized
                            newCatName = ""
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                ) {
                    Text("Agregar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = DeepObsidianSurface
        )
    }
}
