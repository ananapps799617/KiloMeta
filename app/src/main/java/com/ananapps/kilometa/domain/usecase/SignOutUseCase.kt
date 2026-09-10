// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/SignOutUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para orquestar el protocolo Clean Session Handshake (DataGuard Tier 3).
// Purga atómicamente la base de datos Room SQLite, preferencias de conductor y
// desautentica Firebase Auth para garantizar aislamiento multicuenta estricto.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.core.dispatcher.CoroutineDispatchers
import com.ananapps.kilometa.domain.repository.AuthRepository
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.repository.SyncRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE CIERRE DE SESIÓN SEGURO Y PURGA MULTI-TENANT
// ------------------------------------------------------------------------------
class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val driverProfileRepository: DriverProfileRepository,
    private val syncRepository: SyncRepository,
    private val dispatchers: CoroutineDispatchers
) {

    suspend operator fun invoke(): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            // 1. Purga atómica de la base de datos local Room SQLite
            syncRepository.clearAllLocalData().getOrThrow()

            // 2. Purga de preferencias de perfil de conductor y vehículo en memoria y disco
            driverProfileRepository.clearProfile()

            // 3. Cierre de sesión de Firebase Auth
            authRepository.signOut().getOrThrow()
        }
    }
}
