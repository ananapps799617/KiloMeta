// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/SchedulePeriodicSyncUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para programar o cancelar la sincronización en segundo plano según
// las preferencias del conductor.
// Regla de Oro: Cero dependencias de Android framework en capa de dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.SyncPreferences
import com.ananapps.kilometa.domain.repository.SyncScheduler
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE PROGRAMACIÓN DE SINCRONIZACIÓN PERIÓDICA
// ------------------------------------------------------------------------------
/**
 * Gestiona el ciclo de vida de la sincronización en segundo plano evaluando
 * si el conductor tiene habilitada la sincronización automática y la política de red.
 */
class SchedulePeriodicSyncUseCase @Inject constructor(
    private val syncScheduler: SyncScheduler
) {

    /**
     * Agenda o cancela la sincronización periódica según el estado de [preferences].
     *
     * @param preferences Configuración de periodicidad, conectividad y activación.
     */
    operator fun invoke(preferences: SyncPreferences) {
        if (preferences.isAutoSyncEnabled) {
            syncScheduler.schedulePeriodicSync(preferences)
        } else {
            syncScheduler.cancelPeriodicSync()
        }
    }
}
