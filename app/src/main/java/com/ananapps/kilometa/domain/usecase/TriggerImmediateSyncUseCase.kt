// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/TriggerImmediateSyncUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para solicitar un disparo inmediato de sincronización fuera de la
// ventana periódica (ej. al cerrar jornada o forzar actualización).
// Regla de Oro: Cero dependencias de Android framework en capa de dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.repository.SyncScheduler
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE DISPARO INMEDIATO DE SINCRONIZACIÓN
// ------------------------------------------------------------------------------
/**
 * Solicita al programador la ejecución inmediata de una tarea de sincronización.
 */
class TriggerImmediateSyncUseCase @Inject constructor(
    private val syncScheduler: SyncScheduler
) {

    /**
     * Encola una sincronización expedita en segundo plano.
     */
    operator fun invoke() {
        syncScheduler.triggerImmediateSync()
    }
}
