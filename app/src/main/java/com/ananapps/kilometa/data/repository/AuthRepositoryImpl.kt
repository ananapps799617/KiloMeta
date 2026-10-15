// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/AuthRepositoryImpl.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación del contrato de Dominio AuthRepository conectado a FirebaseAuthDataSource.
// Confinado a hilos en segundo plano mediante CoroutineDispatchers.io.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.core.dispatcher.CoroutineDispatchers
import com.ananapps.kilometa.data.remote.firebase.FirebaseAuthDataSource
import com.ananapps.kilometa.domain.model.UserSession
import com.ananapps.kilometa.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: IMPLEMENTACIÓN DEL REPOSITORIO DE AUTENTICACIÓN
// ------------------------------------------------------------------------------
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val dispatchers: CoroutineDispatchers
) : AuthRepository {

    override fun observeCurrentUser(): Flow<UserSession?> {
        return authDataSource.observeAuthState().flowOn(dispatchers.io)
    }

    override suspend fun getCurrentUser(): UserSession? {
        return withContext(dispatchers.io) {
            authDataSource.getCurrentUser()
        }
    }

    override suspend fun signInWithEmail(email: String, pass: String): Result<UserSession> {
        return withContext(dispatchers.io) {
            authDataSource.signInWithEmail(email, pass)
        }
    }

    override suspend fun signUpWithEmail(email: String, pass: String): Result<UserSession> {
        return withContext(dispatchers.io) {
            authDataSource.signUpWithEmail(email, pass)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<UserSession> {
        return withContext(dispatchers.io) {
            authDataSource.signInWithGoogle(idToken)
        }
    }

    override suspend fun signInAnonymously(): Result<UserSession> {
        return withContext(dispatchers.io) {
            authDataSource.signInAnonymously()
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return withContext(dispatchers.io) {
            authDataSource.signOut()
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return withContext(dispatchers.io) {
            authDataSource.sendPasswordResetEmail(email)
        }
    }
}
