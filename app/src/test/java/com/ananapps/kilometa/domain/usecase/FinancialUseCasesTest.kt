// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/FinancialUseCasesTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias exhaustiva para la lógica de Dominio de la Orden #004.
// Valida:
// 1. Prorrateo de cuota semanal ($500k) y cálculo determinista de días al martes.
// 2. Transiciones del Semáforo Financiero (Rojo déficit, Ámbar aceleración, Verde Turno Libre).
// 3. Domain Guards estrictos (rechazo de montos negativos, comisiones > tarifa, volumen <= 0).
// 4. Protección matemática contra división por cero en vehículos detenidos (km = 0).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.domain.model.FinancialStatus
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.ShiftStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

class FinancialUseCasesTest {

    private lateinit var calculateDailyQuotaUseCase: CalculateDailyQuotaUseCase
    private lateinit var getShiftFinancialStatusUseCase: GetShiftFinancialStatusUseCase
    private lateinit var calculateBiFuelCostPerKmUseCase: CalculateBiFuelCostPerKmUseCase
    private lateinit var registerTripUseCase: RegisterTripUseCase
    private lateinit var registerFuelingUseCase: RegisterFuelingUseCase
    private lateinit var registerExpenseUseCase: RegisterExpenseUseCase
    private lateinit var manageShiftUseCase: ManageShiftUseCase

    // Fakes en memoria para repositorios
    private lateinit var fakeTripRepository: FakeTripRepository
    private lateinit var fakeFuelingRepository: FakeFuelingRepository
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var fakeShiftRepository: FakeShiftRepository

