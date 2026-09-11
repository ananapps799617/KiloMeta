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
    private val checkAppUpdateUseCase: CheckAppUpdateUseCase
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateCheckUiState>(UpdateCheckUiState.Idle)
    val updateState: StateFlow<UpdateCheckUiState> = _updateState.asStateFlow()

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
}
