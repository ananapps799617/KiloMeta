// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/ExpenseRepositoryImpl.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación del repositorio de gastos operativos en ruta conectado a Room.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.core.dispatcher.CoroutineDispatchers
import com.ananapps.kilometa.data.local.dao.ExpenseDao
import com.ananapps.kilometa.data.mapper.toDomain
import com.ananapps.kilometa.data.mapper.toEntity
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: IMPLEMENTACIÓN DEL REPOSITORIO DE GASTOS
// ------------------------------------------------------------------------------
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val dispatchers: CoroutineDispatchers
) : ExpenseRepository {

    override fun getExpensesByShift(shiftId: String): Flow<List<Expense>> {
        return expenseDao.getExpensesByShift(shiftId)
            .map { it.toDomain() }
            .flowOn(dispatchers.io)
    }

    override suspend fun addExpense(expense: Expense) {
        withContext(dispatchers.io) {
            expenseDao.insertExpense(expense.toEntity())
        }
    }

    override suspend fun deleteExpense(expenseId: String) {
        withContext(dispatchers.io) {
            expenseDao.deleteExpense(expenseId)
        }
    }
}
