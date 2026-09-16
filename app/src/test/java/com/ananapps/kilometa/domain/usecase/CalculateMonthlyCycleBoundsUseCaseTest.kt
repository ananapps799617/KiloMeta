// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/CalculateMonthlyCycleBoundsUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para el cálculo de límites de ciclo mensual (Orden #042).
// Valida el primer día 00:00:00 al último día 23:59:59 y navegación con monthOffset.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime

class CalculateMonthlyCycleBoundsUseCaseTest {

    private lateinit var useCase: CalculateMonthlyCycleBoundsUseCase
    private val zoneId = ZoneId.of("America/Bogota")

    @Before
    fun setUp() {
        useCase = CalculateMonthlyCycleBoundsUseCase()
    }

    @Test
    fun `mes actual inicia el dia 1 a las 00h y termina el ultimo dia a las 23h59`() {
        val now = ZonedDateTime.of(2026, 9, 16, 12, 0, 0, 0, zoneId)
        val (startMillis, endMillis) = useCase(
            nowMillis = now.toInstant().toEpochMilli(),
            monthOffset = 0,
            zoneId = zoneId
        )

        val start = Instant.ofEpochMilli(startMillis).atZone(zoneId)
        val end = Instant.ofEpochMilli(endMillis).atZone(zoneId)

        assertEquals(1, start.dayOfMonth)
        assertEquals(Month.SEPTEMBER, start.month)
        assertEquals(0, start.hour)
        assertEquals(0, start.minute)

        assertEquals(30, end.dayOfMonth)
        assertEquals(Month.SEPTEMBER, end.month)
        assertEquals(23, end.hour)
        assertEquals(59, end.minute)
    }

    @Test
    fun `cuando monthOffset es 1, retrocede al mes anterior completo`() {
        val now = ZonedDateTime.of(2026, 9, 16, 12, 0, 0, 0, zoneId)
        val (startMillis, endMillis) = useCase(
            nowMillis = now.toInstant().toEpochMilli(),
            monthOffset = 1,
            zoneId = zoneId
        )

        val start = Instant.ofEpochMilli(startMillis).atZone(zoneId)
        val end = Instant.ofEpochMilli(endMillis).atZone(zoneId)

        assertEquals(1, start.dayOfMonth)
        assertEquals(Month.AUGUST, start.month)
        assertEquals(31, end.dayOfMonth)
        assertEquals(Month.AUGUST, end.month)
    }
}
