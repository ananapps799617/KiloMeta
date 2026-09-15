// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/CalculateWeeklyCycleBoundsUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso canónico de Dominio que calcula con precisión milimétrica los límites
// (inicio y fin) del ciclo semanal según el día y hora de corte configurados.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Calcula los timestamps Unix (milisegundos) de inicio y fin del ciclo semanal
 * financiero respetando el día y la hora límite de pago configurada por el conductor.
 *
 * Regla de negocio: Si el día de corte es Martes (23:59), el ciclo inicia el Miércoles a las
 * 00:00:00 y finaliza el Martes a las 23:59:59 (ventana continua de 168 horas).
 */
class CalculateWeeklyCycleBoundsUseCase @Inject constructor() {

    operator fun invoke(
        nowMillis: Long = System.currentTimeMillis(),
        cutoffDay: DayOfWeek = DayOfWeek.TUESDAY,
        cutoffHour: Int = 23,
        zoneId: ZoneId = ZoneId.of("America/Bogota")
    ): Pair<Long, Long> {
        val now = Instant.ofEpochMilli(nowMillis).atZone(zoneId)

        var cutoffCandidate = if (cutoffHour >= 23) {
            now.with(TemporalAdjusters.nextOrSame(cutoffDay))
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999_000_000)
        } else {
            now.with(TemporalAdjusters.nextOrSame(cutoffDay))
                .withHour(cutoffHour)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
        }

        if (!now.isBefore(cutoffCandidate)) {
            cutoffCandidate = cutoffCandidate.plusWeeks(1)
        }

        val cycleEnd = cutoffCandidate
        val cycleStart = if (cutoffHour >= 23) {
            cycleEnd.minusDays(6).toLocalDate().atStartOfDay(zoneId)
        } else {
            cycleEnd.minusWeeks(1)
        }

        return Pair(
            cycleStart.toInstant().toEpochMilli(),
            cycleEnd.toInstant().toEpochMilli()
        )
    }
}
