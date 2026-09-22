// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/EvaluateTripOfferUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Motor matemático de rentabilidad en memoria (<= 2 ms) para solicitudes de viaje (Orden #047).
// Calcula ganancia neta, $/km, $/hora y semáforo instantáneo (Verde, Ámbar, Rojo).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.OfferSemaphoreTier
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.TripOffer
import com.ananapps.kilometa.domain.model.TripOfferEvaluation
import javax.inject.Inject

class EvaluateTripOfferUseCase @Inject constructor() {

    operator fun invoke(
        offer: TripOffer,
        customFuelCostPerKm: Double = 350.0
    ): TripOfferEvaluation {
        val totalDistance = offer.totalDistanceKm
        val commissionRate = when (offer.platform) {
            PlatformType.UBER -> 0.25
            PlatformType.DIDI -> 0.20
            PlatformType.INDRIVE -> 0.10
            PlatformType.PRIVATE -> 0.0
        }

        val estimatedPlatformFee = offer.offeredFare * commissionRate
        val estimatedFuelCost = totalDistance * customFuelCostPerKm
        val estimatedNetEarnings = (offer.offeredFare - estimatedPlatformFee - estimatedFuelCost).coerceAtLeast(0.0)

        val earningsPerKm = if (totalDistance > 0) estimatedNetEarnings / totalDistance else 0.0
        val earningsPerHour = if (offer.estimatedMinutes > 0) {
            (estimatedNetEarnings / offer.estimatedMinutes) * 60.0
        } else {
            0.0
        }

        val alerts = mutableListOf<String>()
        if (offer.pickupDistanceKm > 3.0) {
            alerts.add("Recogida lejana (${offer.pickupDistanceKm} km)")
        }
        if (earningsPerKm < 1800.0) {
            alerts.add("Tarifa baja ($/km)")
        }

        val semaphoreTier = when {
            earningsPerKm >= 2500.0 && earningsPerHour >= 26000.0 && offer.pickupDistanceKm <= 3.0 -> {
                OfferSemaphoreTier.GREEN_PROFITABLE
            }
            earningsPerKm >= 1800.0 && offer.pickupDistanceKm <= 3.5 -> {
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
