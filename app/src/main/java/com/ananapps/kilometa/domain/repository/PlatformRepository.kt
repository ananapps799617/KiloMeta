package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.Platform
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de Dominio para la persistencia y consulta reactiva de plataformas de transporte (Orden #054).
 */
interface PlatformRepository {

    /**
     * Emite reactivamente únicamente las plataformas marcadas como activas para la cabina.
     */
    fun observeActivePlatforms(): Flow<List<Platform>>

    /**
     * Emite reactivamente la totalidad del catálogo (activas e inactivas) para administración.
     */
    fun observeAllPlatforms(): Flow<List<Platform>>

    /**
     * Obtiene una plataforma específica por su identificador soberano.
     */
    suspend fun getPlatformById(id: String): Platform?

    /**
     * Guarda o actualiza los datos de una plataforma.
     */
    suspend fun savePlatform(platform: Platform): Result<Unit>

    /**
     * Conmuta el estado lógico de activación de una plataforma (MND-03).
     */
    suspend fun togglePlatformStatus(id: String, isActive: Boolean): Result<Unit>

    /**
     * Elimina físicamente una plataforma creada por el usuario si no posee historial.
     */
    suspend fun deleteCustomPlatform(id: String): Result<Unit>
}
