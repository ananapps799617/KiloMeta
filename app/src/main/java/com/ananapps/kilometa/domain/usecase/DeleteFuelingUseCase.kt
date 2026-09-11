// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/DeleteFuelingUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para eliminar un abastecimiento de combustible (Gas o Gasolina).
// Valida identificador y coordina el borrado con lápidas anti-resurrección.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.repository.FuelingRepository
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE ELIMINACIÓN DE COMBUSTIBLE
// ------------------------------------------------------------------------------
/**
 * Elimina una recarga de combustible de la base de datos y de la nube.
 */
class DeleteFuelingUseCase @Inject constructor(
    private val fuelingRepository: FuelingRepository
) {

    /**
     * Ejecuta la eliminación de un tanqueo por su ID.
     *
     * @param fuelingId Identificador único del abastecimiento.
     */
    suspend operator fun invoke(fuelingId: String) {
        require(fuelingId.isNotBlank()) { "El identificador del combustible no puede estar en blanco" }
        fuelingRepository.deleteFueling(fuelingId)
    }
}
