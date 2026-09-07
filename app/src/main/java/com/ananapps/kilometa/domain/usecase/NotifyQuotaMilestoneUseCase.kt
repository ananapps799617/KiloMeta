// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/NotifyQuotaMilestoneUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso que evalúa el cumplimiento del 100% de metas (diarias o semanales)
// y dispara la notificación de celebración con política DataGuard (cero cifras líquidas expuestas).
// Regla de Oro: Cero dependencias de Android framework en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.WeeklyQuotaProgress
import com.ananapps.kilometa.domain.repository.VehicularNotificationNotifier
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE NOTIFICACIÓN DE HITOS DE CUOTA
// ------------------------------------------------------------------------------
/**
 * Emite notificaciones de celebración seguras al alcanzar metas financieras.
 */
class NotifyQuotaMilestoneUseCase @Inject constructor(
    private val notifier: VehicularNotificationNotifier
) {

    /**
     * Evalúa el progreso de la bolsa semanal y notifica si se ha alcanzado la meta.
     *
     * @param quotaProgress Estado actual del progreso de la cuota semanal.
     */
    fun checkAndNotifyWeeklyMilestone(quotaProgress: WeeklyQuotaProgress) {
        if (quotaProgress.isWeeklyTargetAchieved) {
            notifier.showMilestoneCelebration(
                title = "¡Meta Semanal Cumplida! 🎯",
                message = "Has completado con éxito la bolsa de la semana. ¡Excelente labor en ruta!"
            )
        }
    }

    /**
     * Evalúa el cierre de una jornada y notifica si se cubrió la cuota diaria.
     *
     * @param summary Resumen financiero de la jornada de trabajo.
     */
    fun checkAndNotifyShiftMilestone(summary: ShiftFinancialSummary) {
        if (summary.isQuotaAchieved) {
            notifier.showMilestoneCelebration(
                title = "¡Meta de Jornada Alcanzada! 🏁",
                message = "Cubriste la meta del día. Cada kilómetro adicional ahora es ganancia pura."
            )
        }
    }
}
