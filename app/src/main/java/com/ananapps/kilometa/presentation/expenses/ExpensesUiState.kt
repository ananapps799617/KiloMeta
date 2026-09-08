// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/expenses/ExpensesUiState.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Estado de UI inmutable para la pantalla independiente de Gastos en Ruta.
// ==============================================================================

package com.ananapps.kilometa.presentation.expenses

import com.ananapps.kilometa.domain.model.Expense

data class ExpensesUiState(
    val isLoading: Boolean = true,
    val expenses: List<Expense> = emptyList(),
    val totalExpensesCop: Double = 0.0,
    val activeShiftId: String? = null
)
