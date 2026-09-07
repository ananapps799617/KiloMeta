// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/remote/firebase/FirebaseAuthDataSource.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Fuente de datos remota para autenticación con Firebase Authentication.
// Encapsula GoogleAuthProvider, credenciales de correo y autenticación anónima.
// Diseñado para resistir ausencia de Play Services sin provocar excepciones fatales.
// ==============================================================================

package com.ananapps.kilometa.data.remote.firebase

import com.ananapps.kilometa.domain.model.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: FUENTE DE DATOS REMOTA DE AUTENTICACIÓN
// ------------------------------------------------------------------------------
@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {

    private fun getAuthSafe(): FirebaseAuth? = auth

    private fun FirebaseUser.toUserSession(): UserSession = UserSession(
        uid = this.uid,
        email = this.email,
        displayName = this.displayName,
        isAnonymous = this.isAnonymous
    )

    // --------------------------------------------------------------------------
    // BLOQUE 2: OBSERVACIÓN REACTIVA DE SESIÓN
    // --------------------------------------------------------------------------
    fun observeAuthState(): Flow<UserSession?> {
        val auth = getAuthSafe() ?: return flowOf(null)

        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                trySend(firebaseAuth.currentUser?.toUserSession())
            }
            auth.addAuthStateListener(listener)
            awaitClose {
                auth.removeAuthStateListener(listener)
            }
        }
    }

    fun getCurrentUser(): UserSession? {
        return getAuthSafe()?.currentUser?.toUserSession()
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: MÉTODOS DE INGRESO Y REGISTRO ASÍNCRONOS
    // --------------------------------------------------------------------------
    suspend fun signInWithEmail(email: String, pass: String): Result<UserSession> = runCatching {
        val auth = getAuthSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseAuth no inicializado")

        val result = auth.signInWithEmailAndPassword(email, pass).await()
        val user = result.user
            ?: throw IllegalStateException("No se pudo obtener el perfil de usuario autenticado")
        user.toUserSession()
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<UserSession> = runCatching {
        val auth = getAuthSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseAuth no inicializado")

        val result = auth.createUserWithEmailAndPassword(email, pass).await()
        val user = result.user
            ?: throw IllegalStateException("No se pudo registrar la nueva cuenta")
        user.toUserSession()
    }

    suspend fun signInWithGoogle(idToken: String): Result<UserSession> = runCatching {
        val auth = getAuthSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseAuth no inicializado")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user
            ?: throw IllegalStateException("No se pudo autenticar con credencial de Google")
        user.toUserSession()
    }

    suspend fun signInAnonymously(): Result<UserSession> = runCatching {
        val auth = getAuthSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseAuth no inicializado")

        val result = auth.signInAnonymously().await()
        val user = result.user
            ?: throw IllegalStateException("No se pudo generar la sesión anónima")
        user.toUserSession()
    }

    suspend fun signOut(): Result<Unit> = runCatching {
        val auth = getAuthSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseAuth no inicializado")
        auth.signOut()
    }
}
