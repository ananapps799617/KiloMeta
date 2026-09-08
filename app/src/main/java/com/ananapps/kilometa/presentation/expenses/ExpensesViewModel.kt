// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/expenses/ExpensesViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel para la gestión integral de gastos operativos en ruta (@HiltViewModel).
// Permite registrar lavadas, despinchadas, alimentos, peajes y eliminaciones.
// ==============================================================================

package com.ananapps.kilometa.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.usecase.RegisterExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val shiftRepository: ShiftRepository,
    private val registerExpenseUseCase: RegisterExpenseUseCase
) : ViewModel() {

    val uiState: StateFlow<ExpensesUiState> = combine(
        expenseRepository.getAllExpenses(),
        shiftRepository.getActiveShift()
    ) { allExpenses, activeShift ->
        val total = allExpenses.sumOf { it.amount }
        ExpensesUiState(
            isLoading = false,
            expenses = allExpenses.sortedByDescending { it.timestamp },
            totalExpensesCop = total,
            activeShiftId = activeShift?.id
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ExpensesUiState())

    fun registerExpense(concept: ExpenseConcept, amount: Double) {
        val shiftId = uiState.value.activeShiftId ?: "general_expense"
        viewModelScope.launch {
            registerExpenseUseCase(shiftId, concept, amount)
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expenseId)
        }
    }
}
