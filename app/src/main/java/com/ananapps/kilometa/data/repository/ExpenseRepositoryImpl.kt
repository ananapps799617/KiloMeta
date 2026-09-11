// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/ExpenseRepositoryImpl.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación del repositorio de gastos operativos en ruta conectado a Room.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.core.dispatcher.CoroutineDispatchers
import com.ananapps.kilometa.data.local.dao.ExpenseDao
import com.ananapps.kilometa.data.local.dao.PendingDeletionDao
import com.ananapps.kilometa.data.local.entity.PendingDeletionEntity
import com.ananapps.kilometa.data.mapper.toDomain
import com.ananapps.kilometa.data.mapper.toEntity
import com.ananapps.kilometa.data.remote.firebase.FirestoreDataSource
import com.ananapps.kilometa.data.remote.mapper.toDto
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.repository.AuthRepository
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
    private val pendingDeletionDao: PendingDeletionDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val authRepository: AuthRepository,
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
            pendingDeletionDao.deleteByEntityId(expense.id)
            val user = authRepository.getCurrentUser()
            if (user != null) {
                firestoreDataSource.saveExpenses(user.uid, listOf(expense.toEntity().toDto()))
            }
        }
    }

    override suspend fun updateExpense(expense: Expense) {
        withContext(dispatchers.io) {
            expenseDao.updateExpense(expense.toEntity())
            pendingDeletionDao.deleteByEntityId(expense.id)
            val user = authRepository.getCurrentUser()
            if (user != null) {
                firestoreDataSource.saveExpenses(user.uid, listOf(expense.toEntity().toDto()))
            }
        }
    }

    override suspend fun deleteExpense(expenseId: String) {
        withContext(dispatchers.io) {
            expenseDao.deleteExpense(expenseId)
            pendingDeletionDao.insert(PendingDeletionEntity(expenseId, "EXPENSE"))
            val user = authRepository.getCurrentUser()
            if (user != null) {
                val result = firestoreDataSource.deleteExpense(user.uid, expenseId)
                if (result.isSuccess) {
                    pendingDeletionDao.deleteByEntityId(expenseId)
                }
            }
        }
    }

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses()
            .map { it.toDomain() }
            .flowOn(dispatchers.io)
    }
}
