// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/Trip.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio que representa una carrera o servicio prestado en una plataforma.
// Incorpora PaymentDestination, distancia, duración y métricas automáticas de rentabilidad ($/km, $/h y semáforo).
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: SEMÁFORO DE RENTABILIDAD POR CARRERA
// ------------------------------------------------------------------------------
enum class TripProfitabilityTier(val displayName: String, val emoji: String) {
    HIGH("Rentable", "🟢"),
    MEDIUM("Equilibrio", "🟡"),
    LOW("No rentable", "🔴")
}

// ------------------------------------------------------------------------------
// BLOQUE 2: MODELO INMUTABLE DE CARRERA / SERVICIO
// ------------------------------------------------------------------------------
data class Trip(
    val id: String,
    val shiftId: String,
    val platformType: PlatformType,
    val grossAmount: Double,
    val commissionAmount: Double,
    val netAmount: Double,
    val paymentMethod: PaymentMethod,
    val paymentDestination: PaymentDestination = PaymentDestination.CASH_IN_HAND,
    val timestamp: Long,
    val startOdometer: Int? = null,
    val endOdometer: Int? = null,
    val platformPromoDiscount: Double = 0.0,
    val distanceKm: Double? = null,
    val durationMinutes: Int? = null
) {
    // --------------------------------------------------------------------------
    // BLOQUE 3: TELEMETRÍA Y MÉTRICAS DE RENTABILIDAD
    // --------------------------------------------------------------------------
    val tripDistanceKm: Double?
        get() = distanceKm ?: if (startOdometer != null && endOdometer != null && endOdometer >= startOdometer) {
            (endOdometer - startOdometer).toDouble()
        } else {
            null
        }

    val ratePerKm: Double?
        get() = tripDistanceKm?.takeIf { it > 0.0 }?.let { netAmount / it }

    val ratePerHour: Double?
        get() = durationMinutes?.takeIf { it > 0 }?.let { (netAmount / it) * 60.0 }

    val profitabilityTier: TripProfitabilityTier?
        get() {
            val rKm = ratePerKm ?: return null
            val rH  = ratePerHour
            return when {
                rKm >= 1700.0 && (rH == null || rH >= 25000.0) -> TripProfitabilityTier.HIGH
                rKm < 1400.0  || (rH != null && rH < 15000.0)  -> TripProfitabilityTier.LOW
                else                                            -> TripProfitabilityTier.MEDIUM
            }
        }


    val commissionRatePercentage: Double
        get() = if (grossAmount > 0.0) {
            (commissionAmount / grossAmount) * 100.0
        } else {
            0.0
        }
}
