// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/ScheduleWeeklyCutoffNotificationsUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso que programa las alarmas y tareas en segundo plano para alertar al conductor
// sobre el corte de la cuota semanal (Martes a las 18:00 preventivo y 23:59:59 cierre).
// Regla de Oro: Cero dependencias de Android framework en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.repository.VehicularNotificationScheduler
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE PROGRAMACIÓN DE ALARMAS DE CORTE SEMANAL
// ------------------------------------------------------------------------------
/**
 * Orquesta la programación de alertas vehiculares para el ciclo semanal de cuota.
 */
class ScheduleWeeklyCutoffNotificationsUseCase @Inject constructor(
    private val scheduler: VehicularNotificationScheduler
) {

    /**
     * Agenda las alarmas para el próximo corte del martes.
     */
    operator fun invoke() {
        scheduler.scheduleWeeklyCutoffAlarms()
    }

    /**
     * Cancela las alarmas programadas en caso de desactivación manual.
     */
    fun cancel() {
        scheduler.cancelScheduledAlarms()
    }
}
