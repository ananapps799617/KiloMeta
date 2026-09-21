// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/HomeAuthDelegate.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Delegado especializado para operaciones de autenticación de usuario en Cabina.
// Extrae la lógica de inicio y cierre de sesión de HomeViewModel para cumplir
// estrictamente con la regla de modularidad de menos de 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.presentation.home

import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.usecase.AuthenticateUserUseCase
import com.ananapps.kilometa.domain.usecase.SignOutUseCase
import com.ananapps.kilometa.domain.usecase.SyncDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeAuthDelegate @Inject constructor(
    private val authenticateUserUseCase: AuthenticateUserUseCase,
    private val driverProfileRepository: DriverProfileRepository,
    private val syncDataUseCase: SyncDataUseCase,
    private val signOutUseCase: SignOutUseCase
) {
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    fun setAuthError(message: String) { _authError.value = message }
    fun clearAuthError() { _authError.value = null }

    fun signInWithGoogle(scope: CoroutineScope, idToken: String) = scope.launch {
        _isAuthLoading.value = true
        _authError.value = null
        val result = authenticateUserUseCase.signInWithGoogle(idToken)
        result.getOrNull()?.let {
            driverProfileRepository.fetchAndHydrateRemoteProfile(it.uid)
            syncDataUseCase()
        } ?: run {
            _authError.value = result.exceptionOrNull()?.localizedMessage ?: "Error Google"
        }
        _isAuthLoading.value = false
    }

    fun signInWithEmail(scope: CoroutineScope, email: String, pass: String) = scope.launch {
        _isAuthLoading.value = true
        _authError.value = null
        val signIn = authenticateUserUseCase.signInWithEmail(email.trim(), pass)
        if (signIn.isSuccess) {
            signIn.getOrNull()?.let {
                driverProfileRepository.fetchAndHydrateRemoteProfile(it.uid)
                syncDataUseCase()
            }
        } else {
            val signUp = authenticateUserUseCase.signUpWithEmail(email.trim(), pass)
            if (signUp.isFailure) {
                _authError.value = signUp.exceptionOrNull()?.localizedMessage ?: "Error auth"
            } else {
                signUp.getOrNull()?.let {
                    driverProfileRepository.fetchAndHydrateRemoteProfile(it.uid)
                    syncDataUseCase()
                }
            }
        }
        _isAuthLoading.value = false
    }

    fun signInAnonymously(scope: CoroutineScope) = scope.launch {
        _isAuthLoading.value = true
        _authError.value = null
        val result = authenticateUserUseCase.signInAnonymously()
        if (result.isFailure) {
            _authError.value = result.exceptionOrNull()?.localizedMessage ?: "Error sesión"
        }
        _isAuthLoading.value = false
    }

    fun signOut(scope: CoroutineScope) = scope.launch {
        signOutUseCase()
    }
}
