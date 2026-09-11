// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/UpdateExpenseUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para la modificación y ajuste de un gasto operativo en ruta.
// Valida que el monto sea estrictamente positivo (> 0).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE ACTUALIZACIÓN DE GASTO
// ------------------------------------------------------------------------------
/**
 * Modifica un gasto operativo existente validando sus montos.
 */
class UpdateExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {

    /**
     * Ejecuta la actualización de un gasto.
     *
     * @param expense Gasto con datos actualizados.
     */
    suspend operator fun invoke(expense: Expense) {
        require(expense.id.isNotBlank()) { "El identificador del gasto no puede estar vacío" }
        require(expense.amount > 0.0) { "El monto del gasto debe ser mayor a cero" }

        expenseRepository.updateExpense(expense)
    }
}
