// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/CalculateDailyQuotaUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para CalculateDailyQuotaUseCase validando el cálculo
// dinámico de días restantes y cuota prorrateada ante cualquier día y hora de corte.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

class CalculateDailyQuotaUseCaseTest {

    private lateinit var useCase: CalculateDailyQuotaUseCase
    private val bogotaZone = ZoneId.of("America/Bogota")

    @Before
    fun setUp() {
        useCase = CalculateDailyQuotaUseCase()
    }

    @Test
    fun `calcula cuota diaria cuando faltan 5 dias con meta semanal de 500k`() {
        // GIVEN: Meta 500k, acumulado 100k, faltante 400k
        val now = ZonedDateTime.of(2026, 9, 3, 18, 0, 0, 0, bogotaZone) // Jueves 18:00
        val cycleEnd = ZonedDateTime.of(2026, 9, 8, 18, 0, 0, 0, bogotaZone) // Martes 18:00 (exactamente 5 días / 120 hrs)

        // WHEN
        val progress = useCase(
            weeklyTarget = 500_000.0,
            accumulatedSoFar = 100_000.0,
            cycleEndMillis = cycleEnd.toInstant().toEpochMilli(),
            cutoffDayOfWeek = DayOfWeek.TUESDAY,
            cutoffHour = 18,
            nowMillis = now.toInstant().toEpochMilli(),
            zoneId = bogotaZone
        )

        // THEN
        assertEquals(500_000.0, progress.weeklyTarget, 0.001)
        assertEquals(100_000.0, progress.accumulatedSoFar, 0.001)
        assertEquals(400_000.0, progress.remainingTarget, 0.001)
        assertEquals(5, progress.daysRemaining)
        assertEquals(80_000.0, progress.suggestedDailyQuota, 0.001) // 400k / 5 = 80k
        assertEquals(DayOfWeek.TUESDAY, progress.cutoffDayOfWeek)
        assertEquals(18, progress.cutoffHour)
    }

    @Test
    fun `meta completada retorna remainingTarget cero y sugerido cero`() {
        val now = ZonedDateTime.of(2026, 9, 3, 10, 0, 0, 0, bogotaZone)
        val cycleEnd = ZonedDateTime.of(2026, 9, 8, 18, 0, 0, 0, bogotaZone)

        val progress = useCase(
            weeklyTarget = 500_000.0,
            accumulatedSoFar = 550_000.0,
            cycleEndMillis = cycleEnd.toInstant().toEpochMilli(),
            cutoffDayOfWeek = DayOfWeek.TUESDAY,
            cutoffHour = 18,
            nowMillis = now.toInstant().toEpochMilli(),
            zoneId = bogotaZone
        )

        assertEquals(0.0, progress.remainingTarget, 0.001)
        assertEquals(0.0, progress.suggestedDailyQuota, 0.001)
        assertTrue(progress.isWeeklyTargetAchieved)
    }

    @Test
    fun `vehiculo con cuota cero no lanza division por cero y retorna cero`() {
        val now = ZonedDateTime.of(2026, 9, 3, 10, 0, 0, 0, bogotaZone)

        val progress = useCase(
            weeklyTarget = 0.0,
            accumulatedSoFar = 0.0,
            nowMillis = now.toInstant().toEpochMilli(),
            zoneId = bogotaZone
        )

        assertEquals(0.0, progress.remainingTarget, 0.001)
        assertEquals(0.0, progress.suggestedDailyQuota, 0.001)
        assertTrue(progress.isWeeklyTargetAchieved)
    }
}
