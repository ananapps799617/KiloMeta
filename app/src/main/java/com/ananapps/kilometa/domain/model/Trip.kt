// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/Trip.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio que representa una carrera o servicio prestado en una plataforma.
// Incorpora PaymentDestination (Efectivo en mano vs Saldo en app).
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO INMUTABLE DE CARRERA / SERVICIO
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
    val endOdometer: Int? = null
) {
    // --------------------------------------------------------------------------
    // BLOQUE 2: PROPIEDADES DE VALIDACIÓN Y RENDIMIENTO
    // --------------------------------------------------------------------------
    val tripDistanceKm: Double?
        get() = if (startOdometer != null && endOdometer != null && endOdometer >= startOdometer) {
            (endOdometer - startOdometer).toDouble()
        } else {
            null
        }

    val commissionRatePercentage: Double
        get() = if (grossAmount > 0.0) {
            (commissionAmount / grossAmount) * 100.0
        } else {
            0.0
        }
}
