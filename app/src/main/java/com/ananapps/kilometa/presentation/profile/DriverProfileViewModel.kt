// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/DriverProfileViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel para orquestar la edición del perfil, configuración vehicular y
// reseteo seguro de contraseña vía Firebase Auth (DataGuard Tier 3).
// ==============================================================================

package com.ananapps.kilometa.presentation.profile

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.DriverProfile
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverProfileViewModel @Inject constructor(
    private val profileRepository: DriverProfileRepository
) : ViewModel() {

    val driverProfile: StateFlow<DriverProfile> = profileRepository.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), DriverProfile())

    val vehicleProfile: StateFlow<VehicleProfile> = profileRepository.getVehicleProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), VehicleProfile())

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    fun saveFullProfile(driver: DriverProfile, vehicle: VehicleProfile) {
        viewModelScope.launch {
            val completedDriver = driver.copy(isProfileCompleted = true)
            profileRepository.saveFullProfile(completedDriver, vehicle)
            _isSaved.value = true
        }
    }

    fun resetProfileForTesting(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            profileRepository.resetProfileForTesting()
            onComplete()
        }
    }

    fun resetSavedNotification() {
        _isSaved.value = false
    }

    /**
     * Enlace seguro de reseteo de contraseña sin intermediación de contraseñas locales.
     * Cumplimiento DataGuard Tier 3: Cero credenciales en Room o SharedPreferences.
     */
    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        val trimmed = email.trim()
        if (trimmed.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            onResult(false, "Por favor introduce un correo electrónico válido.")
            return
        }

        try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(trimmed)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, null)
                    } else {
                        onResult(false, task.exception?.localizedMessage ?: "Error al enviar el enlace.")
                    }
                }
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Error al contactar con el servicio de autenticación.")
        }
    }
}