    @Before
    fun setUp() {
        calculateDailyQuotaUseCase = CalculateDailyQuotaUseCase()
        getShiftFinancialStatusUseCase = GetShiftFinancialStatusUseCase()
        calculateBiFuelCostPerKmUseCase = CalculateBiFuelCostPerKmUseCase()

        fakeTripRepository = FakeTripRepository()
        fakeFuelingRepository = FakeFuelingRepository()
        fakeExpenseRepository = FakeExpenseRepository()
        fakeShiftRepository = FakeShiftRepository()

        registerTripUseCase = RegisterTripUseCase(fakeTripRepository)
        registerFuelingUseCase = RegisterFuelingUseCase(fakeFuelingRepository)
        registerExpenseUseCase = RegisterExpenseUseCase(fakeExpenseRepository)
        manageShiftUseCase = ManageShiftUseCase(fakeShiftRepository)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 1: PRORRATEO SEMANAL Y CÁLCULO DE DÍAS RESTANTES (CORTE MARTES 23:59)
    // --------------------------------------------------------------------------
    @Test
    fun testDaysRemainingCalculationToTuesday() {
        assertEquals(7, calculateDailyQuotaUseCase.calculateDaysRemainingToTuesday(DayOfWeek.WEDNESDAY))
        assertEquals(6, calculateDailyQuotaUseCase.calculateDaysRemainingToTuesday(DayOfWeek.THURSDAY))
        assertEquals(5, calculateDailyQuotaUseCase.calculateDaysRemainingToTuesday(DayOfWeek.FRIDAY))
        assertEquals(4, calculateDailyQuotaUseCase.calculateDaysRemainingToTuesday(DayOfWeek.SATURDAY))
        assertEquals(3, calculateDailyQuotaUseCase.calculateDaysRemainingToTuesday(DayOfWeek.SUNDAY))
        assertEquals(2, calculateDailyQuotaUseCase.calculateDaysRemainingToTuesday(DayOfWeek.MONDAY))
        assertEquals(1, calculateDailyQuotaUseCase.calculateDaysRemainingToTuesday(DayOfWeek.TUESDAY))
    }

    @Test
    fun testWeeklyQuotaProrationCalculation() {
        val bogotaZone = ZoneId.of("America/Bogota")
        // Domingo: 3 días restantes al martes 23:59
        val sundayTimestamp = ZonedDateTime.of(2026, 9, 6, 10, 0, 0, 0, bogotaZone)
            .toInstant().toEpochMilli()

        val progress = calculateDailyQuotaUseCase(
            weeklyTarget = 500_000.0,
            accumulatedSoFar = 200_000.0,
            nowMillis = sundayTimestamp,
            zoneId = bogotaZone
        )

        assertEquals(500_000.0, progress.weeklyTarget, 0.001)
        assertEquals(200_000.0, progress.accumulatedSoFar, 0.001)
        assertEquals(300_000.0, progress.remainingTarget, 0.001)
        assertEquals(3, progress.daysRemaining)
        // 300.000 / 3 días = 100.000 COP por día sugerido
        assertEquals(100_000.0, progress.suggestedDailyQuota, 0.001)
        assertEquals(40.0, progress.progressPercentage, 0.001)
        assertFalse(progress.isWeeklyTargetAchieved)
    }

    @Test
    fun testWeeklyQuotaAchievedZeroRemaining() {
        val progress = calculateDailyQuotaUseCase(
            weeklyTarget = 500_000.0,
            accumulatedSoFar = 550_000.0
        )
        assertEquals(0.0, progress.remainingTarget, 0.001)
        assertEquals(0.0, progress.suggestedDailyQuota, 0.001)
        assertTrue(progress.isWeeklyTargetAchieved)
        assertEquals(100.0, progress.progressPercentage, 0.001)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: TRANSICIONES DEL SEMÁFORO FINANCIERO (ROJO, ÁMBAR, VERDE)
    // --------------------------------------------------------------------------
    @Test
    fun testFinancialStatusTransitions() {
        val targetQuota = 100_000.0

        // Caso 1: Déficit operativo o cobertura < 85% -> ROJO
        val redSummary = createSummary(netProfit = 50_000.0, quota = targetQuota)
        val redStatus = getShiftFinancialStatusUseCase(redSummary, targetQuota)
        assertTrue("Debe ser Red cuando ganancia < 85% de la cuota", redStatus is FinancialStatus.Red)
        assertEquals(50_000.0, redStatus.remainingAmount, 0.001)

        // Caso 2: Cerca de la meta (85% a 99%) -> ÁMBAR
        val amberSummary85 = createSummary(netProfit = 85_000.0, quota = targetQuota)
        val amberStatus = getShiftFinancialStatusUseCase(amberSummary85, targetQuota)
        assertTrue("Debe ser Amber en umbral exacto del 85%", amberStatus is FinancialStatus.Amber)
        assertEquals(15_000.0, amberStatus.remainingAmount, 0.001)

        val amberSummary95 = createSummary(netProfit = 95_000.0, quota = targetQuota)
        val amberStatus95 = getShiftFinancialStatusUseCase(amberSummary95, targetQuota)
        assertTrue("Debe ser Amber cuando ganancia está al 95%", amberStatus95 is FinancialStatus.Amber)
        assertEquals(5_000.0, amberStatus95.remainingAmount, 0.001)

        // Caso 3: Meta cubierta exactamente o superada (>= 100%) -> VERDE (Turno Libre)
        val greenSummary100 = createSummary(netProfit = 100_000.0, quota = targetQuota)
        val greenStatus100 = getShiftFinancialStatusUseCase(greenSummary100, targetQuota)
        assertTrue("Debe ser Green al 100%", greenStatus100 is FinancialStatus.Green)
        assertEquals(0.0, greenStatus100.remainingAmount, 0.001)
        assertEquals(0.0, greenStatus100.surplusAmount, 0.001)

        val greenSummary130 = createSummary(netProfit = 130_000.0, quota = targetQuota)
        val greenStatus130 = getShiftFinancialStatusUseCase(greenSummary130, targetQuota)
        assertTrue("Debe ser Green superando meta", greenStatus130 is FinancialStatus.Green)
        assertEquals(0.0, greenStatus130.remainingAmount, 0.001)
        assertEquals(30_000.0, greenStatus130.surplusAmount, 0.001)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: VALIDACIÓN DE DOMAIN GUARDS (CARRERAS, TANQUEOS, GASTOS, TURNOS)
    // --------------------------------------------------------------------------
    @Test
    fun testRegisterTripDomainGuards() = runBlocking {
        // Tarifa negativa rechazada
        try {
            registerTripUseCase("shift-1", PlatformType.UBER, -100.0, 0.0, PaymentMethod.CASH)
            fail("Debe rechazar tarifa bruta negativa")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("mayor o igual a cero"))
        }

        // Comisión negativa rechazada
        try {
            registerTripUseCase("shift-1", PlatformType.UBER, 20_000.0, -500.0, PaymentMethod.CASH)
            fail("Debe rechazar comisión negativa")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("mayor o igual a cero"))
        }

        // Comisión mayor que tarifa bruta rechazada
        try {
            registerTripUseCase("shift-1", PlatformType.UBER, 20_000.0, 25_000.0, PaymentMethod.CASH)
            fail("Debe rechazar comisión superior a la tarifa bruta")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("no puede exceder"))
        }

        // Carrera válida aceptada
        val validTrip = registerTripUseCase("shift-1", PlatformType.UBER, 20_000.0, 5_000.0, PaymentMethod.CASH)
        assertEquals(15_000.0, validTrip.netAmount, 0.001)
        assertEquals(1, fakeTripRepository.savedTrips.size)
    }

