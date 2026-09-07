// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/ObserveAuthStateUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso de Dominio que emite reactivamente los cambios en la sesión de usuario.
// Regla arquitectural: Cero dependencias del framework Android en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.UserSession
import com.ananapps.kilometa.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE OBSERVACIÓN DE SESIÓN
// ------------------------------------------------------------------------------
/**
 * Emite las transiciones de estado de autenticación para actualizar los controles de cabina.
 */
class ObserveAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    /**
     * Emite la sesión de usuario activa o null si no se ha iniciado sesión.
     */
    operator fun invoke(): Flow<UserSession?> {
        return authRepository.observeCurrentUser()
    }
}
