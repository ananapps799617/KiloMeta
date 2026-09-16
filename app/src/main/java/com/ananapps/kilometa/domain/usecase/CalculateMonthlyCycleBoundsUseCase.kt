// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/CalculateMonthlyCycleBoundsUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para calcular los límites de inicio y fin de mes con soporte para monthOffset (Orden #042).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Calcula los límites temporales (00:00:00 del primer día a 23:59:59 del último día)
 * de un mes calendario, permitiendo auditar meses anteriores mediante [monthOffset].
 */
class CalculateMonthlyCycleBoundsUseCase @Inject constructor() {

    operator fun invoke(
        nowMillis: Long = System.currentTimeMillis(),
        monthOffset: Int = 0,
        zoneId: ZoneId = ZoneId.of("America/Bogota")
    ): Pair<Long, Long> {
        val now = Instant.ofEpochMilli(nowMillis).atZone(zoneId)
        val targetDate = now.toLocalDate().minusMonths(monthOffset.toLong())

        val startOfMonth = targetDate.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(zoneId)
        val endOfMonth = targetDate.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59, 999_000_000).atZone(zoneId)

        return Pair(
            startOfMonth.toInstant().toEpochMilli(),
            endOfMonth.toInstant().toEpochMilli()
        )
    }

    operator fun invoke(monthOffset: Int): Pair<Long, Long> = invoke(
        nowMillis = System.currentTimeMillis(),
        monthOffset = monthOffset
    )
}
