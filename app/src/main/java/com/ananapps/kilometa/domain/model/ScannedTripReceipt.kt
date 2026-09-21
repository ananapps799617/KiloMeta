// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/ScannedTripReceipt.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio inmutable que representa la información extraída automáticamente
// desde un pantallazo de recibo de viaje de Uber o DiDi mediante OCR local (Orden #044).
// Desacoplado de la UI y de Google ML Kit para mantener pureza de arquitectura limpia.
// ==============================================================================

package com.ananapps.kilometa.domain.model

/**
 * Representa los datos estructurados extraídos de un pantallazo de carrera.
 */
data class ScannedTripReceipt(
    val platform: PlatformType,
    val grossAmount: Double,
    val netAmount: Double,
    val commissionAmount: Double,
    val paymentMethod: PaymentMethod,
    val paymentDestination: PaymentDestination,
    val timestamp: Long,
    val distanceKm: Double? = null,
    val durationText: String? = null,
    val passengerName: String? = null,
    val confidenceSummary: String? = null,
    val rawText: String = ""
)
