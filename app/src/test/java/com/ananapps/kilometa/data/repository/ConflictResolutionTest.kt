// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/data/repository/ConflictResolutionTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para la política de resolución de conflictos
// Last-Write-Wins (LWW) aplicada en la sincronización bidireccional de jornadas.
// Valida la convergencia determinista entre la nube y la base de datos local SQLite.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.data.local.dao.ShiftDao
import com.ananapps.kilometa.data.local.entity.ShiftEntity
import com.ananapps.kilometa.data.local.relation.ShiftWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

// ------------------------------------------------------------------------------
// BLOQUE 1: IMPLEMENTACIÓN FAKE DE SHIFTDAO EN MEMORIA PARA PRUEBAS
// ------------------------------------------------------------------------------
class FakeShiftDao : ShiftDao {
    private val shifts = mutableMapOf<String, ShiftEntity>()
    var insertCount = 0
    var updateCount = 0

    override suspend fun insertShift(shift: ShiftEntity) {
        shifts[shift.id] = shift
        insertCount++
    }

    override suspend fun updateShift(shift: ShiftEntity) {
        shifts[shift.id] = shift
        updateCount++
    }

    override fun getActiveShift(): Flow<ShiftEntity?> {
        val active = shifts.values.firstOrNull { it.status == "ACTIVE" }
        return flowOf(active)
    }

    override fun getActiveShiftWithDetails(): Flow<ShiftWithDetails?> = flowOf(null)

    override fun getShiftWithDetailsById(shiftId: String): Flow<ShiftWithDetails?> = flowOf(null)

    override suspend fun getShiftById(shiftId: String): ShiftEntity? = shifts[shiftId]

    override fun getAllShifts(): Flow<List<ShiftEntity>> = flowOf(shifts.values.toList())

    override fun getAllShiftsWithDetails(): Flow<List<ShiftWithDetails>> = flowOf(emptyList())

    override fun getCompletedShiftsWithDetails(): Flow<List<ShiftWithDetails>> = flowOf(emptyList())

    override suspend fun deleteShift(shiftId: String) {
        shifts.remove(shiftId)
    }

