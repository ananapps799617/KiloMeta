// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/DriverProfileViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel para la administración del perfil del conductor y especificaciones
// técnicas del vehículo (multi-propulsión, tenencia, cuotas y días laborales).
// ==============================================================================

package com.ananapps.kilometa.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.DriverProfile
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: VIEWMODEL DE PERFIL DE CONDUCTOR
// ------------------------------------------------------------------------------
@HiltViewModel
class DriverProfileViewModel @Inject constructor(
    private val profileRepository: DriverProfileRepository
) : ViewModel() {

    val profile: StateFlow<DriverProfile> = profileRepository.getProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = DriverProfile()
        )

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    /**
     * Persiste los cambios configurados en el perfil vehicular.
     */
    fun updateProfile(updatedProfile: DriverProfile) {
        viewModelScope.launch {
            profileRepository.saveProfile(updatedProfile)
            _isSaved.value = true
        }
    }

    fun resetSavedNotification() {
        _isSaved.value = false
    }
}
