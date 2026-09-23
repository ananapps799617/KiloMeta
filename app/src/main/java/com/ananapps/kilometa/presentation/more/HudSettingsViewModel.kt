// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/more/HudSettingsViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel para la calibración soberana de parámetros del Copiloto y Burbuja (Orden #047).
// ==============================================================================

package com.ananapps.kilometa.presentation.more

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.HudConfig
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.TripOffer
import com.ananapps.kilometa.domain.repository.HudConfigRepository
import com.ananapps.kilometa.domain.usecase.EvaluateTripOfferUseCase
import com.ananapps.kilometa.presentation.overlay.RideOfferOverlayManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HudSettingsViewModel @Inject constructor(
    private val repository: HudConfigRepository,
    private val overlayManager: RideOfferOverlayManager,
    private val evaluateTripOfferUseCase: EvaluateTripOfferUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _config = MutableStateFlow(HudConfig())
    val config: StateFlow<HudConfig> = _config.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Unit>()
    val saveSuccess: SharedFlow<Unit> = _saveSuccess.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.getHudConfig().collect { saved ->
                _config.value = saved
            }
        }
    }

    fun hasOverlayPermission(): Boolean = Settings.canDrawOverlays(context)

    fun updateConfig(update: (HudConfig) -> HudConfig) {
        _config.value = update(_config.value)
    }

    fun saveConfig() {
        viewModelScope.launch {
            repository.saveHudConfig(_config.value)
            _saveSuccess.emit(Unit)
        }
    }

    fun simulateTestOffer() {
        val mockOffer = TripOffer(
            platform = PlatformType.DIDI,
            offeredFare = 12494.0,
            pickupDistanceKm = 1.5,
            tripDistanceKm = 4.2,
            estimatedMinutes = 25
        )
        val evaluation = evaluateTripOfferUseCase(mockOffer, _config.value)
        overlayManager.showOfferEvaluation(evaluation)
    }
}
