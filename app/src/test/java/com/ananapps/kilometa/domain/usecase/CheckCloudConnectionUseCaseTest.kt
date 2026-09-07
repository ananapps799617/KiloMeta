/**
 * ARCHIVO: CheckCloudConnectionUseCaseTest.kt
 * PAQUETE: com.ananapps.kilometa.domain.usecase
 * PROYECTO: KiloMeta - Ecosistema AnAnApps v2.0
 *
 * PROPÓSITO:
 * Pruebas unitarias pedagógicas para [CheckCloudConnectionUseCase].
 * Valida la correcta delegación hacia el repositorio de sincronización
 * bajo diferentes escenarios de conectividad y latencia con la nube.
 *
 * PRINCIPIOS Y REGLAS DE ORO:
 * - 100% Kotlin Puro en Domain (Cero dependencias de Android framework).
 * - Tipado estricto e inmutabilidad garantizada.
 * - Cobertura de caminos exitosos, fallas de red y banderas de configuración placeholder.
 */

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.ConnectionDiagnostic
import com.ananapps.kilometa.domain.model.RestoreProgress
import com.ananapps.kilometa.domain.model.SyncState
import com.ananapps.kilometa.domain.model.SyncStatus
import com.ananapps.kilometa.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Suite de pruebas para [CheckCloudConnectionUseCase].
 */
class CheckCloudConnectionUseCaseTest {

    // =========================================================================
    // BLOQUE 1: DOBLE DE PRUEBA (TEST DOUBLE - FAKE REPOSITORY)
    // =========================================================================

    private class FakeDiagnosticSyncRepository : SyncRepository {
        var diagnosticToReturn: ConnectionDiagnostic = ConnectionDiagnostic(
            isConnected = true,
            latencyMs = 120L,
            timestampMillis = 1710000000000L,
            message = "Enlace en vivo óptimo",
            isPlaceholderConfig = false
        )

        var checkConnectionCallCount = 0

        override fun observeSyncStatus(): Flow<SyncStatus> = MutableStateFlow(SyncStatus(SyncState.IDLE))
        override suspend fun pushPendingChanges(): Result<Unit> = Result.success(Unit)
        override suspend fun pullRemoteChanges(): Result<Unit> = Result.success(Unit)
        override fun fullDisasterRecovery(): Flow<RestoreProgress> = MutableStateFlow(RestoreProgress.Completed(0, 0, 0, 0))

        override suspend fun checkCloudConnection(): ConnectionDiagnostic {
            checkConnectionCallCount++
            return diagnosticToReturn
        }
    }

    private lateinit var fakeRepository: FakeDiagnosticSyncRepository
    private lateinit var useCase: CheckCloudConnectionUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeDiagnosticSyncRepository()
        useCase = CheckCloudConnectionUseCase(fakeRepository)
    }

    // =========================================================================
    // BLOQUE 2: ESCENARIOS DE ÉXITO Y CONECTIVIDAD SALUDABLE
    // =========================================================================

    @Test
    fun invoke_whenCloudIsReachable_returnsSuccessfulDiagnostic() = runTest {
        // GIVEN: Un repositorio que reporta conectividad exitosa en 85ms
        val expectedDiagnostic = ConnectionDiagnostic(
            isConnected = true,
            latencyMs = 85L,
            timestampMillis = 1710000000500L,
            message = "Enlace óptimo con Firestore (85 ms)",
            isPlaceholderConfig = false
        )
        fakeRepository.diagnosticToReturn = expectedDiagnostic

        // WHEN: Se ejecuta el caso de uso
        val result = useCase()

        // THEN: Se valida la delegación y los valores del diagnóstico
        assertEquals(1, fakeRepository.checkConnectionCallCount)
        assertTrue(result.isConnected)
        assertEquals(85L, result.latencyMs)
        assertEquals(1710000000500L, result.timestampMillis)
        assertTrue(result.message.contains("Enlace óptimo"))
        assertFalse(result.isPlaceholderConfig)
    }

    // =========================================================================
    // BLOQUE 3: ESCENARIOS DE ERROR O DESCONEXIÓN
    // =========================================================================

    @Test
    fun invoke_whenCloudIsUnreachable_returnsFailureDiagnosticWithError() = runTest {
        // GIVEN: Un repositorio que reporta error de timeout de red
        val errorDiagnostic = ConnectionDiagnostic(
            isConnected = false,
            latencyMs = null,
            timestampMillis = 1710000001000L,
            message = "Fallo de conexión o timeout con Firebase Firestore: host unreachable",
            isPlaceholderConfig = false
        )
        fakeRepository.diagnosticToReturn = errorDiagnostic

        // WHEN: Se ejecuta el caso de uso
        val result = useCase()

        // THEN: Se verifica que no hay alcance y se conserva el mensaje descriptivo
        assertFalse(result.isConnected)
        assertNull(result.latencyMs)
        assertTrue(result.message.contains("timeout"))
        assertFalse(result.isPlaceholderConfig)
    }

    // =========================================================================
    // BLOQUE 4: ESCENARIO DE MODO DEMO O PLACEHOLDER
    // =========================================================================

    @Test
    fun invoke_whenConfigIsPlaceholder_marksIsPlaceholderConfigAsTrue() = runTest {
        // GIVEN: Diagnóstico ejecutado sin credenciales reales configuradas
        val placeholderDiagnostic = ConnectionDiagnostic(
            isConnected = true,
            latencyMs = 45L,
            timestampMillis = 1710000002000L,
            message = "Simulación activa (Modo Placeholder)",
            isPlaceholderConfig = true
        )
        fakeRepository.diagnosticToReturn = placeholderDiagnostic

        // WHEN: Se invoca el caso de uso
        val result = useCase()

        // THEN: Se identifica el flag de placeholder para transparencia con el usuario
        assertTrue(result.isConnected)
        assertTrue(result.isPlaceholderConfig)
    }
}
