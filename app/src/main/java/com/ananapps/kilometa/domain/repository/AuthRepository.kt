// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/AuthRepository.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contrato de Dominio para la autenticación y gestión de identidad del conductor.
// Expone flujos reactivos de sesión y operaciones asíncronas desacopladas de Firebase.
// Regla arquitectural: Cero dependencias del framework Android en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------------
// BLOQUE 1: INTERFAZ DEL REPOSITORIO DE AUTENTICACIÓN
// ------------------------------------------------------------------------------
/**
 * Define las operaciones de acceso, registro y observación de sesión de usuario.
 */
interface AuthRepository {

    /**
     * Emite reactivamente el estado de la sesión activa del usuario (null si no hay sesión).
     */
    fun observeCurrentUser(): Flow<UserSession?>

    /**
     * Obtiene de forma sincrónica la sesión actual si existe.
     */
    suspend fun getCurrentUser(): UserSession?

    /**
     * Autentica al conductor mediante correo electrónico y contraseña.
     */
    suspend fun signInWithEmail(email: String, pass: String): Result<UserSession>

    /**
     * Registra un nuevo conductor mediante correo electrónico y contraseña.
     */
    suspend fun signUpWithEmail(email: String, pass: String): Result<UserSession>

    /**
     * Autentica al conductor mediante token de identidad de Google Sign-In.
     */
    suspend fun signInWithGoogle(idToken: String): Result<UserSession>

    /**
     * Inicia una sesión anónima provisional para respaldo local sin registro previo.
     */
    suspend fun signInAnonymously(): Result<UserSession>

    /**
     * Cierra la sesión activa actual en el dispositivo.
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Envía un correo con el enlace oficial de restablecimiento de contraseña.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
}
