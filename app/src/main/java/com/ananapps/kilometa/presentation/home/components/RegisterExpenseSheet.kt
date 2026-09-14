// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterExpenseSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet de alta fidelidad Drivvo para captura de gastos operativos.
// Incorpora cuadrícula ergonómica 2 columnas para conceptos, eliminación de chips
// fijos, cabecera roja #C62828, selector de origen de fondos y Swipe-Down Lockdown.
// Regla sagrada: < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.domain.model.WalletType
import com.ananapps.kilometa.presentation.theme.*

private val ExpenseRedHeader = Color(0xFFC62828)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterExpenseSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirmExpense: (concept: ExpenseConcept, amount: Double, paymentSource: WalletType) -> Unit
) {
    var selectedConcept by remember { mutableStateOf(ExpenseConcept.RENT) }
    var paymentSource by remember { mutableStateOf(WalletType.CASH_PHYSICAL) }
    var amountText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasDirtyData = amountText.isNotBlank() || notesText.isNotBlank()
    val amountNumeric = amountText.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
    val isFormValid = amountNumeric > 0.0

    ModalBottomSheet(
        onDismissRequest = { if (hasDirtyData) showDiscardDialog = true else onDismiss() },
        sheetState = sheetState,
        containerColor = DeepObsidianSurface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera Oficial Drivvo Roja con Flecha y "Gasto"
            Surface(modifier = Modifier.fillMaxWidth(), color = ExpenseRedHeader, shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (hasDirtyData) showDiscardDialog = true else onDismiss() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    Text(text = "Gasto", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White, modifier = Modifier.padding(start = 4.dp))
                }
            }

            // Monto del Egreso en COP
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Text("$", color = ExpenseRedHeader, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp, end = 4.dp)) },
                label = { Text("Valor del Gasto ($ COP)") },
                placeholder = { Text("0") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Cuadrícula Ergonómica 2 Columnas de Conceptos de Gasto
            Text(text = "Concepto de Egreso", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            val concepts = ExpenseConcept.entries
            val rows = concepts.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { concept ->
                            val isSelected = selectedConcept == concept
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable { selectedConcept = concept },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) ExpenseRedHeader.copy(alpha = 0.25f) else DeepObsidianSurfaceVariant,
                                border = if (isSelected) BorderStroke(1.5.dp, ExpenseRedHeader) else null
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(text = concept.icon, fontSize = 18.sp, modifier = Modifier.padding(end = 6.dp))
                                    Text(
                                        text = concept.displayName,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) Color.White else TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Origen de Fondos (Bolsillo de Pago)
            Text(text = "Origen de Fondos", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = paymentSource == WalletType.CASH_PHYSICAL,
                    onClick = { paymentSource = WalletType.CASH_PHYSICAL },
                    label = { Text("💵 Efectivo Físico", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusGreen, selectedLabelColor = Color.Black)
                )
                FilterChip(
                    selected = paymentSource == WalletType.DIGITAL_WALLET,
                    onClick = { paymentSource = WalletType.DIGITAL_WALLET },
                    label = { Text("🟣 Billetera Digital", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF7B1FA2), selectedLabelColor = Color.White)
                )
            }

            // Detalle / Notas Opcionales
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Detalle / Observaciones (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Botón Único Principal GUARDAR
            Button(
                onClick = {
                    if (isFormValid) {
                        onConfirmExpense(selectedConcept, amountNumeric, paymentSource)
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

    if (showDiscardDialog) {
        DiscardChangesDialog(
            title = "¿Descartar Gasto?",
            message = "Tienes un monto ingresado sin guardar. ¿Deseas salir y descartar este gasto?",
            onConfirmDiscard = {
                showDiscardDialog = false
                onDismiss()
            },
            onDismissRequest = { showDiscardDialog = false }
        )
    }
}
