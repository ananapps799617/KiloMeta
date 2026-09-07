// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/Trip.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio que representa una carrera o servicio prestado en una plataforma.
// Modelo inmutable y rigurosamente tipado para cálculos contables limpios.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO INMUTABLE DE CARRERA / SERVICIO
// ------------------------------------------------------------------------------
/**
 * Modela un servicio completado en una plataforma de transporte o servicio particular.
 *
 * @property id Identificador único UUID v4 de la carrera.
 * @property shiftId Identificador del turno al que pertenece la carrera.
 * @property platformType Plataforma en la que se realizó la carrera (Uber, DiDi, InDrive, Particular).
 * @property grossAmount Monto bruto pagado por el usuario final (COP).
 * @property commissionAmount Comisión o descuento cobrado por la plataforma (COP).
 * @property netAmount Monto neto liquidado al conductor (grossAmount - commissionAmount).
 * @property paymentMethod Medio por el cual se recaudó el valor (Efectivo, Nequi, Billetera).
 * @property timestamp Momento exacto de registro de la carrera en Unix millis.
 */
data class Trip(
    val id: String,
    val shiftId: String,
    val platformType: PlatformType,
    val grossAmount: Double,
    val commissionAmount: Double,
    val netAmount: Double,
    val paymentMethod: PaymentMethod,
    val timestamp: Long
) {
    // --------------------------------------------------------------------------
    // BLOQUE 2: PROPIEDADES DE VALIDACIÓN Y RENDIMIENTO
    // --------------------------------------------------------------------------
    /**
     * Porcentaje de comisión retenido por la plataforma para este servicio.
     */
    val commissionRatePercentage: Double
        get() = if (grossAmount > 0.0) {
            (commissionAmount / grossAmount) * 100.0
        } else {
            0.0
        }
}
