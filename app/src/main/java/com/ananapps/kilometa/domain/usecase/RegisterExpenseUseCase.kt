// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/RegisterExpenseUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para el registro de egresos operativos incidentales durante el turno
// (lavadero, peajes, alimentación u otros imprevistos).
// Aplica Domain Guards garantizando montos estrictamente positivos antes de persistir.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import java.util.UUID
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE REGISTRO DE GASTOS CON DOMAIN GUARDS
// ------------------------------------------------------------------------------
/**
 * Valida y persiste un egreso operacional en la jornada del conductor.
 */
class RegisterExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {

    /**
     * Valida y almacena un gasto incidental en ruta.
     *
     * @param shiftId Identificador del turno activo.
     * @param concept Concepto o categoría del egreso (WASH, TOLL, FOOD, OTHER).
     * @param amount Monto pagado en COP (estrictamente > 0.0).
     * @param timestamp Momento Unix del desembolso en milisegundos.
     * @return [Expense] instanciado y persistido.
     * @throws IllegalArgumentException si el monto no es estrictamente positivo.
     */
    suspend operator fun invoke(
        shiftId: String,
        concept: ExpenseConcept,
        amount: Double,
        timestamp: Long = System.currentTimeMillis()
    ): Expense {
        // Domain Guards
        require(shiftId.isNotBlank()) { "El identificador del turno no puede estar vacío" }
        require(amount > 0.0) { "El monto del gasto operativo debe ser estrictamente mayor a cero" }

        val expense = Expense(
            id = UUID.randomUUID().toString(),
            shiftId = shiftId,
            concept = concept,
            amount = amount,
            timestamp = timestamp
        )

        expenseRepository.addExpense(expense)
        return expense
    }
}
