// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/AuthenticateUserUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso de Dominio que encapsula las operaciones de autenticación de conductores.
// Soporta vinculación opcional de cuenta mediante Google Sign-In, correo o sesión anónima.
// Regla arquitectural: Cero dependencias del framework Android en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.UserSession
import com.ananapps.kilometa.domain.repository.AuthRepository
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE GESTIÓN DE IDENTIDAD
// ------------------------------------------------------------------------------
/**
 * Agrupa los flujos de ingreso, registro y desvinculación para la nube.
 */
class AuthenticateUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend fun signInWithGoogle(idToken: String): Result<UserSession> {
        return authRepository.signInWithGoogle(idToken)
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<UserSession> {
        return authRepository.signInWithEmail(email, pass)
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<UserSession> {
        return authRepository.signUpWithEmail(email, pass)
    }

    suspend fun signInAnonymously(): Result<UserSession> {
        return authRepository.signInAnonymously()
    }

    suspend fun signOut(): Result<Unit> {
        return authRepository.signOut()
    }

    suspend fun getCurrentUser(): UserSession? {
        return authRepository.getCurrentUser()
    }
}
