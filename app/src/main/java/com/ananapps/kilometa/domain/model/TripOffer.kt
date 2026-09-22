// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/TripOffer.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de dominio inmutable que representa una solicitud u oferta de carrera
// detectada en tiempo real por el servicio pasivo de accesibilidad (Orden #047).
// ==============================================================================

package com.ananapps.kilometa.domain.model

data class TripOffer(
    val platform: PlatformType,
    val offeredFare: Double,
    val pickupDistanceKm: Double,
    val tripDistanceKm: Double,
    val estimatedMinutes: Int,
    val originAddress: String = "",
    val destinationAddress: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalDistanceKm: Double
        get() = (pickupDistanceKm + tripDistanceKm).coerceAtLeast(0.1)

    val isValid: Boolean
        get() = offeredFare > 0 && totalDistanceKm > 0
}
