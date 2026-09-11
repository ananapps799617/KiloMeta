// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/GetAccountingAuditDashboardUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para GetAccountingAuditDashboardUseCase.
// Valida el cálculo determinista del ciclo semanal (Miércoles a Martes 23:59:59),
// el filtrado topológico de jornadas y la consolidación de KPIs contables de cabina.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.ConnectionDiagnostic
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.RestoreProgress
import com.ananapps.kilometa.domain.model.Shift
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.ShiftStatus
import com.ananapps.kilometa.domain.model.SyncState
import com.ananapps.kilometa.domain.model.SyncStatus
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.domain.model.DriverProfile
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

// ------------------------------------------------------------------------------
// BLOQUE 1: DOUBLES DE PRUEBA PARA REPOSITORIOS
// ------------------------------------------------------------------------------
private class TestShiftRepository : ShiftRepository {
    val completedHistoryFlow = MutableStateFlow<List<ShiftFinancialSummary>>(emptyList())

    override fun getActiveShift(): Flow<Shift?> = emptyFlow()
    override fun getActiveShiftFinancialSummary(): Flow<ShiftFinancialSummary?> = emptyFlow()
    override fun getAllShifts(): Flow<List<Shift>> = emptyFlow()
    override fun getCompletedShiftsHistory(): Flow<List<ShiftFinancialSummary>> = completedHistoryFlow
    override fun getAllTrips(): Flow<List<Trip>> = emptyFlow()
    override fun getAllFuelings(): Flow<List<Fueling>> = emptyFlow()
    override suspend fun startShift(shift: Shift) {}; override suspend fun closeShift(shiftId: String, endOdometer: Int, endTime: Long, notes: String?) {}
    override suspend fun updateShift(shift: Shift) {}; override suspend fun deleteShift(shiftId: String) {}
    override fun getActiveShiftCashSettlement(): Flow<com.ananapps.kilometa.domain.model.CashSettlementSummary?> = flowOf(null); override fun getActiveShiftTrips(): Flow<List<Trip>> = emptyFlow(); override fun getLastKnownOdometer(): Flow<Int?> = flowOf(null)
    override suspend fun getShiftsForDay(dayStart: Long, dayEnd: Long): List<Shift> = emptyList(); override suspend fun consolidateShifts(masterShift: Shift, secondaryShiftIds: List<String>) {}
}

private class TestExpenseRepository : ExpenseRepository {
    override fun getExpensesByShift(shiftId: String): Flow<List<Expense>> = emptyFlow()
    override suspend fun addExpense(expense: Expense) {}
    override suspend fun deleteExpense(expenseId: String) {}
    override fun getAllExpenses(): Flow<List<Expense>> = flowOf(emptyList())
}

private class TestSyncRepository : SyncRepository {
    val syncStatusFlow = MutableStateFlow(SyncStatus(SyncState.SUCCESS, lastSyncTimestamp = 1700000000000L))

    override fun observeSyncStatus(): Flow<SyncStatus> = syncStatusFlow
    override suspend fun pushPendingChanges(): Result<Unit> = Result.success(Unit)
    override suspend fun pullRemoteChanges(): Result<Unit> = Result.success(Unit)
    override fun fullDisasterRecovery(): Flow<RestoreProgress> = emptyFlow()
    override suspend fun checkCloudConnection(): ConnectionDiagnostic = ConnectionDiagnostic(isConnected = true, latencyMs = 50L, message = "OK")
    override suspend fun clearAllLocalData(): Result<Unit> = Result.success(Unit)
}

private class TestDriverProfileRepository : DriverProfileRepository {
    val vehicleProfileFlow = MutableStateFlow(VehicleProfile(quotaCutoffDay = 2, quotaCutoffHour = 23, quotaAmount = 500000.0))
    override fun getProfile(): Flow<DriverProfile> = flowOf(DriverProfile())
    override suspend fun saveProfile(profile: DriverProfile) {}
    override fun getVehicleProfile(): Flow<VehicleProfile> = vehicleProfileFlow
    override suspend fun saveVehicleProfile(vehicle: VehicleProfile) { vehicleProfileFlow.value = vehicle }
    override suspend fun saveFullProfile(driver: DriverProfile, vehicle: VehicleProfile) {}
    override suspend fun resetProfileForTesting() {}
    override suspend fun clearProfile() {}
    override suspend fun fetchAndHydrateRemoteProfile(userId: String): Result<Boolean> = Result.success(true)
}

// ------------------------------------------------------------------------------
// BLOQUE 2: SUITE DE PRUEBAS DE AUDITORÍA CONTABLE
// ------------------------------------------------------------------------------
class GetAccountingAuditDashboardUseCaseTest {

