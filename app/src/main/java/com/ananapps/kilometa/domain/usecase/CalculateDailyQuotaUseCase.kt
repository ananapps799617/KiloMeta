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
     * @param cycleEndMillis Marca de tiempo límite de finalización del ciclo (opcional).
     * @param cutoffDayOfWeek Día de la semana en que vence el corte (default: Martes).
     * @param cutoffHour Hora límite en que vence el corte (default: 18).
     * @param nowMillis Timestamp Unix en milisegundos para cálculo temporal.
     * @param zoneId Zona horaria para evaluar el calendario (por defecto Bogotá).
     * @return [WeeklyQuotaProgress] con desglose de días restantes y cuota diaria sugerida.
     */
    operator fun invoke(
        weeklyTarget: Double = Constants.DEFAULT_WEEKLY_QUOTA_DOUBLE,
        accumulatedSoFar: Double = 0.0,
        cycleEndMillis: Long? = null,
        cutoffDayOfWeek: DayOfWeek = DayOfWeek.TUESDAY,
        cutoffHour: Int = 18,
        nowMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.of("America/Bogota")
    ): WeeklyQuotaProgress {
        require(weeklyTarget >= 0.0) { "La meta semanal no puede ser negativa" }
        require(accumulatedSoFar >= 0.0) { "El acumulado semanal no puede ser negativo" }

        val zonedDateTime = Instant.ofEpochMilli(nowMillis).atZone(zoneId)
        val daysRemaining = if (cycleEndMillis != null) {
            val endZoned = Instant.ofEpochMilli(cycleEndMillis).atZone(zoneId)
            val durationHours = java.time.Duration.between(zonedDateTime, endZoned).toHours()
            if (durationHours <= 0) 1 else kotlin.math.ceil(durationHours / 24.0).toInt().coerceIn(1, 7)
        } else {
            calculateDaysRemainingToCutoff(zonedDateTime.dayOfWeek, cutoffDayOfWeek)
        }

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
            suggestedDailyQuota = suggestedDailyQuota,
            cutoffDayOfWeek = cutoffDayOfWeek,
            cutoffHour = cutoffHour
        )
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: REGLA DE NEGOCIO DE DÍAS RESTANTES HASTA EL CORTE
    // --------------------------------------------------------------------------
    /**
     * Calcula los días restantes desde el día actual hasta el día de corte semanal.
     */
    fun calculateDaysRemainingToCutoff(currentDay: DayOfWeek, cutoffDay: DayOfWeek): Int {
        val diff = (cutoffDay.value - currentDay.value + 7) % 7
        return if (diff == 0) 1 else diff + 1
    }

    /**
     * Retrocompatibilidad: El ciclo semanal con corte el Martes.
     */
    fun calculateDaysRemainingToTuesday(dayOfWeek: DayOfWeek): Int {
        return calculateDaysRemainingToCutoff(dayOfWeek, DayOfWeek.TUESDAY)
    }
}
