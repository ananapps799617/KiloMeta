// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/TripRepository.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contrato de Dominio para la administración de carreras y servicios multi-plataforma.
// Proporciona reactividad por turno y registro de servicios individuales.
// ==============================================================================

package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.Trip
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------------
// BLOQUE 1: INTERFAZ DEL REPOSITORIO DE CARRERAS
// ------------------------------------------------------------------------------
/**
 * Define las operaciones sobre carreras prestadas durante los turnos de trabajo.
 */
interface TripRepository {

    /**
     * Emite el listado de carreras asociadas a un turno determinado en orden cronológico inverso.
     */
    fun getTripsByShift(shiftId: String): Flow<List<Trip>>

    /**
     * Registra una nueva carrera o servicio en la base de datos.
     */
    suspend fun addTrip(trip: Trip)

    /**
     * Elimina un registro de carrera individual por su ID.
     */
    suspend fun deleteTrip(tripId: String)
}
