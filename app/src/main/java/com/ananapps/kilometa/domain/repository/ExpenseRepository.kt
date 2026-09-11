// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/ExpenseRepository.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contrato de Dominio para la administración de gastos operativos en ruta.
// ==============================================================================

package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.Expense
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------------
// BLOQUE 1: INTERFAZ DEL REPOSITORIO DE GASTOS
// ------------------------------------------------------------------------------
/**
 * Define el contrato de persistencia y lectura de egresos operativos.
 */
interface ExpenseRepository {

    /**
     * Emite los gastos incurridos durante un turno de trabajo.
     */
    fun getExpensesByShift(shiftId: String): Flow<List<Expense>>

    /**
     * Registra un nuevo gasto operativo en el turno.
     */
    suspend fun addExpense(expense: Expense)

    /**
     * Elimina un gasto operativo por su ID.
     */
    suspend fun deleteExpense(expenseId: String)

    /**
     * Actualiza la información de un gasto existente.
     */
    suspend fun updateExpense(expense: Expense)

    /**
     * Emite la lista completa de todos los gastos operativos históricos registrados.
     */
    fun getAllExpenses(): Flow<List<Expense>>
}