    private lateinit var shiftRepository: TestShiftRepository
    private lateinit var expenseRepository: TestExpenseRepository
    private lateinit var syncRepository: TestSyncRepository
    private lateinit var driverProfileRepository: TestDriverProfileRepository
    private lateinit var calculateDailyQuotaUseCase: CalculateDailyQuotaUseCase
    private lateinit var useCase: GetAccountingAuditDashboardUseCase

    private val bogotaZone = ZoneId.of("America/Bogota")

    @Before
    fun setUp() {
        shiftRepository = TestShiftRepository()
        expenseRepository = TestExpenseRepository()
        syncRepository = TestSyncRepository()
        driverProfileRepository = TestDriverProfileRepository()
        calculateDailyQuotaUseCase = CalculateDailyQuotaUseCase()
        useCase = GetAccountingAuditDashboardUseCase(
            shiftRepository = shiftRepository,
            expenseRepository = expenseRepository,
            calculateDailyQuotaUseCase = calculateDailyQuotaUseCase,
            syncRepository = syncRepository,
            driverProfileRepository = driverProfileRepository
        )
    }

    @Test
    fun `calculateWeeklyCycleBounds establece ciclo de Miercoles 00_00 a Martes 23_59_59`() {
        // GIVEN: Domingo 2026-09-06 a las 14:30:00 en Bogotá
        val testSunday = ZonedDateTime.of(2026, 9, 6, 14, 30, 0, 0, bogotaZone)
        val testMillis = testSunday.toInstant().toEpochMilli()

        // WHEN: Se calculan los bordes del ciclo
        val (startMillis, endMillis) = useCase.calculateWeeklyCycleBounds(testMillis, bogotaZone)

        // THEN: El inicio fue el Miércoles 2026-09-02 a las 00:00:00
        val startDate = Instant.ofEpochMilli(startMillis).atZone(bogotaZone)
        assertEquals(DayOfWeek.WEDNESDAY, startDate.dayOfWeek)
        assertEquals(2, startDate.dayOfMonth)
        assertEquals(0, startDate.hour)
        assertEquals(0, startDate.minute)

        // AND: El fin es el Martes 2026-09-08 a las 23:59:59
        val endDate = Instant.ofEpochMilli(endMillis).atZone(bogotaZone)
        assertEquals(DayOfWeek.TUESDAY, endDate.dayOfWeek)
        assertEquals(8, endDate.dayOfMonth)
        assertEquals(23, endDate.hour)
        assertEquals(59, endDate.minute)
    }

    @Test
    fun `calculateWeeklyCycleBounds con corte personalizado Martes 18_00`() {
        // GIVEN: Domingo 2026-09-06 a las 14:30:00 en Bogotá con corte Martes 18:00
        val testSunday = ZonedDateTime.of(2026, 9, 6, 14, 30, 0, 0, bogotaZone)
        val testMillis = testSunday.toInstant().toEpochMilli()

        // WHEN: Se calculan los bordes con cutoffDay = TUESDAY y cutoffHour = 18
        val (startMillis, endMillis) = useCase.calculateWeeklyCycleBounds(
            nowMillis = testMillis,
            zoneId = bogotaZone,
            cutoffDay = DayOfWeek.TUESDAY,
            cutoffHour = 18
        )

        // THEN: Fin = Martes 2026-09-08 a las 18:00:00 y Inicio = Martes 2026-09-01 a las 18:00:00
        val endDate = Instant.ofEpochMilli(endMillis).atZone(bogotaZone)
        assertEquals(DayOfWeek.TUESDAY, endDate.dayOfWeek)
        assertEquals(8, endDate.dayOfMonth)
        assertEquals(18, endDate.hour)

        val startDate = Instant.ofEpochMilli(startMillis).atZone(bogotaZone)
        assertEquals(DayOfWeek.TUESDAY, startDate.dayOfWeek)
        assertEquals(1, startDate.dayOfMonth)
        assertEquals(18, startDate.hour)
    }

