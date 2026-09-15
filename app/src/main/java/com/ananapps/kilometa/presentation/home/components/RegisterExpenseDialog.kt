// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterExpenseDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante centrado de 3 capas para registro de gastos operativos (Orden #037).
// Cabecera roja fija, cuerpo scrollable para concepto y monto, y pie fijo con botón GUARDAR GASTO (56dp).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.domain.model.Place
import com.ananapps.kilometa.domain.model.PlaceCategory
import com.ananapps.kilometa.domain.model.WalletType
import com.ananapps.kilometa.presentation.common.PlaceSelectorField
import com.ananapps.kilometa.presentation.theme.*

private val ExpenseRedHeader = Color(0xFFC62828)

@Composable
fun RegisterExpenseDialog(
    onDismiss: () -> Unit,
    savedPlaces: List<Place> = emptyList(),
    onConfirmExpense: (concept: ExpenseConcept, amount: Double, paymentSource: WalletType, timestamp: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedConcept by remember { mutableStateOf(ExpenseConcept.RENT) }
    var selectedCustomName by remember { mutableStateOf<String?>(null) }
    var customCategories by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var paymentSource by remember { mutableStateOf(WalletType.CASH_PHYSICAL) }
    var amountText by remember { mutableStateOf("") }
    var placeLocationText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasDirtyData = amountText.isNotBlank() || notesText.isNotBlank() || placeLocationText.isNotBlank()
    val amountNumeric = amountText.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
    val isFormValid = amountNumeric > 0.0

    Dialog(
        onDismissRequest = { if (hasDirtyData) showDiscardDialog = true else onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .border(1.dp, DeepObsidianBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = DeepObsidianSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // CAPA 1: CABECERA FIJA
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ExpenseRedHeader,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "💸 Registrar Gasto", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                        IconButton(onClick = { if (hasDirtyData) showDiscardDialog = true else onDismiss() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CAPA 2: CUERPO DESPLAZABLE INTERNO
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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

                Spacer(modifier = Modifier.height(14.dp))

                // CAPA 3: BOTONERA DE ACCIÓN FIJA (56dp)
                Button(
                    onClick = {
                        if (isFormValid) {
                            val resolvedConcept = selectedConcept
                            onConfirmExpense(resolvedConcept, amountNumeric, paymentSource, selectedTimestamp)
                            onDismiss()
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRedHeader, disabledContainerColor = ExpenseRedHeader.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = if (amountNumeric > 0.0) "GUARDAR GASTO (- $ %,.0f)".format(amountNumeric) else "GUARDAR GASTO",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
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

    if (showDiscardDialog) {
        DiscardChangesDialog(
            title = "¿Descartar Gasto?",
            message = "Tienes datos digitados sin guardar. ¿Deseas salir y descartar este gasto?",
            onConfirmDiscard = {
                showDiscardDialog = false
                onDismiss()
            },
            onDismissRequest = { showDiscardDialog = false }
        )
    }
}
