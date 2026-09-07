// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/ExportUseCasesTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para los casos de uso de exportación de la Orden #006.
// Valida la compilación tabular multi-tabla con BOM UTF-8 y la estructura enriquecida
// con emojis para mensajería WhatsApp/Telegram.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.HistoryFilterPeriod
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.ShiftStatus
import com.ananapps.kilometa.domain.model.Trip
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExportUseCasesTest {

    private lateinit var shiftRepository: FakeShiftRepository
    private lateinit var expenseRepository: FakeExpenseRepository
    private lateinit var biFuelSavingsUseCase: GetBiFuelSavingsUseCase
    private lateinit var generateCsvReportUseCase: GenerateCsvReportUseCase
    private lateinit var generateShareableSummaryUseCase: GenerateShareableSummaryUseCase

    private val sampleShiftId = "shift-abc-123"

    private val sampleSummary = ShiftFinancialSummary(
        shiftId = sampleShiftId,
        status = ShiftStatus.COMPLETED,
        startTime = 1700000000000L,
        endTime = 1700036000000L,
        startOdometer = 120000,
        endOdometer = 120150,
        totalKilometers = 150,
        dailyQuotaTarget = 83333.0,
        totalGrossIncome = 220000.0,
        totalCommissions = 44000.0,
        totalNetIncome = 176000.0,
        totalGasExpense = 25000.0,
        totalGasolineExpense = 0.0,
        totalFuelExpense = 25000.0,
        totalOperationalExpenses = 6000.0,
        totalExpenses = 31000.0,
        netProfit = 145000.0,
        quotaProgressPercentage = 174.0,
        costPerKilometer = 206.67,
        grossIncomePerKilometer = 1466.67,
        netIncomePerKilometer = 1173.33,
        tripsCount = 2,
        fuelingsCount = 1,
        expensesCount = 1
    )

    @Before
    fun setUp() {
        shiftRepository = FakeShiftRepository()
        expenseRepository = FakeExpenseRepository()
        biFuelSavingsUseCase = GetBiFuelSavingsUseCase()

        generateCsvReportUseCase = GenerateCsvReportUseCase(
            shiftRepository = shiftRepository,
            expenseRepository = expenseRepository
        )

        generateShareableSummaryUseCase = GenerateShareableSummaryUseCase(
            getBiFuelSavingsUseCase = biFuelSavingsUseCase
        )

        // Cargar datos simulados en repositorios
        shiftRepository.allTripsList.addAll(
            listOf(
                Trip(
                    id = "trip-1",
                    shiftId = sampleShiftId,
                    platformType = PlatformType.UBER,
                    grossAmount = 120000.0,
                    commissionAmount = 24000.0,
                    netAmount = 96000.0,
                    paymentMethod = PaymentMethod.CASH,
                    timestamp = 1700010000000L
                ),
                Trip(
                    id = "trip-2",
                    shiftId = sampleShiftId,
                    platformType = PlatformType.DIDI,
                    grossAmount = 100000.0,
                    commissionAmount = 20000.0,
                    netAmount = 80000.0,
                    paymentMethod = PaymentMethod.NEQUI,
                    timestamp = 1700020000000L
                )
            )
        )

        shiftRepository.allFuelingsList.add(
            Fueling(
                id = "fuel-1",
                shiftId = sampleShiftId,
                fuelType = FuelType.GAS,
                amountPaid = 25000.0,
                volume = 11.5,
                odometer = 120080,
                timestamp = 1700015000000L
            )
        )

        expenseRepository.savedExpenses.add(
            Expense(
                id = "exp-1",
                shiftId = sampleShiftId,
                concept = ExpenseConcept.WASH,
                amount = 6000.0,
                timestamp = 1700030000000L
            )
        )
    }

    // --------------------------------------------------------------------------
    // BLOQUE 1: VALIDACIÓN DE REPORTE TABULAR CSV (MULTI-TABLA Y UTF-8 BOM)
    // --------------------------------------------------------------------------
    @Test
    fun testGenerateCsvReportMultiTableStructureAndUtf8Bom() = runBlocking {
        val report = generateCsvReportUseCase(
            periodFilter = HistoryFilterPeriod.WEEK,
            shifts = listOf(sampleSummary)
        )

        // 1. Verificación del encabezado UTF-8 BOM (\uFEFF)
        assertTrue(
            "El reporte CSV debe iniciar con el marcador UTF-8 BOM (\\uFEFF)",
            report.content.startsWith("\uFEFF")
        )

        // 2. Verificación de presencia de las 4 secciones contables
        assertTrue("Debe contener Sección 1", report.content.contains("SECCION 1: RESUMEN DE JORNADAS"))
        assertTrue("Debe contener Sección 2", report.content.contains("SECCION 2: DETALLE DE CARRERAS"))
        assertTrue("Debe contener Sección 3", report.content.contains("SECCION 3: ABASTECIMIENTO DE COMBUSTIBLE"))
        assertTrue("Debe contener Sección 4", report.content.contains("SECCION 4: GASTOS OPERATIVOS EN RUTA"))

        // 3. Verificación de delimitador punto y coma y sanitización
        assertTrue("Debe contener ID del turno", report.content.contains(sampleShiftId))
        assertTrue("Debe contener UBER", report.content.contains("Uber"))
        assertTrue("Debe contener DiDi", report.content.contains("DiDi"))
        assertTrue("Debe contener Gas Natural Vehicular", report.content.contains("Gas Natural Vehicular"))
        assertTrue("Debe contener Lavado / Aseo", report.content.contains("Lavado / Aseo"))

        // 4. Verificación de conteo de filas (1 shift + 2 trips + 1 fueling + 1 expense = 5 registros)
        assertEquals(5, report.totalRows)

        // 5. Verificación de nombre del archivo
        assertTrue(report.fileName.startsWith("kilometa_reporte_week_"))
        assertTrue(report.fileName.endsWith(".csv"))
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: VALIDACIÓN DE TEXTO PARA WHATSAPP / TELEGRAM
    // --------------------------------------------------------------------------
    @Test
    fun testGenerateShareableSummarySingleShift() {
        val summary = generateShareableSummaryUseCase(sampleSummary)

        assertTrue(summary.subject.contains("Liquidación KiloMeta"))
        val text = summary.messageText

        // Encabezado con emojis
        assertTrue(text.contains("🚕 *KILOMETA - LIQUIDACIÓN DE JORNADA*"))
        // Odómetro
        assertTrue(text.contains("120000 ➔ 120150 (150 km)"))
        // Ingresos y comisiones
        assertTrue(text.contains("💵 *Ingresos Brutos:* $ 220.000 (2 carreras)"))
        assertTrue(text.contains("📉 *Comisiones App:* -$ 44.000"))
        assertTrue(text.contains("💰 *Neto en Mano:* $ 176.000"))
        // Combustible bi-modal
        assertTrue(text.contains("GNV (Gas): $ 25.000"))
        // Beneficio neto y meta
        assertTrue(text.contains("✨ *BENEFICIO NETO:* $ 145.000"))
        assertTrue(text.contains("✅ *Meta Cuota:* $ 83.333"))
        // Ahorro auditado
        assertTrue(text.contains("🌱 *Ahorro con GNV:*"))
        // Rótulo offline
        assertTrue(text.contains("Generado con KiloMeta 100% Offline"))
    }

    @Test
    fun testGenerateShareableSummaryConsolidatedPeriod() {
        val summary = generateShareableSummaryUseCase(
            periodFilter = HistoryFilterPeriod.MONTH,
            shifts = listOf(sampleSummary)
        )

        assertTrue(summary.subject.contains("Reporte KiloMeta - Este Mes"))
        val text = summary.messageText

        assertTrue(text.contains("🚕 *KILOMETA - REPORTE CONSOLIDADO*"))
        assertTrue(text.contains("🗓️ *Período:* ESTE MES"))
        assertTrue(text.contains("📋 *Jornadas Registradas:* 1"))
        assertTrue(text.contains("🛣️ *Kilómetros Totales:* 150 km"))
        assertTrue(text.contains("🏁 *Carreras Atendidas:* 2"))
        assertTrue(text.contains("🏆 *BENEFICIO NETO TOTAL:* $ 145.000"))
    }
}
