// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/GetPocketLedgerUseCaseTest.kt
// DESCRIPCI?N PEDAG?GICA:
// Pruebas unitarias para el Extracto Contable y Libro Mayor por Bolsillo (Orden #044).
// Valida el aislamiento estricto por bolsillo, la navegaci?n por semanas y los ciclos (Lunes-Domingo vs Cuota).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetPocketLedgerUseCaseTest {

    private lateinit var fakeShiftRepository: FakeShiftRepository
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var fakeWalletTransferRepository: FakeWalletTransferRepository
    private lateinit var fakeMaintenanceRepository: FakeMaintenanceRepository
    private lateinit var fakeDriverProfileRepository: FakeDriverProfileRepository
    private lateinit var useCase: GetPocketLedgerUseCase

    @Before
    fun setUp() {
        fakeShiftRepository = FakeShiftRepository()
        fakeExpenseRepository = FakeExpenseRepository()
        fakeWalletTransferRepository = FakeWalletTransferRepository()
        fakeMaintenanceRepository = FakeMaintenanceRepository()
        fakeDriverProfileRepository = FakeDriverProfileRepository().apply {
            currentVehicle = VehicleProfile(quotaCutoffDay = 2, quotaCutoffHour = 23) // Martes 23:59
        }
        useCase = GetPocketLedgerUseCase(
            fakeShiftRepository,
            fakeExpenseRepository,
            fakeWalletTransferRepository,
            fakeMaintenanceRepository,
            fakeDriverProfileRepository,
            CalculateWeeklyCycleBoundsUseCase()
        )
    }

    @Test
    fun testUberLedgerIsolatesUberMovementsAndPlatformWeek() = runBlocking {
        val now = System.currentTimeMillis()

        // 1. Carrera Digital Uber (37.5k neta a favor)
        val uberTripDigital = Trip(
            id = "u1",
            shiftId = "s1",
            platformType = PlatformType.UBER,
            grossAmount = 50_000.0,
            commissionAmount = 12_500.0,
            netAmount = 37_500.0,
            paymentMethod = PaymentMethod.APP_WALLET,
            paymentDestination = PaymentDestination.APP_RETAINED,
            timestamp = now
        )

        // 2. Carrera Efectivo Uber (Comisi?n 7.5k en contra de la billetera Uber)
        val uberTripCash = Trip(
            id = "u2",
            shiftId = "s1",
            platformType = PlatformType.UBER,
            grossAmount = 30_000.0,
            commissionAmount = 7_500.0,
            netAmount = 22_500.0,
            paymentMethod = PaymentMethod.CASH,
            paymentDestination = PaymentDestination.CASH_IN_HAND,
            timestamp = now + 1000
        )

        // 3. Carrera DiDi (NO DEBE APARECER EN EL EXTRACTO DE UBER)
        val didiTrip = Trip(
            id = "d1",
            shiftId = "s1",
            platformType = PlatformType.DIDI,
            grossAmount = 20_000.0,
            commissionAmount = 4_000.0,
            netAmount = 16_000.0,
            paymentMethod = PaymentMethod.APP_WALLET,
            paymentDestination = PaymentDestination.APP_RETAINED,
            timestamp = now + 2000
        )

        // 4. Retiro / Dispersi?n Uber a Nequi (20k en contra de Uber)
        val transfer = WalletTransfer(
            id = "tr1",
            sourceWallet = WalletType.UBER,
            destinationWallet = WalletType.DIGITAL_WALLET,
            amount = 20_000.0,
            timestamp = now + 3000,
            transferType = TransferType.PLATFORM_PAYOUT
        )

        fakeShiftRepository.allTripsList.addAll(listOf(uberTripDigital, uberTripCash, didiTrip))
        fakeWalletTransferRepository.savedTransfers.add(transfer)

        val summary = useCase(PocketType.UBER, weekOffset = 0).first()

        assertEquals(PocketType.UBER, summary.pocketType)
        assertEquals(3, summary.transactions.size)
        // 37.5k entrada - 7.5k comisi?n - 20k retiro = 10k neto
        assertEquals(37_500.0, summary.totalInflow, 0.001)
        assertEquals(27_500.0, summary.totalOutflow, 0.001)
        assertEquals(10_000.0, summary.netPeriodBalance, 0.001)
        assertFalse(summary.canGoNext)
    }

    @Test
    fun testCashPhysicalLedgerTracksCashAndFuelExpenses() = runBlocking {
        val now = System.currentTimeMillis()

        val cashTrip = Trip(
            id = "c1",
            shiftId = "s1",
            platformType = PlatformType.PRIVATE,
            grossAmount = 40_000.0,
            commissionAmount = 0.0,
            netAmount = 40_000.0,
            paymentMethod = PaymentMethod.CASH,
            paymentDestination = PaymentDestination.CASH_IN_HAND,
            timestamp = now
        )

        val fuelCash = Fueling(
            id = "f1",
            shiftId = "s1",
            fuelType = FuelType.GAS,
            amountPaid = 22_000.0,
            volume = 10.0,
            odometer = 100_000,
            timestamp = now + 500,
            paymentSource = WalletType.CASH_PHYSICAL
        )

        fakeShiftRepository.allTripsList.add(cashTrip)
        fakeShiftRepository.allFuelingsList.add(fuelCash)

        val summary = useCase(PocketType.CASH_PHYSICAL, weekOffset = 0).first()

        assertEquals(PocketType.CASH_PHYSICAL, summary.pocketType)
        assertEquals(2, summary.transactions.size)
        assertEquals(40_000.0, summary.totalInflow, 0.001)
        assertEquals(22_000.0, summary.totalOutflow, 0.001)
        assertEquals(18_000.0, summary.netPeriodBalance, 0.001)
    }

    @Test
    fun testPastWeekOffsetCanGoNextIsTrue() = runBlocking {
        val summary = useCase(PocketType.DIDI, weekOffset = 1).first()
        assertTrue(summary.canGoNext)
    }
}
