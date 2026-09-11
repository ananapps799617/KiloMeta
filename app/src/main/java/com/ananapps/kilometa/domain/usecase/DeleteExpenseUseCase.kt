// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/DeleteExpenseUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para eliminar un gasto operativo en ruta.
// Valida identificador y coordina el borrado con lápidas anti-resurrección.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.repository.ExpenseRepository
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE ELIMINACIÓN DE GASTO
// ------------------------------------------------------------------------------
/**
 * Elimina un gasto operativo de ruta.
 */
class DeleteExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {

    /**
     * Ejecuta la eliminación de un gasto por su ID.
     *
     * @param expenseId Identificador único del gasto.
     */
    suspend operator fun invoke(expenseId: String) {
        require(expenseId.isNotBlank()) { "El identificador del gasto no puede estar en blanco" }
        expenseRepository.deleteExpense(expenseId)
    }
}