    override suspend fun deleteAllShifts() {
        shifts.clear()
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: EJECUTOR DE LA REGLA LWW SEGÚN SYNCREPOSITORYIMPL
// ------------------------------------------------------------------------------
/**
 * Emula la resolución de conflictos aplicada en el paso pull de SyncRepositoryImpl.
 */
class ShiftConflictResolver(private val shiftDao: ShiftDao) {

    suspend fun applyRemoteShift(remoteShift: ShiftEntity) {
        val localShift = shiftDao.getShiftById(remoteShift.id)
        if (localShift == null) {
            shiftDao.insertShift(remoteShift)
        } else if (remoteShift.status == "COMPLETED" && localShift.status == "ACTIVE") {
            // Regla 1: Remoto completó la jornada (LWW gana estado COMPLETED)
            shiftDao.updateShift(remoteShift)
        } else if (remoteShift.endOdometer != null && localShift.endOdometer == null) {
            // Regla 2: Remoto posee odómetro final de cierre de jornada
            shiftDao.updateShift(remoteShift)
        }
        // En cualquier otro caso, se preserva el estado local del dispositivo
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 3: SUITE DE PRUEBAS DE RESOLUCIÓN DE CONFLICTOS LWW
// ------------------------------------------------------------------------------
class ConflictResolutionTest {

    private lateinit var shiftDao: FakeShiftDao
    private lateinit var resolver: ShiftConflictResolver

    @Before
    fun setUp() {
        shiftDao = FakeShiftDao()
        resolver = ShiftConflictResolver(shiftDao)
    }

    @Test
    fun `cuando shift no existe localmente se inserta shift remoto como nuevo registro`() = runBlocking {
        // GIVEN: Una jornada remota nueva sin equivalente en el dispositivo
        val remoteShift = ShiftEntity(
            id = "shift-001",
            startTime = 1700000000000L,
            startOdometer = 50000,
            dailyQuotaTarget = 100000.0,
            status = "ACTIVE"
        )

        // WHEN: Se procesa el registro remoto
        resolver.applyRemoteShift(remoteShift)

        // THEN: Se inserta directamente en la base de datos local
        val local = shiftDao.getShiftById("shift-001")
        assertNotNull(local)
        assertEquals("shift-001", local?.id)
        assertEquals(1, shiftDao.insertCount)
        assertEquals(0, shiftDao.updateCount)
    }

    @Test
    fun `cuando remoto es COMPLETED y local es ACTIVE se sobreescribe local con remoto`() = runBlocking {
        // GIVEN: Jornada local en curso (ACTIVE)
        val localShift = ShiftEntity(
            id = "shift-002",
            startTime = 1700000000000L,
            startOdometer = 50000,
            dailyQuotaTarget = 100000.0,
            status = "ACTIVE"
        )
        shiftDao.insertShift(localShift)

        // AND: Jornada remota cerrada (COMPLETED) por el mismo usuario en otro dispositivo
        val remoteShift = localShift.copy(
            status = "COMPLETED",
            endTime = 1700036000000L,
            endOdometer = 50150
        )

        // WHEN: Se resuelve el conflicto
        resolver.applyRemoteShift(remoteShift)

        // THEN: Prevalece el estado COMPLETED de la nube (LWW)
        val updatedLocal = shiftDao.getShiftById("shift-002")
        assertEquals("COMPLETED", updatedLocal?.status)
        assertEquals(50150, updatedLocal?.endOdometer)
        assertEquals(1, shiftDao.updateCount)
    }

    @Test
    fun `cuando remoto tiene endOdometer y local no lo tiene se actualiza odometro final local`() = runBlocking {
        // GIVEN: Jornada local sin odómetro final
        val localShift = ShiftEntity(
            id = "shift-003",
            startTime = 1700000000000L,
            startOdometer = 75000,
            endOdometer = null,
            dailyQuotaTarget = 120000.0,
            status = "ACTIVE"
        )
        shiftDao.insertShift(localShift)

        // AND: Remoto que ya registró el kilometraje de llegada
        val remoteShift = localShift.copy(
            endOdometer = 75180
        )

        // WHEN: Se resuelve el conflicto
        resolver.applyRemoteShift(remoteShift)

        // THEN: Se actualiza el odómetro final localmente
        val updatedLocal = shiftDao.getShiftById("shift-003")
        assertEquals(75180, updatedLocal?.endOdometer)
        assertEquals(1, shiftDao.updateCount)
    }

    @Test
    fun `cuando ambos estan ACTIVE sin cambios criticos se preserva el estado local del conductor`() = runBlocking {
        // GIVEN: Jornada local activa con notas tomadas en ruta
        val localShift = ShiftEntity(
            id = "shift-004",
            startTime = 1700000000000L,
            startOdometer = 80000,
            endOdometer = null,
            dailyQuotaTarget = 150000.0,
            status = "ACTIVE",
            notes = "Nota local no sincronizada aun"
        )
        shiftDao.insertShift(localShift)

        // AND: Remoto con estado ACTIVE y sin odómetro final
        val remoteShift = ShiftEntity(
            id = "shift-004",
            startTime = 1700000000000L,
            startOdometer = 80000,
            endOdometer = null,
            dailyQuotaTarget = 150000.0,
            status = "ACTIVE",
            notes = "Nota remota antigua"
        )

        // WHEN: Se resuelve el conflicto
        resolver.applyRemoteShift(remoteShift)

        // THEN: Se preserva la nota local (no se sobreescribe)
        val preservedLocal = shiftDao.getShiftById("shift-004")
        assertEquals("Nota local no sincronizada aun", preservedLocal?.notes)
        assertEquals(0, shiftDao.updateCount)
    }

    @Test
    fun `cuando local es COMPLETED y remoto es ACTIVE nunca se degrada la jornada local a activa`() = runBlocking {
        // GIVEN: Jornada local finalizada exitosamente
        val localShift = ShiftEntity(
            id = "shift-005",
            startTime = 1700000000000L,
            endTime = 1700036000000L,
            startOdometer = 60000,
            endOdometer = 60120,
            dailyQuotaTarget = 90000.0,
            status = "COMPLETED"
        )
        shiftDao.insertShift(localShift)

        // AND: Remoto desactualizado que aún dice ACTIVE
        val staleRemoteShift = localShift.copy(
            status = "ACTIVE",
            endTime = null,
            endOdometer = null
        )

        // WHEN: Se resuelve el conflicto
        resolver.applyRemoteShift(staleRemoteShift)

        // THEN: La jornada local sigue siendo COMPLETED
        val finalLocal = shiftDao.getShiftById("shift-005")
        assertEquals("COMPLETED", finalLocal?.status)
        assertEquals(60120, finalLocal?.endOdometer)
        assertEquals(0, shiftDao.updateCount)
    }
}
