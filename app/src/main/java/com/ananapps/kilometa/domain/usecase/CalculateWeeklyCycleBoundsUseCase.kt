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
        val today = now.toLocalDate()

        // El día de corte es el último día del ciclo semanal (cierra a las 23:59:59.999)
        val endLocalDate = today.with(TemporalAdjusters.nextOrSame(cutoffDay))
        val cycleEnd = endLocalDate.atTime(23, 59, 59, 999_000_000).atZone(zoneId)

        // El ciclo inicia 6 días antes a las 00:00:00 (ej: si corte es Martes, inicia el Miércoles a las 00:00:00)
        val startLocalDate = endLocalDate.minusDays(6)
        val cycleStart = startLocalDate.atStartOfDay(zoneId)

        return Pair(
            cycleStart.toInstant().toEpochMilli(),
            cycleEnd.toInstant().toEpochMilli()
        )
    }
}
