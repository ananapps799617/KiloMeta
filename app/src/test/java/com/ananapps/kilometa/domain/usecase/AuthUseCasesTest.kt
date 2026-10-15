// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/AuthUseCasesTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para autenticación y observación de sesión de conductor.
// Utiliza un FakeAuthRepository in-memory tipado al 100%.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.UserSession
import com.ananapps.kilometa.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// ------------------------------------------------------------------------------
// BLOQUE 1: FAKE DE REPOSITORIO DE AUTENTICACIÓN
// ------------------------------------------------------------------------------
class FakeAuthRepository : AuthRepository {
    private val sessionFlow = MutableStateFlow<UserSession?>(null)

    override fun observeCurrentUser(): Flow<UserSession?> = sessionFlow
    override suspend fun getCurrentUser(): UserSession? = sessionFlow.value

    override suspend fun signInWithEmail(email: String, pass: String): Result<UserSession> {
        val user = UserSession(uid = "uid-email-123", email = email, displayName = "Conductor")
        sessionFlow.value = user
        return Result.success(user)
    }

    override suspend fun signUpWithEmail(email: String, pass: String): Result<UserSession> {
        val user = UserSession(uid = "uid-signup-456", email = email, displayName = "Nuevo Conductor")
        sessionFlow.value = user
        return Result.success(user)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<UserSession> {
        val user = UserSession(uid = "uid-google-789", email = "driver@gmail.com", displayName = "Google Driver")
        sessionFlow.value = user
        return Result.success(user)
    }

    override suspend fun signInAnonymously(): Result<UserSession> {
        val user = UserSession(uid = "uid-anon-000", email = null, isAnonymous = true)
        sessionFlow.value = user
        return Result.success(user)
    }

    override suspend fun signOut(): Result<Unit> {
        sessionFlow.value = null
        return Result.success(Unit)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return Result.success(Unit)
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: SUITE DE PRUEBAS DE AUTENTICACIÓN
// ------------------------------------------------------------------------------
class AuthUseCasesTest {

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var observeAuthStateUseCase: ObserveAuthStateUseCase
    private lateinit var authenticateUserUseCase: AuthenticateUserUseCase

    @Before
    fun setUp() {
        fakeAuthRepository = FakeAuthRepository()
        observeAuthStateUseCase = ObserveAuthStateUseCase(fakeAuthRepository)
        authenticateUserUseCase = AuthenticateUserUseCase(fakeAuthRepository)
    }

    @Test
    fun testInitialSessionIsNull() = runBlocking {
        val session = observeAuthStateUseCase().first()
        assertNull("Al arrancar, no debe haber sesión activa", session)
    }

    @Test
    fun testGoogleSignInSuccess() = runBlocking {
        val result = authenticateUserUseCase.signInWithGoogle("valid-token")
        assertTrue(result.isSuccess)

        val session = result.getOrNull()
        assertNotNull(session)
        assertEquals("driver@gmail.com", session?.email)
        assertEquals("Google Driver", session?.displayName)

        val observed = observeAuthStateUseCase().first()
        assertEquals(session, observed)
    }

    @Test
    fun testEmailSignInSuccess() = runBlocking {
        val result = authenticateUserUseCase.signInWithEmail("taxista@bogota.co", "secret123")
        assertTrue(result.isSuccess)
        assertEquals("taxista@bogota.co", result.getOrNull()?.email)
    }

    @Test
    fun testAnonymousSignIn() = runBlocking {
        val result = authenticateUserUseCase.signInAnonymously()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isAnonymous == true)
        assertNull(result.getOrNull()?.email)
    }

    @Test
    fun testSignOut() = runBlocking {
        authenticateUserUseCase.signInWithGoogle("token")
        assertNotNull(observeAuthStateUseCase().first())

        val signoutResult = authenticateUserUseCase.signOut()
        assertTrue(signoutResult.isSuccess)
        assertNull("Tras cerrar sesión el flujo debe emitir null", observeAuthStateUseCase().first())
    }
}
