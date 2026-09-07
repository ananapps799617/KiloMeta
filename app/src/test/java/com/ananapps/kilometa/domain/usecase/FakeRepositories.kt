// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/FakeRepositories.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Fakes de prueba in-memory estrictamente tipados para repositorios de Dominio.
// Permiten pruebas unitarias de casos de uso sin mocks dinámicos ni dependencias pesadas.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.Shift
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.ShiftStatus
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import com.ananapps.kilometa.domain.repository.FuelingRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// ------------------------------------------------------------------------------
// BLOQUE 1: FAKE DE REPOSITORIO DE CARRERAS
// ------------------------------------------------------------------------------
class FakeTripRepository : TripRepository {
    val savedTrips = mutableListOf<Trip>()
    override fun getTripsByShift(shiftId: String): Flow<List<Trip>> = flowOf(savedTrips)
    override suspend fun addTrip(trip: Trip) { savedTrips.add(trip) }
    override suspend fun deleteTrip(tripId: String) { savedTrips.removeAll { it.id == tripId } }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: FAKE DE REPOSITORIO DE COMBUSTIBLE
// ------------------------------------------------------------------------------
class FakeFuelingRepository : FuelingRepository {
    val savedFuelings = mutableListOf<Fueling>()
    override fun getFuelingsByShift(shiftId: String): Flow<List<Fueling>> = flowOf(savedFuelings)
    override suspend fun addFueling(fueling: Fueling) { savedFuelings.add(fueling) }
    override suspend fun deleteFueling(fuelingId: String) { savedFuelings.removeAll { it.id == fuelingId } }
}

// ------------------------------------------------------------------------------
// BLOQUE 3: FAKE DE REPOSITORIO DE GASTOS OPERATIVOS
// ------------------------------------------------------------------------------
class FakeExpenseRepository : ExpenseRepository {
    val savedExpenses = mutableListOf<Expense>()
    override fun getExpensesByShift(shiftId: String): Flow<List<Expense>> = flowOf(savedExpenses)
    override suspend fun addExpense(expense: Expense) { savedExpenses.add(expense) }
    override suspend fun deleteExpense(expenseId: String) { savedExpenses.removeAll { it.id == expenseId } }
    override fun getAllExpenses(): Flow<List<Expense>> = flowOf(savedExpenses)
}

// ------------------------------------------------------------------------------
// BLOQUE 4: FAKE DE REPOSITORIO DE TURNOS
// ------------------------------------------------------------------------------
class FakeShiftRepository : ShiftRepository {
    val shifts = mutableListOf<Shift>()
    val completedSummaries = mutableListOf<ShiftFinancialSummary>()
    val allTripsList = mutableListOf<Trip>()
    val allFuelingsList = mutableListOf<Fueling>()

    override fun getActiveShift(): Flow<Shift?> = flowOf(shifts.find { it.isActive })
    override fun getActiveShiftFinancialSummary(): Flow<ShiftFinancialSummary?> = flowOf(null)
    override fun getAllShifts(): Flow<List<Shift>> = flowOf(shifts)
    override fun getCompletedShiftsHistory(): Flow<List<ShiftFinancialSummary>> = flowOf(completedSummaries)
    override fun getAllTrips(): Flow<List<Trip>> = flowOf(allTripsList)
    override fun getAllFuelings(): Flow<List<Fueling>> = flowOf(allFuelingsList)

    override suspend fun startShift(shift: Shift) { shifts.add(shift) }
    override suspend fun closeShift(shiftId: String, endOdometer: Int, endTime: Long, notes: String?) {
        val idx = shifts.indexOfFirst { it.id == shiftId }
        if (idx != -1) {
            shifts[idx] = shifts[idx].copy(
                endOdometer = endOdometer,
                endTime = endTime,
                status = ShiftStatus.COMPLETED,
                notes = notes
            )
        }
    }
    override suspend fun updateShift(shift: Shift) {}
    override suspend fun deleteShift(shiftId: String) { shifts.removeAll { it.id == shiftId } }
}
