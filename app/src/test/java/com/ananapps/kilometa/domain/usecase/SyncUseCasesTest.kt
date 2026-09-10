// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/SyncUseCasesTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para el motor Cloud Sync y Disaster Recovery.
// Valida el flujo push/pull y el orden topológico estricto de hidratación (Shifts -> Hijos).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.ConnectionDiagnostic
import com.ananapps.kilometa.domain.model.RestoreProgress
import com.ananapps.kilometa.domain.model.SyncState
import com.ananapps.kilometa.domain.model.SyncStatus
import com.ananapps.kilometa.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// ------------------------------------------------------------------------------
// BLOQUE 1: FAKE DE REPOSITORIO DE SINCRONIZACIÓN
// ------------------------------------------------------------------------------
class FakeSyncRepository : SyncRepository {
    val executionLog = mutableListOf<String>()
    var pushShouldFail = false
    var pullShouldFail = false

    private val statusFlow = MutableStateFlow(SyncStatus(state = SyncState.IDLE))

    override fun observeSyncStatus(): Flow<SyncStatus> = statusFlow

    override suspend fun pushPendingChanges(): Result<Unit> {
        executionLog.add("PUSH")
        return if (pushShouldFail) {
            statusFlow.value = SyncStatus(SyncState.ERROR, errorMessage = "Error en push")
            Result.failure(Exception("Error de red en subida"))
        } else {
            statusFlow.value = SyncStatus(SyncState.SUCCESS, lastSyncTimestamp = System.currentTimeMillis())
            Result.success(Unit)
        }
    }

    override suspend fun pullRemoteChanges(): Result<Unit> {
        executionLog.add("PULL")
        return if (pullShouldFail) {
            statusFlow.value = SyncStatus(SyncState.ERROR, errorMessage = "Error en pull")
            Result.failure(Exception("Error de red en descarga"))
        } else {
            statusFlow.value = SyncStatus(SyncState.SUCCESS, lastSyncTimestamp = System.currentTimeMillis())
            Result.success(Unit)
        }
    }

    override suspend fun checkCloudConnection(): ConnectionDiagnostic {
        executionLog.add("CHECK_CONNECTION")
        return ConnectionDiagnostic(
            isConnected = true,
            latencyMs = 120L,
            message = "Conexión simulada exitosa"
        )
    }

    override fun fullDisasterRecovery(): Flow<RestoreProgress> = flow {
        executionLog.add("RECOVERY_START")
        emit(RestoreProgress.Running("Iniciando", 0, 0.05f))

        executionLog.add("RESTORE_SHIFTS")
        emit(RestoreProgress.Running("Jornadas", 2, 0.35f))

        executionLog.add("RESTORE_TRIPS")
        emit(RestoreProgress.Running("Carreras", 5, 0.60f))

        executionLog.add("RESTORE_FUELINGS")
        emit(RestoreProgress.Running("Tanqueos", 2, 0.80f))

        executionLog.add("RESTORE_EXPENSES")
        emit(RestoreProgress.Running("Gastos", 1, 0.95f))

        executionLog.add("RECOVERY_COMPLETED")
        emit(RestoreProgress.Completed(2, 5, 2, 1))
    }

    override suspend fun clearAllLocalData(): Result<Unit> {
        executionLog.add("CLEAR_ALL_LOCAL_DATA")
        return Result.success(Unit)
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: SUITE DE PRUEBAS DEL MOTOR DE SINCRONIZACIÓN
// ------------------------------------------------------------------------------
class SyncUseCasesTest {

    private lateinit var fakeSyncRepository: FakeSyncRepository
    private lateinit var syncDataUseCase: SyncDataUseCase
    private lateinit var restoreDataUseCase: RestoreDataUseCase

    @Before
    fun setUp() {
        fakeSyncRepository = FakeSyncRepository()
        syncDataUseCase = SyncDataUseCase(fakeSyncRepository)
        restoreDataUseCase = RestoreDataUseCase(fakeSyncRepository)
    }

    @Test
    fun testSyncDataOrchestrationOrder() = runBlocking {
        val result = syncDataUseCase()

        assertTrue("El ciclo completo de sync debe ser exitoso", result.isSuccess)
        assertEquals(listOf("PUSH", "PULL"), fakeSyncRepository.executionLog)
    }

    @Test
    fun testSyncDataStopsWhenPushFails() = runBlocking {
        fakeSyncRepository.pushShouldFail = true

        val result = syncDataUseCase()

        assertTrue("Si el push falla, el resultado debe ser failure", result.isFailure)
        assertEquals(listOf("PUSH"), fakeSyncRepository.executionLog)
    }

    @Test
    fun testFullDisasterRecoveryTopologicalOrder() = runBlocking {
        val progressEvents = restoreDataUseCase().toList()

        // Verificación de eventos emitidos
        assertEquals(6, progressEvents.size)
        assertTrue(progressEvents.first() is RestoreProgress.Running)
        assertTrue(progressEvents.last() is RestoreProgress.Completed)

        val completed = progressEvents.last() as RestoreProgress.Completed
        assertEquals(2, completed.totalShifts)
        assertEquals(5, completed.totalTrips)
        assertEquals(2, completed.totalFuelings)
        assertEquals(1, completed.totalExpenses)

        // Validación estricta del orden topológico referencial (Primero Padres, luego Hijos)
        val expectedLog = listOf(
            "RECOVERY_START",
            "RESTORE_SHIFTS",
            "RESTORE_TRIPS",
            "RESTORE_FUELINGS",
            "RESTORE_EXPENSES",
            "RECOVERY_COMPLETED"
        )
        assertEquals("Debe respetarse el orden relacional para evitar violaciones de Foreign Key", expectedLog, fakeSyncRepository.executionLog)
    }
}
