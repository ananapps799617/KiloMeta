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
import com.ananapps.kilometa.data.local.dao.ShiftDao
import com.ananapps.kilometa.data.mapper.toDomain
import com.ananapps.kilometa.data.mapper.toEntity
import com.ananapps.kilometa.data.mapper.toFinancialSummary
import com.ananapps.kilometa.domain.model.Shift
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.ShiftStatus
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

    override fun getAllShifts(): Flow<List<Shift>> {
        return shiftDao.getAllShifts()
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

    override suspend fun updateShift(shift: Shift) {
        withContext(dispatchers.io) {
            shiftDao.updateShift(shift.toEntity())
        }
    }

    override suspend fun deleteShift(shiftId: String) {
        withContext(dispatchers.io) {
            shiftDao.deleteShift(shiftId)
        }
    }
}
