// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/more/MoreViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel para la pestaña "Más Opciones". Maneja la verificación reactiva de
// actualizaciones OTA y el ciclo de vida del diálogo de instalación In-App.
// ==============================================================================

package com.ananapps.kilometa.presentation.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.BuildConfig
import com.ananapps.kilometa.domain.model.AppUpdateInfo
import com.ananapps.kilometa.domain.usecase.CheckAppUpdateUseCase
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UpdateCheckUiState {
    data object Idle : UpdateCheckUiState
    data object Checking : UpdateCheckUiState
    data class Available(val info: AppUpdateInfo) : UpdateCheckUiState
    data object UpToDate : UpdateCheckUiState
    data class Error(val message: String) : UpdateCheckUiState
}

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val checkAppUpdateUseCase: CheckAppUpdateUseCase,
    private val syncRepository: SyncRepository,
    private val profileRepository: DriverProfileRepository
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateCheckUiState>(UpdateCheckUiState.Idle)
    val updateState: StateFlow<UpdateCheckUiState> = _updateState.asStateFlow()

    private val _isPurging = MutableStateFlow(false)
    val isPurging: StateFlow<Boolean> = _isPurging.asStateFlow()

    fun checkForUpdates() {
        if (_updateState.value is UpdateCheckUiState.Checking) return
        viewModelScope.launch {
            _updateState.value = UpdateCheckUiState.Checking
            val currentCode = BuildConfig.VERSION_CODE.toLong()
            checkAppUpdateUseCase(currentCode).fold(
                onSuccess = { info ->
                    if (info.isUpdateAvailable) {
                        _updateState.value = UpdateCheckUiState.Available(info)
                    } else {
                        _updateState.value = UpdateCheckUiState.UpToDate
                    }
                },
                onFailure = { err ->
                    _updateState.value = UpdateCheckUiState.Error(
                        err.localizedMessage ?: "No se pudo conectar al servidor de actualización"
                    )
                }
            )
        }
    }

    fun dismissUpdate() {
        _updateState.value = UpdateCheckUiState.Idle
    }

    fun purgeAccountData(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_isPurging.value) return
        viewModelScope.launch {
            _isPurging.value = true
            syncRepository.purgeAllUserData()
                .onSuccess {
                    profileRepository.clearProfile()
                    _isPurging.value = false
                    onSuccess()
                }
                .onFailure { err ->
                    _isPurging.value = false
                    onError(err.localizedMessage ?: "Error al purgar los datos de la cuenta")
                }
        }
    }
}
