// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/HudConfigRepositoryImpl.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación local soberana con SharedPreferences y StateFlow reactivo (Orden #047).
// ==============================================================================

package com.ananapps.kilometa.data.repository

import android.content.Context
import com.ananapps.kilometa.domain.model.HudConfig
import com.ananapps.kilometa.domain.repository.HudConfigRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HudConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HudConfigRepository {

    private val prefs = context.getSharedPreferences("kilometa_hud_prefs", Context.MODE_PRIVATE)
    private val _configFlow = MutableStateFlow(readFromPrefs())

    override fun getHudConfig(): Flow<HudConfig> = _configFlow.asStateFlow()

    override suspend fun saveHudConfig(config: HudConfig) {
        prefs.edit()
            .putFloat("min_profit_km", config.minProfitPerKm.toFloat())
            .putFloat("min_profit_hour", config.minProfitPerHour.toFloat())
            .putFloat("break_even_km", config.breakEvenPerKm.toFloat())
            .putFloat("max_pickup_km", config.maxPickupDistanceKm.toFloat())
            .putFloat("custom_fuel_km", config.customFuelCostPerKm.toFloat())
            .putBoolean("use_auto_fuel", config.useAutoFuelCost)
            .putFloat("uber_commission", config.uberCommissionPercent.toFloat())
            .putFloat("didi_commission", config.didiCommissionPercent.toFloat())
            .putFloat("indrive_commission", config.indriveCommissionPercent.toFloat())
            .putInt("hud_dismiss_sec", config.hudDismissSeconds)
            .putBoolean("auto_start_shift", config.autoStartWithShift)
            .putBoolean("is_floating_bubble_enabled", config.isFloatingBubbleEnabled)
            .apply()

        _configFlow.value = config
    }

    private fun readFromPrefs(): HudConfig {
        return HudConfig(
            minProfitPerKm = prefs.getFloat("min_profit_km", 2500f).toDouble(),
            minProfitPerHour = prefs.getFloat("min_profit_hour", 26000f).toDouble(),
            breakEvenPerKm = prefs.getFloat("break_even_km", 1800f).toDouble(),
            maxPickupDistanceKm = prefs.getFloat("max_pickup_km", 3.0f).toDouble(),
            customFuelCostPerKm = prefs.getFloat("custom_fuel_km", 350f).toDouble(),
            useAutoFuelCost = prefs.getBoolean("use_auto_fuel", true),
            uberCommissionPercent = prefs.getFloat("uber_commission", 25f).toDouble(),
            didiCommissionPercent = prefs.getFloat("didi_commission", 20f).toDouble(),
            indriveCommissionPercent = prefs.getFloat("indrive_commission", 10f).toDouble(),
            hudDismissSeconds = prefs.getInt("hud_dismiss_sec", 15),
            autoStartWithShift = prefs.getBoolean("auto_start_shift", true),
            isFloatingBubbleEnabled = prefs.getBoolean("is_floating_bubble_enabled", false)
        )
    }
}
