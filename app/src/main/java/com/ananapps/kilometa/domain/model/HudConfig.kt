// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/HudConfig.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de configuración para el Copiloto, la Burbuja Flotante y el Semáforo de Ofertas.
// Permite al conductor calibrar sus propios estándares de rentabilidad en cabina.
// ==============================================================================

package com.ananapps.kilometa.domain.model

data class HudConfig(
    val minProfitPerKm: Double = 2500.0,
    val minProfitPerHour: Double = 26000.0,
    val breakEvenPerKm: Double = 1800.0,
    val maxPickupDistanceKm: Double = 3.0,
    val customFuelCostPerKm: Double = 350.0,
    val useAutoFuelCost: Boolean = true,
    val uberCommissionPercent: Double = 25.0,
    val didiCommissionPercent: Double = 20.0,
    val indriveCommissionPercent: Double = 10.0,
    val hudDismissSeconds: Int = 15,
    val autoStartWithShift: Boolean = true
)
