// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/UpdateTripUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para la edición y actualización segura de una carrera o servicio.
// Aplica invariantes contables (gross >= commission >= 0) y recalcula la ganancia neta.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.domain.repository.TripRepository
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE ACTUALIZACIÓN DE CARRERA
// ------------------------------------------------------------------------------
/**
 * Modifica una carrera existente garantizando consistencia contable.
 */
class UpdateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {

    /**
     * Ejecuta la actualización de la carrera.
     *
     * @param trip Instancia de [Trip] con los valores modificados.
     */
    suspend operator fun invoke(trip: Trip) {
        require(trip.id.isNotBlank()) { "El identificador de la carrera no puede estar vacío" }
        require(trip.grossAmount >= 0.0) { "El valor cobrado no puede ser negativo" }
        require(trip.commissionAmount >= 0.0) { "La comisión no puede ser negativa" }
        require(trip.grossAmount >= trip.commissionAmount) {
            "El valor cobrado (${trip.grossAmount}) no puede ser menor a la comisión (${trip.commissionAmount})"
        }

        val calculatedNet = trip.grossAmount - trip.commissionAmount
        val normalizedTrip = trip.copy(netAmount = calculatedNet)

        tripRepository.updateTrip(normalizedTrip)
    }
}
