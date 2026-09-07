// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/HistoryAnalyticsUseCasesTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias exhaustiva para los casos de uso analíticos de la Orden #005.
// Valida:
// 1. Agregación contable por plataforma y cálculo exacto de márgenes de retención.
// 2. Auditoría matemática de ahorro bi-combustible (GNV vs. Gasolina equivalente).
// 3. Protección matemática contra división por cero con colecciones vacías y odómetro detenido.
// 4. Integridad reactiva del caso de uso de histórico de turnos completados.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.ShiftStatus
import com.ananapps.kilometa.domain.model.Trip
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HistoryAnalyticsUseCasesTest {

    private lateinit var getPlatformAnalyticsUseCase: GetPlatformAnalyticsUseCase
    private lateinit var getBiFuelSavingsUseCase: GetBiFuelSavingsUseCase
    private lateinit var getShiftHistoryUseCase: GetShiftHistoryUseCase
    private lateinit var fakeShiftRepository: FakeShiftRepository

    @Before
    fun setUp() {
        getPlatformAnalyticsUseCase = GetPlatformAnalyticsUseCase()
        getBiFuelSavingsUseCase = GetBiFuelSavingsUseCase()
        fakeShiftRepository = FakeShiftRepository()
        getShiftHistoryUseCase = GetShiftHistoryUseCase(fakeShiftRepository)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 1: AGREGACIÓN POR PLATAFORMA Y CÁLCULO DE MARGEN DE RETENCIÓN
    // --------------------------------------------------------------------------
    @Test
    fun testPlatformAnalyticsAggregationAndRetentionMargins() {
        val testTrips = listOf(
            // Uber: Bruto $50.000, Comisión $12.500 (25%), Neto $37.500
            createTrip("t1", PlatformType.UBER, gross = 50_000.0, commission = 12_500.0),
            // Uber: Bruto $30.000, Comisión $7.500 (25%), Neto $22.500
            createTrip("t2", PlatformType.UBER, gross = 30_000.0, commission = 7_500.0),
            // DiDi: Bruto $40.000, Comisión $8.000 (20%), Neto $32.000
            createTrip("t3", PlatformType.DIDI, gross = 40_000.0, commission = 8_000.0),
            // InDrive: Bruto $30.000, Comisión $3.000 (10%), Neto $27.000
            createTrip("t4", PlatformType.INDRIVE, gross = 30_000.0, commission = 3_000.0),
            // Particular: Bruto $50.000, Comisión $0 (0%), Neto $50.000
            createTrip("t5", PlatformType.PRIVATE, gross = 50_000.0, commission = 0.0)
        )

        val analytics = getPlatformAnalyticsUseCase(testTrips)
        val totalGross = 200_000.0

        // Verificación Uber
        val uber = analytics.first { it.platformType == PlatformType.UBER }
        assertEquals(80_000.0, uber.grossIncome, 0.001)
        assertEquals(20_000.0, uber.commissionPaid, 0.001)
        assertEquals(60_000.0, uber.netIncome, 0.001)
        assertEquals(2, uber.tripsCount)
        assertEquals(40_000.0, uber.averageTicket, 0.001)
        assertEquals(75.0, uber.netMarginPercentage, 0.001) // Conserva el 75%
        assertEquals(25.0, uber.commissionRatePercentage, 0.001) // Comisión del 25%
        assertEquals((80_000.0 / totalGross) * 100.0, uber.revenueSharePercentage, 0.001)

        // Verificación DiDi
        val didi = analytics.first { it.platformType == PlatformType.DIDI }
        assertEquals(40_000.0, didi.grossIncome, 0.001)
        assertEquals(8_000.0, didi.commissionPaid, 0.001)
        assertEquals(32_000.0, didi.netIncome, 0.001)
        assertEquals(1, didi.tripsCount)
        assertEquals(80.0, didi.netMarginPercentage, 0.001)
        assertEquals(20.0, didi.commissionRatePercentage, 0.001)

        // Verificación InDrive
        val indrive = analytics.first { it.platformType == PlatformType.INDRIVE }
        assertEquals(30_000.0, indrive.grossIncome, 0.001)
        assertEquals(3_000.0, indrive.commissionPaid, 0.001)
        assertEquals(27_000.0, indrive.netIncome, 0.001)
        assertEquals(90.0, indrive.netMarginPercentage, 0.001)
        assertEquals(10.0, indrive.commissionRatePercentage, 0.001)

        // Verificación Particular (cero intermediación)
        val private = analytics.first { it.platformType == PlatformType.PRIVATE }
        assertEquals(50_000.0, private.grossIncome, 0.001)
        assertEquals(0.0, private.commissionPaid, 0.001)
        assertEquals(50_000.0, private.netIncome, 0.001)
        assertEquals(100.0, private.netMarginPercentage, 0.001)
        assertEquals(0.0, private.commissionRatePercentage, 0.001)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: CÁLCULO EXACTO DE AHORRO BI-COMBUSTIBLE (GNV VS GASOLINA)
    // --------------------------------------------------------------------------
    @Test
    fun testExactBiFuelSavingsCalculation() {
        // Escenario: 350 km rodados, $60.000 COP invertidos en GNV, $0 en gasolina
        // Rendimiento referencia: 35 km/galón, Precio referencia: $15.800 COP/galón
        // Equivalente en gasolina: (350 / 35) * 15.800 = 10 galones * 15.800 = 158.000 COP
        // Ahorro neto: 158.000 - 60.000 = 98.000 COP
        // % de Ahorro: (98.000 / 158.000) * 100 = 62.0253%
        val metrics = getBiFuelSavingsUseCase(
            totalKmDriven = 350,
            totalGasExpense = 60_000.0,
            totalGasolineExpense = 0.0
        )

        assertEquals(350, metrics.totalKmDriven)
        assertEquals(60_000.0, metrics.totalGasExpense, 0.001)
        assertEquals(0.0, metrics.totalGasolineExpense, 0.001)
        assertEquals(60_000.0, metrics.actualFuelExpense, 0.001)
        assertEquals(158_000.0, metrics.equivalentGasolineExpense, 0.001)
        assertEquals(98_000.0, metrics.netSavingsAmount, 0.001)
        assertEquals((98_000.0 / 158_000.0) * 100.0, metrics.savingsPercentage, 0.001)
        assertEquals(60_000.0 / 350.0, metrics.costPerKmGas, 0.001)
        assertEquals(0.0, metrics.costPerKmGasoline, 0.001)
        assertTrue(metrics.hasPositiveSavings)
    }

    @Test
    fun testBiFuelSavingsWithGasolineSupport() {
        // Escenario mixto: 140 km rodados, $20.000 en gas + $10.000 en gasolina
        // Equivalente gasolina: (140 / 35) * 15.800 = 4 galones * 15.800 = 63.200 COP
        // Gasto real: 20.000 + 10.000 = 30.000 COP
        // Ahorro neto: 63.200 - 30.000 = 33.200 COP
        val metrics = getBiFuelSavingsUseCase(
            totalKmDriven = 140,
            totalGasExpense = 20_000.0,
            totalGasolineExpense = 10_000.0
        )

        assertEquals(63_200.0, metrics.equivalentGasolineExpense, 0.001)
        assertEquals(30_000.0, metrics.actualFuelExpense, 0.001)
        assertEquals(33_200.0, metrics.netSavingsAmount, 0.001)
        assertEquals((33_200.0 / 63_200.0) * 100.0, metrics.savingsPercentage, 0.001)
        assertEquals(20_000.0 / 140.0, metrics.costPerKmGas, 0.001)
        assertEquals(10_000.0 / 140.0, metrics.costPerKmGasoline, 0.001)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: PROTECCIÓN MATEMÁTICA CONTRA DIVISIÓN POR CERO
    // --------------------------------------------------------------------------
    @Test
    fun testPlatformAnalyticsEmptyCollectionDivisionByZero() {
        val analytics = getPlatformAnalyticsUseCase(emptyList())

        assertEquals(PlatformType.entries.size, analytics.size)
        for (item in analytics) {
            assertEquals(0.0, item.grossIncome, 0.001)
            assertEquals(0.0, item.commissionPaid, 0.001)
            assertEquals(0.0, item.netIncome, 0.001)
            assertEquals(0, item.tripsCount)
            assertEquals(0.0, item.averageTicket, 0.001)
            assertEquals(0.0, item.netMarginPercentage, 0.001)
            assertEquals(0.0, item.revenueSharePercentage, 0.001)
            assertFalse(item.averageTicket.isNaN())
            assertFalse(item.netMarginPercentage.isNaN())
            assertFalse(item.revenueSharePercentage.isNaN())
        }
    }

    @Test
    fun testBiFuelSavingsZeroKilometersDivisionByZero() {
        val metrics = getBiFuelSavingsUseCase(
            totalKmDriven = 0,
            totalGasExpense = 15_000.0,
            totalGasolineExpense = 5_000.0
        )

        assertEquals(0, metrics.totalKmDriven)
        assertEquals(20_000.0, metrics.actualFuelExpense, 0.001)
        assertEquals(0.0, metrics.equivalentGasolineExpense, 0.001)
        assertEquals(0.0, metrics.netSavingsAmount, 0.001)
        assertEquals(0.0, metrics.savingsPercentage, 0.001)
        assertEquals(0.0, metrics.costPerKmGas, 0.001)
        assertEquals(0.0, metrics.costPerKmGasoline, 0.001)
        assertEquals(0.0, metrics.blendedCostPerKm, 0.001)
        assertFalse(metrics.savingsPercentage.isNaN())
        assertFalse(metrics.savingsPercentage.isInfinite())
        assertFalse(metrics.blendedCostPerKm.isNaN())
    }

    // --------------------------------------------------------------------------
    // BLOQUE 4: INTEGRIDAD REACTIVA DEL HISTORIAL DE TURNOS
    // --------------------------------------------------------------------------
    @Test
    fun testShiftHistoryFlowEmission() = runBlocking {
        val summary1 = createCompletedSummary("shift-1", netProfit = 120_000.0, km = 150)
        val summary2 = createCompletedSummary("shift-2", netProfit = 90_000.0, km = 110)
        fakeShiftRepository.completedSummaries.addAll(listOf(summary1, summary2))

        val result = getShiftHistoryUseCase().first()

        assertEquals(2, result.size)
        assertEquals("shift-1", result[0].shiftId)
        assertEquals(120_000.0, result[0].netProfit, 0.001)
        assertEquals(150, result[0].totalKilometers)
        assertEquals("shift-2", result[1].shiftId)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 5: CONSTRUCTORES AUXILIARES
    // --------------------------------------------------------------------------
    private fun createTrip(
        id: String,
        platformType: PlatformType,
        gross: Double,
        commission: Double
    ): Trip {
        return Trip(
            id = id,
            shiftId = "test-shift",
            platformType = platformType,
            grossAmount = gross,
            commissionAmount = commission,
            netAmount = gross - commission,
            paymentMethod = PaymentMethod.CASH,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun createCompletedSummary(
        shiftId: String,
        netProfit: Double,
        km: Int
    ): ShiftFinancialSummary {
        return ShiftFinancialSummary(
            shiftId = shiftId,
            status = ShiftStatus.COMPLETED,
            startTime = System.currentTimeMillis() - 36_000_000L,
            endTime = System.currentTimeMillis(),
            startOdometer = 100_000,
            endOdometer = 100_000 + km,
            totalKilometers = km,
            dailyQuotaTarget = 100_000.0,
            totalGrossIncome = netProfit + 30_000.0,
            totalCommissions = 5_000.0,
            totalNetIncome = netProfit + 25_000.0,
            totalGasExpense = 25_000.0,
            totalGasolineExpense = 0.0,
            totalFuelExpense = 25_000.0,
            totalOperationalExpenses = 0.0,
            totalExpenses = 25_000.0,
            netProfit = netProfit,
            quotaProgressPercentage = (netProfit / 100_000.0) * 100.0,
            costPerKilometer = if (km > 0) 25_000.0 / km else 0.0,
            grossIncomePerKilometer = if (km > 0) (netProfit + 30_000.0) / km else 0.0,
            netIncomePerKilometer = if (km > 0) (netProfit + 25_000.0) / km else 0.0,
            tripsCount = 8,
            fuelingsCount = 1,
            expensesCount = 0
        )
    }
}
