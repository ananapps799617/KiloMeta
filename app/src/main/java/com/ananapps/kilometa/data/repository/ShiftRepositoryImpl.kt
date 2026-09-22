// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/ShiftRepositoryImpl.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación del repositorio de Turnos conectado a Room SQLite y Coroutines.
// Utiliza ShiftDao para persistencia y transformaciones reactivas mediante Flow.
// Confinado a operaciones en hilo de fondo usando CoroutineDispatchers.io.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.core.dispatcher.CoroutineDispatchers
import com.ananapps.kilometa.data.local.dao.ExpenseDao
import com.ananapps.kilometa.data.local.dao.FuelingDao
import com.ananapps.kilometa.data.local.dao.PendingDeletionDao
import com.ananapps.kilometa.data.local.dao.ShiftDao
import com.ananapps.kilometa.data.local.dao.TripDao
import com.ananapps.kilometa.data.local.entity.PendingDeletionEntity
import com.ananapps.kilometa.data.mapper.toDomain
import com.ananapps.kilometa.data.mapper.toEntity
import com.ananapps.kilometa.data.mapper.toFinancialSummary
import com.ananapps.kilometa.data.mapper.toCashSettlement
import com.ananapps.kilometa.data.remote.firebase.FirestoreDataSource
import com.ananapps.kilometa.data.remote.mapper.toDto
import com.ananapps.kilometa.domain.model.CashSettlementSummary
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.Shift
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.ShiftStatus
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.domain.repository.AuthRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: IMPLEMENTACIÓN DEL CONTRATO DE REPOSITORIO DE TURNOS
// ------------------------------------------------------------------------------
@Singleton
class ShiftRepositoryImpl @Inject constructor(
    private val shiftDao: ShiftDao,
    private val tripDao: TripDao,
    private val fuelingDao: FuelingDao,
    private val expenseDao: ExpenseDao,
    private val pendingDeletionDao: PendingDeletionDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val authRepository: AuthRepository,
    private val dispatchers: CoroutineDispatchers
) : ShiftRepository {

    override fun getActiveShift(): Flow<Shift?> {
        return shiftDao.getActiveShift()
            .map { it?.toDomain() }
            .flowOn(dispatchers.io)
    }

    override fun getActiveShiftFinancialSummary(): Flow<ShiftFinancialSummary?> {
        return shiftDao.getActiveShiftWithDetails()
            .map { it?.toFinancialSummary() }
            .flowOn(dispatchers.io)
    }

    override fun getActiveShiftCashSettlement(): Flow<CashSettlementSummary?> {
        return shiftDao.getActiveShiftWithDetails()
            .map { it?.toCashSettlement() }
            .flowOn(dispatchers.io)
    }

    override fun getActiveShiftTrips(): Flow<List<Trip>> {
        return shiftDao.getActiveShiftWithDetails()
            .map { details ->
                details?.trips?.map { it.toDomain() }?.sortedByDescending { it.timestamp } ?: emptyList()
            }
            .flowOn(dispatchers.io)
    }

    override fun getAllShifts(): Flow<List<Shift>> {
        return shiftDao.getAllShifts()
            .map { it.toDomain() }
            .flowOn(dispatchers.io)
    }

    override fun getCompletedShiftsHistory(): Flow<List<ShiftFinancialSummary>> {
        return shiftDao.getCompletedShiftsWithDetails()
            .map { list -> list.map { it.toFinancialSummary() } }
            .flowOn(dispatchers.io)
    }

    override fun getAllShiftsHistoryWithDetails(): Flow<List<ShiftFinancialSummary>> {
        return shiftDao.getAllShiftsWithDetails()
            .map { list -> list.map { it.toFinancialSummary() } }
            .flowOn(dispatchers.io)
    }

    override fun getAllTrips(): Flow<List<Trip>> {
        return tripDao.getAllTrips()
            .map { it.toDomain() }
            .flowOn(dispatchers.io)
    }

    override fun getAllFuelings(): Flow<List<Fueling>> {
        return fuelingDao.getAllFuelings()
            .map { it.toDomain() }
            .flowOn(dispatchers.io)
    }

    override suspend fun startShift(shift: Shift) {
        withContext(dispatchers.io) {
            shiftDao.insertShift(shift.toEntity())
        }
    }

    override suspend fun closeShift(
        shiftId: String,
        endOdometer: Int,
        endTime: Long,
        notes: String?
    ) {
        withContext(dispatchers.io) {
            val existing = shiftDao.getShiftById(shiftId) ?: return@withContext
            val updated = existing.copy(
                endTime = endTime,
                endOdometer = endOdometer,
                status = ShiftStatus.COMPLETED.name,
                notes = notes ?: existing.notes
            )
            shiftDao.updateShift(updated)
        }
    }

    override suspend fun closeAllActiveShifts(endOdometer: Int, endTime: Long) {
        withContext(dispatchers.io) {
            shiftDao.closeAllActiveShifts(endOdometer, endTime)
        }
    }

    override suspend fun updateShift(shift: Shift) {
        withContext(dispatchers.io) {
            shiftDao.updateShift(shift.toEntity())
            val user = authRepository.getCurrentUser()
            if (user != null) {
                firestoreDataSource.saveShifts(user.uid, listOf(shift.toEntity().toDto()))
            }
        }
    }

    override suspend fun deleteShift(shiftId: String) {
        withContext(dispatchers.io) {
            // 1. Recopilar IDs de dependencias antes del borrado en cascada
            val tripIds = tripDao.getTripIdsByShift(shiftId)
            val expenseIds = expenseDao.getExpenseIdsByShift(shiftId)
            val fuelingIds = fuelingDao.getFuelingIdsByShift(shiftId)

            // 2. Registrar lápidas preventivas
            pendingDeletionDao.insert(PendingDeletionEntity(shiftId, "SHIFT"))
            tripIds.forEach { pendingDeletionDao.insert(PendingDeletionEntity(it, "TRIP")) }
            expenseIds.forEach { pendingDeletionDao.insert(PendingDeletionEntity(it, "EXPENSE")) }
            fuelingIds.forEach { pendingDeletionDao.insert(PendingDeletionEntity(it, "FUELING")) }

            // 3. Borrado local en Room
            shiftDao.deleteShift(shiftId)

            // 4. Purga remota en Firestore
            val user = authRepository.getCurrentUser()
            if (user != null) {
                val shiftResult = firestoreDataSource.deleteShift(user.uid, shiftId)
                if (shiftResult.isSuccess) pendingDeletionDao.deleteByEntityId(shiftId)
                tripIds.forEach { id ->
                    val r = firestoreDataSource.deleteTrip(user.uid, id)
                    if (r.isSuccess) pendingDeletionDao.deleteByEntityId(id)
                }
                expenseIds.forEach { id ->
                    val r = firestoreDataSource.deleteExpense(user.uid, id)
                    if (r.isSuccess) pendingDeletionDao.deleteByEntityId(id)
                }
                fuelingIds.forEach { id ->
                    val r = firestoreDataSource.deleteFueling(user.uid, id)
                    if (r.isSuccess) pendingDeletionDao.deleteByEntityId(id)
                }
            }
        }
    }

    override fun getLastKnownOdometer(): Flow<Int?> {
        return shiftDao.getLastKnownOdometer()
    }

    override suspend fun getShiftsForDay(dayStart: Long, dayEnd: Long): List<Shift> {
        return withContext(dispatchers.io) {
            shiftDao.getShiftsForDay(dayStart, dayEnd).map { it.toDomain() }
        }
    }

    override suspend fun consolidateShifts(masterShift: Shift, secondaryShiftIds: List<String>) {
        withContext(dispatchers.io) {
            shiftDao.updateShift(masterShift.toEntity())
            val user = authRepository.getCurrentUser()
            if (user != null) {
                firestoreDataSource.saveShifts(user.uid, listOf(masterShift.toEntity().toDto()))
            }
            for (secId in secondaryShiftIds) {
                shiftDao.reassignTripsShift(secId, masterShift.id)
                shiftDao.reassignFuelingsShift(secId, masterShift.id)
                shiftDao.reassignExpensesShift(secId, masterShift.id)
                shiftDao.deleteShift(secId)
                pendingDeletionDao.insert(PendingDeletionEntity(secId, "SHIFT"))
                if (user != null) {
                    val r = firestoreDataSource.deleteShift(user.uid, secId)
                    if (r.isSuccess) pendingDeletionDao.deleteByEntityId(secId)
                }
            }
        }
    }
}
