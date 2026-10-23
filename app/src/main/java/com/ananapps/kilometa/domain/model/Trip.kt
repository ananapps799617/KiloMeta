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
    val durationMinutes: Int? = null,
    val platformId: String = platformType.name.lowercase()
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
        get() = calculateProfitabilityTier()

    fun calculateProfitabilityTier(
        minPerKm: Double = 1700.0,
        minPerHour: Double = 25000.0,
        breakEvenPerKm: Double = 1400.0
    ): TripProfitabilityTier? {
        val rKm = ratePerKm ?: return null
        val rH  = ratePerHour
        val lowHour = minPerHour * 0.6
        return when {
            rKm >= minPerKm && (rH == null || rH >= minPerHour) -> TripProfitabilityTier.HIGH
            rKm < breakEvenPerKm || (rH != null && rH < lowHour) -> TripProfitabilityTier.LOW
            else -> TripProfitabilityTier.MEDIUM
        }
    }


    val commissionRatePercentage: Double
        get() = if (grossAmount > 0.0) {
            (commissionAmount / grossAmount) * 100.0
        } else {
            0.0
        }
}
