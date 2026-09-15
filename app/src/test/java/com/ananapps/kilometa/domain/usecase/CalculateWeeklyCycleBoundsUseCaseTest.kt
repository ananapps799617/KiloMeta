// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/CalculateWeeklyCycleBoundsUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para validar el cálculo dinámico de ciclos semanales
// según el día de corte configurado por el conductor (ej. Martes -> Miércoles a Martes).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class CalculateWeeklyCycleBoundsUseCaseTest {

    private lateinit var useCase: CalculateWeeklyCycleBoundsUseCase
    private val zoneId = ZoneId.of("America/Bogota")

    @Before
    fun setUp() {
        useCase = CalculateWeeklyCycleBoundsUseCase()
    }

    @Test
    fun `cuando corte es Martes y hoy es Miercoles, ciclo va de Miercoles a Martes`() {
        // Miércoles 9 de septiembre de 2026 a las 10:00 AM
        val wednesday = ZonedDateTime.of(2026, 9, 9, 10, 0, 0, 0, zoneId)
        val (startMillis, endMillis) = useCase(
            nowMillis = wednesday.toInstant().toEpochMilli(),
            cutoffDay = DayOfWeek.TUESDAY,
            cutoffHour = 23,
            zoneId = zoneId
        )

        val startZoned = Instant.ofEpochMilli(startMillis).atZone(zoneId)
        val endZoned = Instant.ofEpochMilli(endMillis).atZone(zoneId)

        // Inicio: Miércoles 9 de septiembre a las 00:00:00
        assertEquals(DayOfWeek.WEDNESDAY, startZoned.dayOfWeek)
        assertEquals(9, startZoned.dayOfMonth)
        assertEquals(0, startZoned.hour)
        assertEquals(0, startZoned.minute)

        // Fin: Martes 15 de septiembre a las 23:59:59
        assertEquals(DayOfWeek.TUESDAY, endZoned.dayOfWeek)
        assertEquals(15, endZoned.dayOfMonth)
        assertEquals(23, endZoned.hour)
        assertEquals(59, endZoned.minute)
    }

    @Test
    fun `cuando corte es Martes y hoy es Martes en la manana, ciclo inicio el Miercoles anterior`() {
        // Martes 15 de septiembre de 2026 a las 08:00 AM (antes del corte)
        val tuesdayMorning = ZonedDateTime.of(2026, 9, 15, 8, 0, 0, 0, zoneId)
        val (startMillis, endMillis) = useCase(
            nowMillis = tuesdayMorning.toInstant().toEpochMilli(),
            cutoffDay = DayOfWeek.TUESDAY,
            cutoffHour = 23,
            zoneId = zoneId
        )

        val startZoned = Instant.ofEpochMilli(startMillis).atZone(zoneId)
        val endZoned = Instant.ofEpochMilli(endMillis).atZone(zoneId)

        // Inicio: Miércoles 9 de septiembre (hace 6 días)
        assertEquals(DayOfWeek.WEDNESDAY, startZoned.dayOfWeek)
        assertEquals(9, startZoned.dayOfMonth)

        // Fin: Martes 15 de septiembre a las 23:59:59 (hoy)
        assertEquals(DayOfWeek.TUESDAY, endZoned.dayOfWeek)
        assertEquals(15, endZoned.dayOfMonth)
        assertEquals(23, endZoned.hour)
    }

    @Test
    fun `cuando corte es Domingo, semana estandar va de Lunes a Domingo`() {
        // Jueves 10 de septiembre de 2026 a las 14:00
        val thursday = ZonedDateTime.of(2026, 9, 10, 14, 0, 0, 0, zoneId)
        val (startMillis, endMillis) = useCase(
            nowMillis = thursday.toInstant().toEpochMilli(),
            cutoffDay = DayOfWeek.SUNDAY,
            cutoffHour = 23,
            zoneId = zoneId
        )

        val startZoned = Instant.ofEpochMilli(startMillis).atZone(zoneId)
        val endZoned = Instant.ofEpochMilli(endMillis).atZone(zoneId)

        // Inicio: Lunes 7 de septiembre
        assertEquals(DayOfWeek.MONDAY, startZoned.dayOfWeek)
        assertEquals(7, startZoned.dayOfMonth)

        // Fin: Domingo 13 de septiembre
        assertEquals(DayOfWeek.SUNDAY, endZoned.dayOfWeek)
        assertEquals(13, endZoned.dayOfMonth)
    }
}
