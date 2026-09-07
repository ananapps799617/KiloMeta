// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/DriverProfileRepository.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contrato de Dominio para la consulta reactiva y persistencia del perfil
// del conductor y especificaciones técnicas de su vehículo.
// Regla de Oro: Cero dependencias del framework Android en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.DriverProfile
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------------
// BLOQUE 1: CONTRATO DE REPOSITORIO DE PERFIL DE CONDUCTOR
// ------------------------------------------------------------------------------
/**
 * Interfaz de abstracción para la gestión de perfil de conductor y vehículo.
 */
interface DriverProfileRepository {

    /**
     * Emite reactivamente el perfil configurado del conductor y vehículo.
     */
    fun getProfile(): Flow<DriverProfile>

    /**
     * Persiste los cambios realizados al perfil del conductor localmente y
     * realiza sincronización oportunista con la nube si existe sesión activa.
     *
     * @param profile Perfil inmutable actualizado.
     */
    suspend fun saveProfile(profile: DriverProfile)
}
