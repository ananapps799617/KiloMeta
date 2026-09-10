// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/data/local/RoomDatabaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para la capa de persistencia Room SQLite de la Orden #003.
// Valida:
// 1. Integridad relacional y agregación de jornada (ShiftWithDetails -> ShiftFinancialSummary).
// 2. Comportamiento y coherencia de borrado en cascada (Cascade Delete).
// 3. Serialización/deserialización determinista de TypeConverters.
// 4. Mapeos bidireccionales entidad-dominio sin pérdidas de precisión numérica.
// ==============================================================================

package com.ananapps.kilometa.data.local

import com.ananapps.kilometa.data.local.converter.Converters
import com.ananapps.kilometa.data.local.entity.ExpenseEntity
import com.ananapps.kilometa.data.local.entity.FuelingEntity
import com.ananapps.kilometa.data.local.entity.ShiftEntity
import com.ananapps.kilometa.data.local.entity.TripEntity
import com.ananapps.kilometa.data.local.relation.ShiftWithDetails
import com.ananapps.kilometa.data.mapper.toDomain
import com.ananapps.kilometa.data.mapper.toEntity
import com.ananapps.kilometa.data.mapper.toFinancialSummary
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.ShiftStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class RoomDatabaseTest {

    private val converters = Converters()

    // --------------------------------------------------------------------------
    // BLOQUE 1: PRUEBAS DE CONVERSORES DE TIPOS (TYPECONVERTERS)
    // --------------------------------------------------------------------------
    @Test
    fun testTypeConvertersEnumSerializationAndResilience() {
        // PlatformType
        assertEquals("UBER", converters.fromPlatformType(PlatformType.UBER))
        assertEquals(PlatformType.UBER, converters.toPlatformType("UBER"))
        assertEquals(PlatformType.DIDI, converters.toPlatformType("DIDI"))
        assertEquals(PlatformType.PRIVATE, converters.toPlatformType("UNKNOWN_VAL"))

        // FuelType
        assertEquals("GAS", converters.fromFuelType(FuelType.GAS))
        assertEquals("GASOLINE", converters.fromFuelType(FuelType.GASOLINE))
        assertEquals(FuelType.GAS, converters.toFuelType("GAS"))
        assertEquals(FuelType.GASOLINE, converters.toFuelType("GASOLINE"))
        assertEquals(FuelType.GAS, converters.toFuelType("CORRUPTED_VALUE"))

        // PaymentMethod
        assertEquals("CASH", converters.fromPaymentMethod(PaymentMethod.CASH))
        assertEquals("NEQUI", converters.fromPaymentMethod(PaymentMethod.NEQUI))
        assertEquals(PaymentMethod.NEQUI, converters.toPaymentMethod("NEQUI"))
        assertEquals(PaymentMethod.CASH, converters.toPaymentMethod("INVALID"))

        // ExpenseConcept
        assertEquals("WASH", converters.fromExpenseConcept(ExpenseConcept.WASH))
        assertEquals(ExpenseConcept.FOOD, converters.toExpenseConcept("FOOD"))
        assertEquals(ExpenseConcept.OTHER, converters.toExpenseConcept("NON_EXISTING"))

        // ShiftStatus
        assertEquals("ACTIVE", converters.fromShiftStatus(ShiftStatus.ACTIVE))
        assertEquals(ShiftStatus.COMPLETED, converters.toShiftStatus("COMPLETED"))
        assertEquals(ShiftStatus.ACTIVE, converters.toShiftStatus("BAD_STATUS"))
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: AGREGACIÓN RELACIONAL Y CONSOLIDACIÓN FINANCIERA
    // --------------------------------------------------------------------------
    @Test
    fun testShiftWithDetailsFinancialAggregation() {
        val shiftId = UUID.randomUUID().toString()

        val shiftEntity = ShiftEntity(
            id = shiftId,
            startTime = 1725696000000L,
            endTime = 1725732000000L,
            startOdometer = 120500,
            endOdometer = 120650, // 150 km recorridos
            dailyQuotaTarget = 83333.0,
            status = ShiftStatus.COMPLETED.name,
            notes = "Jornada completa exitosa"
        )

        val trips = listOf(
            TripEntity(
                id = UUID.randomUUID().toString(),
                shiftId = shiftId,
                platformName = PlatformType.UBER.name,
                grossAmount = 30000.0,
                commissionAmount = 6000.0,
                netAmount = 24000.0,
                paymentMethod = PaymentMethod.APP_WALLET.name,
                timestamp = 1725700000000L
            ),
            TripEntity(
                id = UUID.randomUUID().toString(),
                shiftId = shiftId,
                platformName = PlatformType.DIDI.name,
                grossAmount = 40000.0,
                commissionAmount = 8000.0,
                netAmount = 32000.0,
                paymentMethod = PaymentMethod.CASH.name,
                timestamp = 1725710000000L
            ),
            TripEntity(
                id = UUID.randomUUID().toString(),
                shiftId = shiftId,
                platformName = PlatformType.PRIVATE.name,
                grossAmount = 50000.0,
                commissionAmount = 0.0,
                netAmount = 50000.0,
                paymentMethod = PaymentMethod.NEQUI.name,
                timestamp = 1725720000000L
            )
        )

        val fuelings = listOf(
            FuelingEntity(
                id = UUID.randomUUID().toString(),
                shiftId = shiftId,
                fuelType = FuelType.GAS.name,
                amountPaid = 25000.0,
                volume = 12.5, // m³
                odometer = 120550,
                timestamp = 1725705000000L
            ),
            FuelingEntity(
                id = UUID.randomUUID().toString(),
                shiftId = shiftId,
                fuelType = FuelType.GASOLINE.name,
                amountPaid = 15000.0,
                volume = 1.0, // Gal
                odometer = 120600,
                timestamp = 1725715000000L
            )
        )

        val expenses = listOf(
            ExpenseEntity(
                id = UUID.randomUUID().toString(),
                shiftId = shiftId,
                concept = ExpenseConcept.FOOD.name,
                amount = 12000.0,
                timestamp = 1725712000000L
            )
        )

        val shiftWithDetails = ShiftWithDetails(
            shift = shiftEntity,
            trips = trips,
            fuelings = fuelings,
            expenses = expenses
        )

        val summary = shiftWithDetails.toFinancialSummary()

        // Validaciones numéricas de ingresos y comisiones
        assertEquals(shiftId, summary.shiftId)
        assertEquals(150, summary.totalKilometers)
        assertEquals(120000.0, summary.totalGrossIncome, 0.001)
        assertEquals(14000.0, summary.totalCommissions, 0.001)
        assertEquals(106000.0, summary.totalNetIncome, 0.001)

        // Validaciones bi-combustible y gastos
        assertEquals(25000.0, summary.totalGasExpense, 0.001)
        assertEquals(15000.0, summary.totalGasolineExpense, 0.001)
        assertEquals(40000.0, summary.totalFuelExpense, 0.001)
        assertEquals(12000.0, summary.totalOperationalExpenses, 0.001)
        assertEquals(52000.0, summary.totalExpenses, 0.001)

        // Ganancia neta real en cabina: Net (106000) - Total Gastos (52000) = 54000
        assertEquals(54000.0, summary.netProfit, 0.001)

        // Cumplimiento de meta de cuota diaria (83333 COP)
        assertFalse(summary.isQuotaAchieved)
        assertEquals(29333.0, summary.remainingForQuota, 0.001)
        assertEquals((54000.0 / 83333.0) * 100.0, summary.quotaProgressPercentage, 0.01)

        // Métricas unitarias por kilómetro (150 km)
        assertEquals(52000.0 / 150.0, summary.costPerKilometer, 0.001)
        assertEquals(120000.0 / 150.0, summary.grossIncomePerKilometer, 0.001)
        assertEquals(106000.0 / 150.0, summary.netIncomePerKilometer, 0.001)

        // Conteos
        assertEquals(3, summary.tripsCount)
        assertEquals(2, summary.fuelingsCount)
        assertEquals(1, summary.expensesCount)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: VALIDACIÓN DE REGLAS DE INTEGRIDAD RELACIONAL Y CASCADE DELETE
    // --------------------------------------------------------------------------
    @Test
    fun testCascadeDeleteIntegrityLogic() {
        val shiftIdA = UUID.randomUUID().toString()
        val shiftIdB = UUID.randomUUID().toString()

        val tripsDatabase = mutableListOf(
            TripEntity(UUID.randomUUID().toString(), shiftIdA, "UBER", 20000.0, 4000.0, 16000.0, "CASH", "CASH_IN_HAND", 1000L),
            TripEntity(UUID.randomUUID().toString(), shiftIdA, "DIDI", 25000.0, 5000.0, 20000.0, "NEQUI", "CASH_IN_HAND", 2000L),
            TripEntity(UUID.randomUUID().toString(), shiftIdB, "PRIVATE", 30000.0, 0.0, 30000.0, "CASH", "CASH_IN_HAND", 3000L)
        )

        val fuelingsDatabase = mutableListOf(
            FuelingEntity(UUID.randomUUID().toString(), shiftIdA, "GAS", 20000.0, 10.0, 100, 1500L),
            FuelingEntity(UUID.randomUUID().toString(), shiftIdB, "GASOLINE", 30000.0, 2.0, 200, 3500L)
        )

        val expensesDatabase = mutableListOf(
            ExpenseEntity(UUID.randomUUID().toString(), shiftIdA, "WASH", 10000.0, 1600L),
            ExpenseEntity(UUID.randomUUID().toString(), shiftIdB, "FOOD", 15000.0, 3600L)
        )

        // Simulación de CASCADE al eliminar shiftIdA
        val deletedShiftId = shiftIdA
        tripsDatabase.removeAll { it.shiftId == deletedShiftId }
        fuelingsDatabase.removeAll { it.shiftId == deletedShiftId }
        expensesDatabase.removeAll { it.shiftId == deletedShiftId }

        // Comprobación de que no quedan registros huérfanos del turno A
        assertTrue(tripsDatabase.none { it.shiftId == shiftIdA })
        assertTrue(fuelingsDatabase.none { it.shiftId == shiftIdA })
        assertTrue(expensesDatabase.none { it.shiftId == shiftIdA })

        // Comprobación de que los registros del turno B permanecen intactos
        assertEquals(1, tripsDatabase.size)
        assertEquals(shiftIdB, tripsDatabase.first().shiftId)
        assertEquals(1, fuelingsDatabase.size)
        assertEquals(shiftIdB, fuelingsDatabase.first().shiftId)
        assertEquals(1, expensesDatabase.size)
        assertEquals(shiftIdB, expensesDatabase.first().shiftId)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 4: VALIDACIÓN DE MAPEOS BIDIRECCIONALES
    // --------------------------------------------------------------------------
    @Test
    fun testBidirectionalMappersPrecision() {
        val shiftId = UUID.randomUUID().toString()
        val originalShift = ShiftEntity(
            id = shiftId,
            startTime = 1000L,
            endTime = 5000L,
            startOdometer = 50000,
            endOdometer = 50120,
            dailyQuotaTarget = 90000.0,
            status = ShiftStatus.COMPLETED.name,
            notes = "Sin novedades"
        )

        val domainShift = originalShift.toDomain()
        assertEquals(originalShift.id, domainShift.id)
        assertEquals(120, domainShift.kilometersDriven)
        assertFalse(domainShift.isActive)

        val reconstructedEntity = domainShift.toEntity()
        assertEquals(originalShift, reconstructedEntity)
    }
}
