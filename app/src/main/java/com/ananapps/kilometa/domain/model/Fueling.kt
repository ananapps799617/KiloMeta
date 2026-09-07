// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/Fueling.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio para el registro de abastecimiento de combustible bi-modal.
// Maneja tanto Gas Natural Vehicular (GNV) como Gasolina con métricas de precio unitario.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO INMUTABLE DE TANQUEO / RECARGA
// ------------------------------------------------------------------------------
/**
 * Modela un evento de abastecimiento de combustible en estación de servicio.
 *
 * @property id Identificador único UUID v4 de la recarga.
 * @property shiftId Turno en el que ocurrió el abastecimiento.
 * @property fuelType Tipo de combustible cargado (GAS o GASOLINE).
 * @property amountPaid Monto total en COP pagado por la recarga.
 * @property volume Cantidad de combustible cargado (m³ para GNV, Galones para gasolina).
 * @property odometer Lectura del odómetro del vehículo en el momento del tanqueo.
 * @property timestamp Marca de tiempo Unix de la recarga en milisegundos.
 */
data class Fueling(
    val id: String,
    val shiftId: String,
    val fuelType: FuelType,
    val amountPaid: Double,
    val volume: Double,
    val odometer: Int,
    val timestamp: Long
) {
    // --------------------------------------------------------------------------
    // BLOQUE 2: PROPIEDADES CALCULADAS DE EFICIENCIA
    // --------------------------------------------------------------------------
    /**
     * Precio unitario por volumen (COP/m³ o COP/Gal).
     */
    val unitPrice: Double
        get() = if (volume > 0.0) {
            amountPaid / volume
        } else {
            0.0
        }
}
