// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/dao/ExpenseDao.kt
// DESCRIPCIÓN PEDAGÓGICA:
// DAO para la tabla 'expenses'. Administra las operaciones CRUD para gastos
// incidentales u operativos del conductor en ruta.
// ==============================================================================

package com.ananapps.kilometa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ananapps.kilometa.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------------
// BLOQUE 1: CONTRATO DE ACCESO A DATOS PARA GASTOS OPERATIVOS
// ------------------------------------------------------------------------------
@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE shift_id = :shiftId ORDER BY timestamp DESC")
    fun getExpensesByShift(shiftId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    suspend fun getExpenseById(expenseId: String): ExpenseEntity?

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: String)

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
}
