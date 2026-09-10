// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/SignOutUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para SignOutUseCase (Clean Session Handshake).
// Valida la purga atómica de Room SQLite, SharedPreferences y Firebase Auth.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.core.dispatcher.CoroutineDispatchers
import com.ananapps.kilometa.domain.model.ConnectionDiagnostic
import com.ananapps.kilometa.domain.model.DriverProfile
import com.ananapps.kilometa.domain.model.RestoreProgress
import com.ananapps.kilometa.domain.model.SyncStatus
import com.ananapps.kilometa.domain.model.UserSession
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.domain.repository.AuthRepository
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.repository.SyncRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// ------------------------------------------------------------------------------
// BLOQUE 1: TEST DOUBLES ESPECÍFICOS PARA SESIÓN Y PURGA
// ------------------------------------------------------------------------------
private class FakeSignOutAuthRepo : AuthRepository {
    var isSignedOut = false
    override fun observeCurrentUser(): Flow<UserSession?> = flowOf(null)
    override suspend fun getCurrentUser(): UserSession? = null
    override suspend fun signInWithEmail(email: String, pass: String): Result<UserSession> = Result.failure(Exception())
    override suspend fun signUpWithEmail(email: String, pass: String): Result<UserSession> = Result.failure(Exception())
    override suspend fun signInWithGoogle(idToken: String): Result<UserSession> = Result.failure(Exception())
    override suspend fun signInAnonymously(): Result<UserSession> = Result.failure(Exception())
    override suspend fun signOut(): Result<Unit> {
        isSignedOut = true
        return Result.success(Unit)
    }
}

private class FakeLocalProfileRepo : DriverProfileRepository {
    var isCleared = false
    override fun getProfile(): Flow<DriverProfile> = flowOf(DriverProfile())
    override suspend fun saveProfile(profile: DriverProfile) {}
    override fun getVehicleProfile(): Flow<VehicleProfile> = flowOf(VehicleProfile())
    override suspend fun saveVehicleProfile(vehicle: VehicleProfile) {}
    override suspend fun saveFullProfile(driver: DriverProfile, vehicle: VehicleProfile) {}
    override suspend fun resetProfileForTesting() {}
    override suspend fun clearProfile() { isCleared = true }
    override suspend fun fetchAndHydrateRemoteProfile(userId: String): Result<Boolean> = Result.success(true)
}

private class FakeLocalSyncRepo : SyncRepository {
    var isDatabaseCleared = false
    override fun observeSyncStatus(): Flow<SyncStatus> = emptyFlow()
    override suspend fun pushPendingChanges(): Result<Unit> = Result.success(Unit)
    override suspend fun pullRemoteChanges(): Result<Unit> = Result.success(Unit)
    override fun fullDisasterRecovery(): Flow<RestoreProgress> = emptyFlow()
    override suspend fun checkCloudConnection(): ConnectionDiagnostic = ConnectionDiagnostic(
        isConnected = true,
        latencyMs = 10L,
        message = "OK"
    )
    override suspend fun clearAllLocalData(): Result<Unit> {
        isDatabaseCleared = true
        return Result.success(Unit)
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: SUITE DE PRUEBAS DE LIMPIEZA DE SESIÓN (DATA GUARD TIER 3)
// ------------------------------------------------------------------------------
@OptIn(ExperimentalCoroutinesApi::class)
class SignOutUseCaseTest {

    private lateinit var authRepo: FakeSignOutAuthRepo
    private lateinit var profileRepo: FakeLocalProfileRepo
    private lateinit var syncRepo: FakeLocalSyncRepo
    private lateinit var signOutUseCase: SignOutUseCase

    @Before
    fun setUp() {
        val testDispatcher = UnconfinedTestDispatcher()
        val dispatchers = CoroutineDispatchers(
            main = testDispatcher,
            io = testDispatcher,
            default = testDispatcher
        )
        authRepo = FakeSignOutAuthRepo()
        profileRepo = FakeLocalProfileRepo()
        syncRepo = FakeLocalSyncRepo()
        signOutUseCase = SignOutUseCase(authRepo, profileRepo, syncRepo, dispatchers)
    }

    @Test
    fun `invoke executes complete clean session handshake purging local db, prefs and auth`() = runTest {
        val result = signOutUseCase()

        assertTrue(result.isSuccess)
        assertTrue("La base de datos Room SQLite debió ser purgada", syncRepo.isDatabaseCleared)
        assertTrue("Las preferencias del perfil debieron ser purgadas", profileRepo.isCleared)
        assertTrue("Firebase Auth debió cerrar la sesión", authRepo.isSignedOut)
    }
}
