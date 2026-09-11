// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/FuelingRepository.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contrato de Dominio para el registro y consulta de abastecimientos bi-combustible.
// ==============================================================================

package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.Fueling
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------------
// BLOQUE 1: INTERFAZ DEL REPOSITORIO DE COMBUSTIBLE
// ------------------------------------------------------------------------------
/**
 * Define las operaciones de persistencia y consulta para recargas de combustible.
 */
interface FuelingRepository {

    /**
     * Emite el listado de recargas asociadas a un turno específico.
     */
    fun getFuelingsByShift(shiftId: String): Flow<List<Fueling>>

    /**
     * Registra un nuevo evento de abastecimiento de combustible (Gas o Gasolina).
     */
    suspend fun addFueling(fueling: Fueling)

    /**
     * Elimina un registro de tanqueo por su identificador.
     */
    suspend fun deleteFueling(fuelingId: String)

    /**
     * Actualiza la información de un tanqueo existente.
     */
    suspend fun updateFueling(fueling: Fueling)
}
