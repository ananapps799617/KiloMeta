// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/DeleteShiftUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para la eliminación definitiva de un turno de trabajo.
// Invoca ShiftRepository para ejecutar el protocolo en cascada de lápidas y purga Cloud.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.repository.ShiftRepository
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE ELIMINACIÓN DE TURNO
// ------------------------------------------------------------------------------
/**
 * Elimina un turno de conducción y todas sus dependencias con blindaje de lápidas.
 */
class DeleteShiftUseCase @Inject constructor(
    private val shiftRepository: ShiftRepository
) {

    /**
     * Ejecuta la eliminación del turno por su ID.
     *
     * @param shiftId Identificador del turno a eliminar.
     */
    suspend operator fun invoke(shiftId: String) {
        require(shiftId.isNotBlank()) { "El identificador del turno no puede estar en blanco" }
        shiftRepository.deleteShift(shiftId)
    }
}
