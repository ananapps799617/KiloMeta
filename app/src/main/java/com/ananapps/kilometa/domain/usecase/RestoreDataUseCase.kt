// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/RestoreDataUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso de Dominio que orquesta la reconstrucción integral de la base de datos local
// desde la nube (Disaster Recovery) emitiendo el avance paso a paso para la UI.
// Regla arquitectural: Cero dependencias del framework Android en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.RestoreProgress
import com.ananapps.kilometa.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE RECUPERACIÓN ANTE DESASTRE
// ------------------------------------------------------------------------------
/**
 * Inicia la hidratación de emergencia post-robo o reemplazo de dispositivo móvil.
 */
class RestoreDataUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {

    /**
     * Emite el progreso en tiempo real de la restauración entidad por entidad.
     *
     * @return [Flow] que emite estados [RestoreProgress] (Running, Completed o Failed).
     */
    operator fun invoke(): Flow<RestoreProgress> {
        return syncRepository.fullDisasterRecovery()
    }
}
