// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/DeleteTripUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para eliminar un viaje registrado erróneamente durante la cabina activa.
// Valida que el identificador sea válido antes de invocar al repositorio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.repository.TripRepository
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE ELIMINACIÓN DE CARRERA
// ------------------------------------------------------------------------------
/**
 * Elimina una carrera de la base de datos local por su ID.
 */
class DeleteTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {

    /**
     * Ejecuta la eliminación segura de una carrera en el turno activo.
     *
     * @param tripId Identificador único de la carrera a eliminar.
     * @throws IllegalArgumentException si tripId es en blanco.
     */
    suspend operator fun invoke(tripId: String) {
        require(tripId.isNotBlank()) { "El identificador de la carrera no puede estar en blanco" }
        tripRepository.deleteTrip(tripId)
    }
}