    @Test
    fun `invoke acumula exclusivamente jornadas completadas dentro del ciclo contable actual`() = runBlocking {
        // GIVEN: Miércoles 2026-09-02 a las 10:00:00
        val nowDateTime = ZonedDateTime.of(2026, 9, 2, 10, 0, 0, 0, bogotaZone)
        val nowMillis = nowDateTime.toInstant().toEpochMilli()

        // Turno 1: Dentro del ciclo actual (Jueves 2026-09-03)
        val shiftInCycle = createDummySummary(
            shiftId = "shift-current",
            startTime = ZonedDateTime.of(2026, 9, 3, 8, 0, 0, 0, bogotaZone).toInstant().toEpochMilli(),
            grossIncome = 200_000.0,
            commissions = 40_000.0,
            netIncome = 160_000.0,
            gasExpense = 25_000.0,
            gasolineExpense = 10_000.0,
            operational = 5_000.0,
            kilometers = 100
        )

        // Turno 2: Fuera del ciclo (Martes de la semana anterior: 2026-08-25)
        val shiftPastCycle = createDummySummary(
            shiftId = "shift-past",
            startTime = ZonedDateTime.of(2026, 8, 25, 8, 0, 0, 0, bogotaZone).toInstant().toEpochMilli(),
            grossIncome = 300_000.0,
            commissions = 60_000.0,
            netIncome = 240_000.0,
            gasExpense = 40_000.0,
            gasolineExpense = 0.0,
            operational = 10_000.0,
            kilometers = 150
        )

        shiftRepository.completedHistoryFlow.value = listOf(shiftInCycle, shiftPastCycle)

        // WHEN: Se obtiene la auditoría
        val summary = useCase(nowMillis = nowMillis, zoneId = bogotaZone).first()

        // THEN: Solo se consolidaron los valores del turno dentro del ciclo
        assertEquals(200_000.0, summary.totalGrossIncome, 0.001)
        assertEquals(160_000.0, summary.totalNetIncomeInHand, 0.001)
        assertEquals(40_000.0, summary.totalCommissions, 0.001)
        assertEquals(100, summary.totalKilometers)
        assertEquals(25_000.0, summary.totalGasExpense, 0.001)
        assertEquals(10_000.0, summary.totalGasolineExpense, 0.001)
        assertEquals(5_000.0, summary.totalOperationalExpenses, 0.001)

        // Net profit = 160_000 - 35_000 - 5_000 = 120_000
        assertEquals(120_000.0, summary.netProfit, 0.001)

        // Costo por km = 40_000 / 100 = 400 COP/km
        assertEquals(400.0, summary.costPerKilometer, 0.001)

        // Ingreso por km = 200_000 / 100 = 2000 COP/km
        assertEquals(2000.0, summary.revenuePerKilometer, 0.001)

        // Ratio combustible = 35_000 / 200_000 = 17.5% <= 25% (Óptimo)
        assertEquals(17.5, summary.fuelExpenseToIncomeRatio, 0.001)
        assertTrue(summary.isFuelEfficiencyOptimal)
        assertTrue(summary.isCloudSyncHealthy)
    }

    @Test
    fun `isFuelEfficiencyOptimal retorna false si el gasto de combustible supera el 25 por ciento`() = runBlocking {
        val nowMillis = ZonedDateTime.of(2026, 9, 2, 10, 0, 0, 0, bogotaZone).toInstant().toEpochMilli()

        // Turno con 30% de gasto en combustible (60.000 / 200.000)
        val highFuelShift = createDummySummary(
            shiftId = "shift-high-fuel",
            startTime = nowMillis,
            grossIncome = 200_000.0,
            commissions = 40_000.0,
            netIncome = 160_000.0,
            gasExpense = 50_000.0,
            gasolineExpense = 10_000.0,
            operational = 5_000.0,
            kilometers = 100
        )
        shiftRepository.completedHistoryFlow.value = listOf(highFuelShift)

        val summary = useCase(nowMillis = nowMillis, zoneId = bogotaZone).first()

        assertEquals(30.0, summary.fuelExpenseToIncomeRatio, 0.001)
        assertFalse(summary.isFuelEfficiencyOptimal)
    }

    private fun createDummySummary(
        shiftId: String, startTime: Long, grossIncome: Double, commissions: Double,
        netIncome: Double, gasExpense: Double, gasolineExpense: Double, operational: Double, kilometers: Int
    ): ShiftFinancialSummary {
        val totalFuel = gasExpense + gasolineExpense
        val totalExpenses = totalFuel + operational
        val netProfit = netIncome - totalExpenses

        return ShiftFinancialSummary(
            shiftId = shiftId, status = ShiftStatus.COMPLETED, startTime = startTime,
            endTime = startTime + 36_000_000L, startOdometer = 100_000, endOdometer = 100_000 + kilometers,
            totalKilometers = kilometers, dailyQuotaTarget = 83_333.0, totalGrossIncome = grossIncome,
            totalCommissions = commissions, totalNetIncome = netIncome, totalGasExpense = gasExpense,
            totalGasolineExpense = gasolineExpense, totalFuelExpense = totalFuel,
            totalOperationalExpenses = operational, totalExpenses = totalExpenses, netProfit = netProfit,
            quotaProgressPercentage = 100.0, costPerKilometer = if (kilometers > 0) totalExpenses / kilometers else 0.0,
            grossIncomePerKilometer = if (kilometers > 0) grossIncome / kilometers else 0.0,
            netIncomePerKilometer = if (kilometers > 0) netIncome / kilometers else 0.0,
            tripsCount = 5, fuelingsCount = 1, expensesCount = 1
        )
    }
}
