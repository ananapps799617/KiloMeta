// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterExpenseSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet express para captura de gastos operativos incidentales en ruta.
// Permite imputar egresos por lavado, peajes, comida u otros conceptos con chips de acceso ágil.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: HOJA MODAL DE GASTOS INCIDENTALES
// ------------------------------------------------------------------------------
import com.ananapps.kilometa.domain.model.WalletType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterExpenseSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirmExpense: (concept: ExpenseConcept, amount: Double, paymentSource: WalletType) -> Unit
) {
    var selectedConcept by remember { mutableStateOf(ExpenseConcept.WASH) }
    var paymentSource by remember { mutableStateOf(WalletType.CASH_PHYSICAL) }
    var expenseAmount by remember { mutableDoubleStateOf(10_000.0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidianSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "REGISTRAR GASTO OPERATIVO",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = StatusRed
            )

            Text(
                text = "Concepto de Egreso",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpenseConcept.entries.forEach { concept ->
                    FilterChip(
                        selected = selectedConcept == concept,
                        onClick = { selectedConcept = concept },
                        label = { Text(concept.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusRed,
                            selectedLabelColor = TextPrimary
                        )
                    )
                }
            }

            Text(
                text = "Origen de Fondos",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = paymentSource == WalletType.CASH_PHYSICAL,
                    onClick = { paymentSource = WalletType.CASH_PHYSICAL },
                    label = { Text("💵 Efectivo Físico") }
                )
                FilterChip(
                    selected = paymentSource == WalletType.DIGITAL_WALLET,
                    onClick = { paymentSource = WalletType.DIGITAL_WALLET },
                    label = { Text("🟣 Billetera Digital") }
                )
            }

            Text(
                text = "Monto: ${formatCop(expenseAmount)}",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5_000.0, 10_000.0, 15_000.0, 20_000.0).forEach { chipAmount ->
                    SuggestionChip(
                        onClick = { expenseAmount = chipAmount },
                        label = { Text("$${chipAmount.toInt() / 1000}k") }
                    )
                }
            }

            Button(
                onClick = {
                    onConfirmExpense(selectedConcept, expenseAmount, paymentSource)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
            ) {
                Text(
                    text = "Guardar Gasto (-${formatCop(expenseAmount)})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        }
    }
}

private fun formatCop(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return String.format(Locale.GERMANY, "$%,d COP", rounded)
}
