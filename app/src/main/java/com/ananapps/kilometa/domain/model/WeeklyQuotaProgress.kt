// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/WeeklyQuotaProgress.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo inmutable que representa el avance del conductor hacia su meta semanal
// de $500.000 COP (bolsa con corte los martes a las 23:59:59).
// Calcula de forma pedagógica el prorrateo sugerido por día restante.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO INMUTABLE DE PROGRESO DE CUOTA SEMANAL
// ------------------------------------------------------------------------------
/**
 * Representa el estado de cumplimiento de la meta semanal prorrateada.
 *
 * @property weeklyTarget Meta financiera semanal fijada por el conductor (ej. $500.000 COP).
 * @property accumulatedSoFar Beneficio neto acumulado en los turnos de la semana en curso (COP).
 * @property remainingTarget Saldo pendiente para completar la meta semanal (0 si ya se cumplió).
 * @property daysRemaining Días restantes hasta el cierre de ciclo (martes a las 23:59:59), mín 1.
 * @property suggestedDailyQuota Monto sugerido a recaudar por día para alcanzar la meta a tiempo.
 */
data class WeeklyQuotaProgress(
    val weeklyTarget: Double,
    val accumulatedSoFar: Double,
    val remainingTarget: Double,
    val daysRemaining: Int,
    val suggestedDailyQuota: Double,
    val cutoffDayOfWeek: java.time.DayOfWeek = java.time.DayOfWeek.TUESDAY,
    val cutoffHour: Int = 18
) {
    // --------------------------------------------------------------------------
    // BLOQUE 2: PROPIEDADES CALCULADAS PARA UI Y ANÁLISIS
    // --------------------------------------------------------------------------
    /**
     * Porcentaje de avance de la meta semanal en un rango de 0.0 a 100.0%.
     */
    val progressPercentage: Double
        get() = if (weeklyTarget > 0.0) {
            ((accumulatedSoFar / weeklyTarget) * 100.0).coerceIn(0.0, 100.0)
        } else {
            0.0
        }

    /**
     * Fracción normalizada entre 0f y 1f para componentes gráficos de progreso (LinearProgressIndicator).
     */
    val progressFraction: Float
        get() = (progressPercentage / 100.0).toFloat()

    /**
     * Indica si el conductor ya superó o igualó la meta de la bolsa semanal.
     */
    val isWeeklyTargetAchieved: Boolean
        get() = accumulatedSoFar >= weeklyTarget
}
