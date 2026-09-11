// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/UpdateFuelingUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para la modificación de un registro de combustible (Gas o Gasolina).
// Valida que el costo, volumen y odómetro sean consistentes.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.repository.FuelingRepository
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE ACTUALIZACIÓN DE COMBUSTIBLE
// ------------------------------------------------------------------------------
/**
 * Modifica un tanqueo de combustible validando coherencia física.
 */
class UpdateFuelingUseCase @Inject constructor(
    private val fuelingRepository: FuelingRepository
) {

    /**
     * Ejecuta la actualización de un abastecimiento.
     *
     * @param fueling Instancia de combustible con datos modificados.
     */
    suspend operator fun invoke(fueling: Fueling) {
        require(fueling.id.isNotBlank()) { "El identificador del combustible no puede estar vacío" }
        require(fueling.amountPaid > 0.0) { "El costo del combustible debe ser mayor a cero" }
        require(fueling.volume > 0.0) { "El volumen despachado debe ser mayor a cero" }
        require(fueling.odometer >= 0) { "El odómetro no puede ser negativo" }

        fuelingRepository.updateFueling(fueling)
    }
}
