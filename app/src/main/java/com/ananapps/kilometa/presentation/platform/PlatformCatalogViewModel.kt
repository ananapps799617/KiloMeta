package com.ananapps.kilometa.presentation.platform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.Platform
import com.ananapps.kilometa.domain.usecase.platform.GetAllPlatformsUseCase
import com.ananapps.kilometa.domain.usecase.platform.SavePlatformUseCase
import com.ananapps.kilometa.domain.usecase.platform.TogglePlatformStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la administración del catálogo dinámico multi-país de plataformas (Orden #054).
 * Regla inquebrantable: Modularidad estricta < 280 líneas de código.
 */
@HiltViewModel
class PlatformCatalogViewModel @Inject constructor(
    getAllPlatformsUseCase: GetAllPlatformsUseCase,
    private val savePlatformUseCase: SavePlatformUseCase,
    private val togglePlatformStatusUseCase: TogglePlatformStatusUseCase
) : ViewModel() {

    val platforms: StateFlow<List<Platform>> = getAllPlatformsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    private val _editingPlatform = MutableStateFlow<Platform?>(null)
    val editingPlatform: StateFlow<Platform?> = _editingPlatform.asStateFlow()

    private val _isSheetOpen = MutableStateFlow(false)
    val isSheetOpen: StateFlow<Boolean> = _isSheetOpen.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun openCreatePlatform() {
        _editingPlatform.value = null
        _errorMessage.value = null
        _isSheetOpen.value = true
    }

    fun openEditPlatform(platform: Platform) {
        _editingPlatform.value = platform
        _errorMessage.value = null
        _isSheetOpen.value = true
    }

    fun closeSheet() {
        _editingPlatform.value = null
        _errorMessage.value = null
        _isSheetOpen.value = false
    }

    fun togglePlatformActive(id: String, isActive: Boolean) {
        viewModelScope.launch {
            togglePlatformStatusUseCase(id, isActive).onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }

    fun savePlatform(platform: Platform) {
        viewModelScope.launch {
            savePlatformUseCase(platform)
                .onSuccess {
                    closeSheet()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
