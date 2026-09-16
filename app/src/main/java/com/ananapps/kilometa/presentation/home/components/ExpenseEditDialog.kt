// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/ExpenseEditDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo de edición de gastos operativos (Orden #041).
// Redirige al formulario unificado RegisterExpenseDialog garantizando 100% de paridad simétrica.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.runtime.Composable
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.Place

@Composable
fun ExpenseEditDialog(
    expense: Expense,
    onDismiss: () -> Unit,
    savedPlaces: List<Place> = emptyList(),
    onConfirm: (Expense) -> Unit
) {
    RegisterExpenseDialog(
        existingExpense = expense,
        savedPlaces = savedPlaces,
        onDismiss = onDismiss,
        onConfirmUpdate = onConfirm
    )
}
