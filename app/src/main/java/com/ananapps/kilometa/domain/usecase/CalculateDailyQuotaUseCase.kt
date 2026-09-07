// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/CalculateDailyQuotaUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso que calcula el prorrateo dinámico de la meta semanal ($500.000 COP)
// basándose en el ciclo operacional con corte los martes a las 23:59:59.
// Determina los días restantes del ciclo y la cuota diaria sugerida para cumplir la meta.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.WeeklyQuotaProgress
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE PRORRATEO DE CUOTA SEMANAL
// ------------------------------------------------------------------------------
/**
 * Calcula de manera determinista y libre de efectos secundarios el progreso
 * hacia la meta semanal de bolsa ($500.000 COP) y el prorrateo diario exigido.
 */
class CalculateDailyQuotaUseCase @Inject constructor() {

    /**
     * Ejecuta el cálculo de prorrateo para el momento dado.
     *
     * @param weeklyTarget Meta total semanal en COP (por defecto $500.000).
     * @param accumulatedSoFar Ingreso neto acumulado en la semana en curso (COP).
     * @param nowMillis Timestamp Unix en milisegundos para cálculo temporal.
     * @param zoneId Zona horaria para evaluar el calendario (por defecto local).
     * @return [WeeklyQuotaProgress] con desglose de días restantes y cuota diaria sugerida.
     */
    operator fun invoke(
        weeklyTarget: Double = Constants.DEFAULT_WEEKLY_QUOTA_DOUBLE,
        accumulatedSoFar: Double = 0.0,
        nowMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): WeeklyQuotaProgress {
        require(weeklyTarget >= 0.0) { "La meta semanal no puede ser negativa" }
        require(accumulatedSoFar >= 0.0) { "El acumulado semanal no puede ser negativo" }

        val zonedDateTime = Instant.ofEpochMilli(nowMillis).atZone(zoneId)
        val daysRemaining = calculateDaysRemainingToTuesday(zonedDateTime.dayOfWeek)

        val remainingTarget = if (accumulatedSoFar >= weeklyTarget) {
            0.0
        } else {
            weeklyTarget - accumulatedSoFar
        }

        val suggestedDailyQuota = if (remainingTarget <= 0.0 || daysRemaining <= 0) {
            0.0
        } else {
            remainingTarget / daysRemaining
        }

        return WeeklyQuotaProgress(
            weeklyTarget = weeklyTarget,
            accumulatedSoFar = accumulatedSoFar,
            remainingTarget = remainingTarget,
            daysRemaining = daysRemaining,
            suggestedDailyQuota = suggestedDailyQuota
        )
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: REGLA DE NEGOCIO DE DÍAS RESTANTES (CORTE MARTES 23:59:59)
    // --------------------------------------------------------------------------
    /**
     * El ciclo semanal inicia el Miércoles a las 00:00:00 y finaliza el Martes a las 23:59:59.
     * - Miércoles: 7 días restantes (incluye el miércoles completo).
     * - Jueves: 6 días restantes.
     * - Viernes: 5 días restantes.
     * - Sábado: 4 días restantes.
     * - Domingo: 3 días restantes.
     * - Lunes: 2 días restantes.
     * - Martes: 1 día restante (día de cierre de liquidación semanal).
     */
    fun calculateDaysRemainingToTuesday(dayOfWeek: DayOfWeek): Int {
        return when (dayOfWeek) {
            DayOfWeek.WEDNESDAY -> 7
            DayOfWeek.THURSDAY -> 6
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 4
            DayOfWeek.SUNDAY -> 3
            DayOfWeek.MONDAY -> 2
            DayOfWeek.TUESDAY -> 1
        }
    }
}
