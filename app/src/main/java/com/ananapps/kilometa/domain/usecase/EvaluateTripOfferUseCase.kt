// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/EvaluateTripOfferUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Motor matemático de rentabilidad en memoria (<= 2 ms) para solicitudes de viaje (Orden #047).
// Calcula ganancia neta, $/km, $/hora y semáforo instantáneo (Verde, Ámbar, Rojo).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.*
import javax.inject.Inject

class EvaluateTripOfferUseCase @Inject constructor() {

    operator fun invoke(
        offer: TripOffer,
        config: HudConfig = HudConfig(),
        customFuelCostPerKm: Double = 350.0
    ): TripOfferEvaluation {
        val cfg = config
        val totalDistance = offer.totalDistanceKm
        val commissionRate = when (offer.platform) {
            PlatformType.UBER -> cfg.uberCommissionPercent / 100.0
            PlatformType.DIDI -> cfg.didiCommissionPercent / 100.0
            PlatformType.INDRIVE -> cfg.indriveCommissionPercent / 100.0
            PlatformType.PRIVATE -> 0.0
        }

        val fuelCostKm = if (cfg.useAutoFuelCost) customFuelCostPerKm else cfg.customFuelCostPerKm
        val estimatedPlatformFee = 0.0 // Tarifa en pantalla ya incluye descuento de plataforma
        val estimatedFuelCost = totalDistance * fuelCostKm
        val estimatedNetEarnings = (offer.offeredFare - estimatedFuelCost).coerceAtLeast(0.0)

        val earningsPerKm = if (totalDistance > 0) estimatedNetEarnings / totalDistance else 0.0
        val earningsPerHour = if (offer.estimatedMinutes > 0) {
            (estimatedNetEarnings / offer.estimatedMinutes) * 60.0
        } else {
            0.0
        }

        val alerts = mutableListOf<String>()
        if (offer.pickupDistanceKm > cfg.maxPickupDistanceKm) {
            alerts.add("Recogida lejana (%.1f km > %.1f km)".format(offer.pickupDistanceKm, cfg.maxPickupDistanceKm))
        }
        if (earningsPerKm < cfg.breakEvenPerKm) {
            alerts.add("Tarifa baja ($/km)")
        }

        val semaphoreTier = when {
            earningsPerKm >= cfg.minProfitPerKm &&
            earningsPerHour >= cfg.minProfitPerHour &&
            offer.pickupDistanceKm <= cfg.maxPickupDistanceKm -> {
                OfferSemaphoreTier.GREEN_PROFITABLE
            }
            earningsPerKm >= cfg.breakEvenPerKm &&
            offer.pickupDistanceKm <= (cfg.maxPickupDistanceKm + 0.5) -> {
                OfferSemaphoreTier.YELLOW_BREAK_EVEN
            }
            else -> {
                OfferSemaphoreTier.RED_UNFAVORABLE
            }
        }

        return TripOfferEvaluation(
            offer = offer,
            totalDistanceKm = totalDistance,
            estimatedFuelCost = estimatedFuelCost,
            estimatedPlatformFee = estimatedPlatformFee,
            estimatedNetEarnings = estimatedNetEarnings,
            earningsPerKm = earningsPerKm,
            earningsPerHour = earningsPerHour,
            semaphoreTier = semaphoreTier,
            alerts = alerts
        )
    }
}
