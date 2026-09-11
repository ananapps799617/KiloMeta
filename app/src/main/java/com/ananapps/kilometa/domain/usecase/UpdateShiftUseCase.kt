// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/UpdateShiftUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para la edición de datos generales de un turno (odómetros, notas, tiempos).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.Shift
import com.ananapps.kilometa.domain.repository.ShiftRepository
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE ACTUALIZACIÓN DE TURNO
// ------------------------------------------------------------------------------
/**
 * Modifica un turno existente garantizando consistencia temporal y de odometría.
 */
class UpdateShiftUseCase @Inject constructor(
    private val shiftRepository: ShiftRepository
) {

    /**
     * Ejecuta la actualización del turno.
     */
    suspend operator fun invoke(shift: Shift) {
        require(shift.id.isNotBlank()) { "El identificador del turno no puede estar en blanco" }
        require(shift.startOdometer >= 0) { "El odómetro inicial no puede ser negativo" }
        if (shift.endOdometer != null) {
            require(shift.endOdometer >= shift.startOdometer) {
                "El odómetro final (${shift.endOdometer}) no puede ser menor al inicial (${shift.startOdometer})"
            }
        }
        if (shift.endTime != null) {
            require(shift.endTime >= shift.startTime) {
                "La hora de finalización no puede ser anterior a la de inicio"
            }
        }

        shiftRepository.updateShift(shift)
    }
}
