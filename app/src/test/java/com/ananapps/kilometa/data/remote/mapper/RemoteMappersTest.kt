// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/data/remote/mapper/RemoteMappersTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para validación de mapeos bidireccionales Room <-> DTO.
// Garantiza paridad de datos y ausencia de pérdida de información en la sincronización.
// ==============================================================================

package com.ananapps.kilometa.data.remote.mapper

import com.ananapps.kilometa.data.local.entity.ExpenseEntity
import com.ananapps.kilometa.data.local.entity.FuelingEntity
import com.ananapps.kilometa.data.local.entity.ShiftEntity
import com.ananapps.kilometa.data.local.entity.TripEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteMappersTest {

    // --------------------------------------------------------------------------
    // BLOQUE 1: MAPEO BIDIRECCIONAL DE JORNADAS (SHIFTS)
    // --------------------------------------------------------------------------
    @Test
    fun testShiftEntityToDtoAndBack() {
        val entity = ShiftEntity(
            id = "shift-001",
            startTime = 1700000000000L,
            endTime = 1700036000000L,
            startOdometer = 120000,
            endOdometer = 120150,
            dailyQuotaTarget = 83333.0,
            status = "COMPLETED",
            notes = "Jornada tranquila"
        )

        val dto = entity.toDto(updatedAt = 1700036000500L, deleted = false)

        assertEquals(entity.id, dto.id)
        assertEquals(entity.startTime, dto.startTime)
        assertEquals(entity.endTime, dto.endTime)
        assertEquals(entity.startOdometer, dto.startOdometer)
        assertEquals(entity.endOdometer, dto.endOdometer)
        assertEquals(entity.dailyQuotaTarget, dto.dailyQuotaTarget, 0.001)
        assertEquals(entity.status, dto.status)
        assertEquals(entity.notes, dto.notes)
        assertEquals(1700036000500L, dto.updatedAt)
        assertFalse(dto.deleted)

        val restoredEntity = dto.toEntity()
        assertEquals(entity, restoredEntity)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: MAPEO BIDIRECCIONAL DE CARRERAS (TRIPS)
    // --------------------------------------------------------------------------
    @Test
    fun testTripEntityToDtoAndBack() {
        val entity = TripEntity(
            id = "trip-101",
            shiftId = "shift-001",
            platformName = "UBER",
            grossAmount = 35000.0,
            commissionAmount = 7000.0,
            netAmount = 28000.0,
            paymentMethod = "APP_WALLET",
            paymentDestination = "APP_RETAINED",
            timestamp = 1700010000000L,
            startOdometer = 207800,
            endOdometer = 207835
        )

        val dto = entity.toDto(updatedAt = 1700010001000L, deleted = false)

        assertEquals(entity.id, dto.id)
        assertEquals(entity.shiftId, dto.shiftId)
        assertEquals(entity.platformName, dto.platformName)
        assertEquals(entity.grossAmount, dto.grossAmount, 0.001)
        assertEquals(entity.commissionAmount, dto.commissionAmount, 0.001)
        assertEquals(entity.netAmount, dto.netAmount, 0.001)
        assertEquals(entity.paymentMethod, dto.paymentMethod)
        assertEquals(entity.paymentDestination, dto.paymentDestination)
        assertEquals(entity.timestamp, dto.timestamp)
        assertEquals(entity.startOdometer, dto.startOdometer)
        assertEquals(entity.endOdometer, dto.endOdometer)

        val restoredEntity = dto.toEntity()
        assertEquals(entity, restoredEntity)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: MAPEO BIDIRECCIONAL DE COMBUSTIBLE (FUELINGS)
    // --------------------------------------------------------------------------
    @Test
    fun testFuelingEntityToDtoAndBack() {
        val entity = FuelingEntity(
            id = "fuel-201",
            shiftId = "shift-001",
            fuelType = "GAS",
            amountPaid = 28000.0,
            volume = 12.5,
            odometer = 120080,
            timestamp = 1700020000000L
        )

        val dto = entity.toDto()

        assertEquals(entity.id, dto.id)
        assertEquals(entity.shiftId, dto.shiftId)
        assertEquals(entity.fuelType, dto.fuelType)
        assertEquals(entity.amountPaid, dto.amountPaid, 0.001)
        assertEquals(entity.volume, dto.volume, 0.001)
        assertEquals(entity.odometer, dto.odometer)
        assertEquals(entity.timestamp, dto.timestamp)

        val restoredEntity = dto.toEntity()
        assertEquals(entity, restoredEntity)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 4: MAPEO BIDIRECCIONAL DE GASTOS OPERATIVOS (EXPENSES)
    // --------------------------------------------------------------------------
    @Test
    fun testExpenseEntityToDtoAndBack() {
        val entity = ExpenseEntity(
            id = "exp-301",
            shiftId = "shift-001",
            concept = "WASH",
            amount = 6000.0,
            timestamp = 1700030000000L
        )

        val dto = entity.toDto()

        assertEquals(entity.id, dto.id)
        assertEquals(entity.shiftId, dto.shiftId)
        assertEquals(entity.concept, dto.concept)
        assertEquals(entity.amount, dto.amount, 0.001)
        assertEquals(entity.timestamp, dto.timestamp)

        val restoredEntity = dto.toEntity()
        assertEquals(entity, restoredEntity)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 5: MAPEO DE COLECCIONES
    // --------------------------------------------------------------------------
    @Test
    fun testListMappers() {
        val shifts = listOf(
            ShiftEntity("s1", 100L, null, 1000, null, 80000.0, "ACTIVE", null),
            ShiftEntity("s2", 200L, 300L, 1100, 1200, 80000.0, "COMPLETED", null)
        )

        val dtos = shifts.toShiftDtoList()
        assertEquals(2, dtos.size)

        val entities = dtos.toShiftEntityList()
        assertEquals(shifts, entities)
    }
}
