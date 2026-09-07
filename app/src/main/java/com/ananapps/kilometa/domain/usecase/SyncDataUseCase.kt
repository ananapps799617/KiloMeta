// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/SyncDataUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso de Dominio que orquesta el ciclo de sincronización push/pull
// de forma segura y tolerante a fallas de conectividad.
// Regla arquitectural: Cero dependencias del framework Android en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.repository.SyncRepository
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE SINCRONIZACIÓN ASÍNCRONA
// ------------------------------------------------------------------------------
/**
 * Ejecuta el ciclo completo de respaldo bidireccional (subida de cambios locales y descarga de remotos).
 */
class SyncDataUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {

    /**
     * Orquesta el push de mutaciones pendientes y el pull de actualizaciones remotas.
     *
     * @return [Result.success] si ambas fases concluyen sin anomalías, o [Result.failure] con la causa.
     */
    suspend operator fun invoke(): Result<Unit> {
        val pushResult = syncRepository.pushPendingChanges()
        if (pushResult.isFailure) {
            return pushResult
        }

        return syncRepository.pullRemoteChanges()
    }
}