    @Test
    fun testRegisterFuelingDomainGuards() = runBlocking {
        // Monto pagado <= 0 rechazado
        try {
            registerFuelingUseCase("shift-1", FuelType.GAS, 0.0, 10.0, 100_000)
            fail("Debe rechazar monto pagado <= 0")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("mayor a cero"))
        }

        // Volumen <= 0 rechazado
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { registerFuelingUseCase("shift-1", FuelType.GAS, 25_000.0, 0.0, 100_000) }
        }
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { registerFuelingUseCase("shift-1", FuelType.GAS, 25_000.0, 10.0, -1) }
        }
    }

    @Test
    fun testRegisterExpenseDomainGuards() = runBlocking {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { registerExpenseUseCase("shift-1", ExpenseConcept.WASH, 0.0) }
        }
    }

    @Test
    fun testManageShiftDomainGuards() = runBlocking {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { manageShiftUseCase.startShift(startOdometer = -5) }
        }
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { manageShiftUseCase.closeShift("shift-1", startOdometer = 100_500, endOdometer = 100_400) }
        }
    }

    @Test
    fun testZeroKilometersDivisionByZeroProtection() {
        val metrics = calculateBiFuelCostPerKmUseCase(
            totalKilometers = 0,
            gasCostTotal = 30_000.0,
            gasolineCostTotal = 40_000.0
        )

        assertEquals(0, metrics.totalKilometers)
        assertEquals(70_000.0, metrics.totalFuelCost, 0.001)
        // No debe arrojar ArithmeticException ni NaN, debe retornar 0.0
        assertEquals(0.0, metrics.gasCostPerKm, 0.001)
        assertEquals(0.0, metrics.gasolineCostPerKm, 0.001)
        assertEquals(0.0, metrics.blendedCostPerKm, 0.001)
        assertFalse(metrics.blendedCostPerKm.isNaN())
        assertFalse(metrics.blendedCostPerKm.isInfinite())
    }

    @Test
    fun testValidKilometersBiFuelCostCalculation() {
        val metrics = calculateBiFuelCostPerKmUseCase(
            totalKilometers = 100,
            gasCostTotal = 20_000.0,
            gasolineCostTotal = 15_000.0
        )

        assertEquals(100, metrics.totalKilometers)
        assertEquals(35_000.0, metrics.totalFuelCost, 0.001)
        assertEquals(200.0, metrics.gasCostPerKm, 0.001)
        assertEquals(150.0, metrics.gasolineCostPerKm, 0.001)
        assertEquals(350.0, metrics.blendedCostPerKm, 0.001)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 5: CONSTRUCTOR AUXILIAR DE RESÚMENES FINANCIEROS
    // --------------------------------------------------------------------------
    private fun createSummary(netProfit: Double, quota: Double): ShiftFinancialSummary {
        return ShiftFinancialSummary(
            shiftId = "shift-test-1", status = ShiftStatus.ACTIVE,
            startTime = System.currentTimeMillis(), endTime = null,
            startOdometer = 100_000, endOdometer = null, totalKilometers = 50,
            dailyQuotaTarget = quota, totalGrossIncome = netProfit + 20_000.0,
            totalCommissions = 5_000.0, totalNetIncome = netProfit + 15_000.0,
            totalGasExpense = 15_000.0, totalGasolineExpense = 0.0,
            totalFuelExpense = 15_000.0, totalOperationalExpenses = 0.0,
            totalExpenses = 15_000.0, netProfit = netProfit,
            quotaProgressPercentage = (netProfit / quota) * 100.0,
            costPerKilometer = 300.0, grossIncomePerKilometer = 1000.0,
            netIncomePerKilometer = 800.0, tripsCount = 5,
            fuelingsCount = 1, expensesCount = 0
        )
    }
}
