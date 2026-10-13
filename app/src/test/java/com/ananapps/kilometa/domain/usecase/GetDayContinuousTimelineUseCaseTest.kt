// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/GetDayContinuousTimelineUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pruebas unitarias para validar el cálculo automático de jornada y resumen continuo
// al estilo KiloMeta con la invariante canónica pocketNet = netOperationalMargin - dailyQuotaTarget.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetDayContinuousTimelineUseCaseTest {

    private lateinit var fakeShiftRepository: FakeShiftRepository
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var fakeWalletTransferRepository: FakeWalletTransferRepository
    private lateinit var fakeMaintenanceRepository: FakeMaintenanceRepository
    private lateinit var fakeDriverProfileRepository: FakeDriverProfileRepository
    private lateinit var useCase: GetDayContinuousTimelineUseCase

    @Before
    fun setUp() {
        fakeShiftRepository = FakeShiftRepository()
        fakeExpenseRepository = FakeExpenseRepository()
        fakeWalletTransferRepository = FakeWalletTransferRepository()
        fakeMaintenanceRepository = FakeMaintenanceRepository()
        fakeDriverProfileRepository = FakeDriverProfileRepository().apply {
            currentVehicle = VehicleProfile(quotaAmount = 500_000.0, workDaysWeek = 6, quotaCutoffDay = 2, quotaCutoffHour = 23)
        }
        useCase = GetDayContinuousTimelineUseCase(
            fakeShiftRepository,
            fakeExpenseRepository,
            fakeWalletTransferRepository,
            fakeMaintenanceRepository,
            CalculateShiftCashSettlementUseCase(),
            fakeDriverProfileRepository,
            CalculateWeeklyCycleBoundsUseCase()
        )
    }

    @Test
    fun testEmptyDayWithVehicleQuota_producesAccurateDeficit() = runBlocking {
        val summary = useCase().first()
        val expectedDailyQuota = 500_000.0 / 6.0
        assertEquals(expectedDailyQuota, summary.dailyQuotaTarget, 0.001)
        assertEquals(0.0, summary.netOperationalMargin, 0.001)
        assertEquals(-expectedDailyQuota, summary.pocketNet, 0.001)
        assertEquals(0, summary.totalTripsCount)
        assertTrue(summary.events.isEmpty())
        assertFalse(summary.hasActiveShift)
    }

    @Test
    fun testOwnedVehicleWithoutQuota_producesPocketNetEqualToOperationalMargin() = runBlocking {
        fakeDriverProfileRepository.currentVehicle = VehicleProfile(quotaAmount = 0.0, workDaysWeek = 0)
        val now = System.currentTimeMillis()
        fakeShiftRepository.allTripsList.add(
            Trip(
                id = "t1", shiftId = "s1", platformType = PlatformType.UBER,
                grossAmount = 100_000.0, commissionAmount = 15_000.0, netAmount = 85_000.0,
                paymentMethod = PaymentMethod.APP_WALLET, paymentDestination = PaymentDestination.APP_RETAINED,
                timestamp = now
            )
        )
        fakeShiftRepository.allFuelingsList.add(
            Fueling(
                id = "f1", shiftId = "s1", fuelType = FuelType.GAS,
                amountPaid = 35_000.0, volume = 15.0, odometer = 1000, timestamp = now
            )
        )

        val summary = useCase().first()
        assertEquals(0.0, summary.dailyQuotaTarget, 0.001)
        assertEquals(50_000.0, summary.netOperationalMargin, 0.001)
        assertEquals(50_000.0, summary.pocketNet, 0.001)
    }

    @Test
    fun testExactBreakeven_producesZeroPocketNet() = runBlocking {
        fakeDriverProfileRepository.currentVehicle = VehicleProfile(quotaAmount = 480_000.0, workDaysWeek = 6)
        val now = System.currentTimeMillis()
        fakeShiftRepository.allTripsList.add(
            Trip(
                id = "t1", shiftId = "s1", platformType = PlatformType.DIDI,
                grossAmount = 130_000.0, commissionAmount = 20_000.0, netAmount = 110_000.0,
                paymentMethod = PaymentMethod.CASH, paymentDestination = PaymentDestination.CASH_IN_HAND,
                timestamp = now
            )
        )
        fakeShiftRepository.allFuelingsList.add(
            Fueling(
                id = "f1", shiftId = "s1", fuelType = FuelType.GAS,
                amountPaid = 20_000.0, volume = 9.0, odometer = 1200, timestamp = now
            )
        )
        fakeExpenseRepository.savedExpenses.add(
            Expense(
                id = "e1", shiftId = "s1", concept = ExpenseConcept.WASH,
                amount = 10_000.0, timestamp = now
            )
        )

        val summary = useCase().first()
        assertEquals(80_000.0, summary.dailyQuotaTarget, 0.001)
        assertEquals(80_000.0, summary.netOperationalMargin, 0.001)
        assertEquals(0.0, summary.pocketNet, 0.001)
    }

    @Test
    fun testSurplusAfterQuota_producesPositivePocketNet() = runBlocking {
        fakeDriverProfileRepository.currentVehicle = VehicleProfile(quotaAmount = 500_000.0, workDaysWeek = 6)
        val now = System.currentTimeMillis()
        fakeShiftRepository.allTripsList.add(
            Trip(
                id = "t1", shiftId = "s1", platformType = PlatformType.UBER,
                grossAmount = 250_000.0, commissionAmount = 40_000.0, netAmount = 210_000.0,
                paymentMethod = PaymentMethod.APP_WALLET, paymentDestination = PaymentDestination.APP_RETAINED,
                timestamp = now
            )
        )
        fakeShiftRepository.allFuelingsList.add(
            Fueling(
                id = "f1", shiftId = "s1", fuelType = FuelType.GASOLINE,
                amountPaid = 45_000.0, volume = 2.8, odometer = 1500, timestamp = now
            )
        )
        fakeExpenseRepository.savedExpenses.add(
            Expense(
                id = "e1", shiftId = "s1", concept = ExpenseConcept.TOLL,
                amount = 15_000.0, timestamp = now
            )
        )

        val summary = useCase().first()
        val expectedDailyQuota = 500_000.0 / 6.0
        assertEquals(expectedDailyQuota, summary.dailyQuotaTarget, 0.001)
        assertEquals(150_000.0, summary.netOperationalMargin, 0.001)
        assertEquals(150_000.0 - expectedDailyQuota, summary.pocketNet, 0.001)
    }

    @Test
    fun testOperationalDeficitWithHighExpenses_producesDeeperNegativePocketNet() = runBlocking {
        fakeDriverProfileRepository.currentVehicle = VehicleProfile(quotaAmount = 480_000.0, workDaysWeek = 6)
        val now = System.currentTimeMillis()
        fakeShiftRepository.allTripsList.add(
            Trip(
                id = "t1", shiftId = "s1", platformType = PlatformType.INDRIVE,
                grossAmount = 30_000.0, commissionAmount = 5_000.0, netAmount = 25_000.0,
                paymentMethod = PaymentMethod.CASH, paymentDestination = PaymentDestination.CASH_IN_HAND,
                timestamp = now
            )
        )
        fakeShiftRepository.allFuelingsList.add(
            Fueling(
                id = "f1", shiftId = "s1", fuelType = FuelType.GAS,
                amountPaid = 50_000.0, volume = 20.0, odometer = 2000, timestamp = now
            )
        )
        fakeMaintenanceRepository.records.add(
            MaintenanceRecord(
                id = "m1", vehicleId = "v1", type = MaintenanceType.TIRES,
                costCop = 30_000.0, odometer = 2000, timestamp = now
            )
        )

        val summary = useCase().first()
        assertEquals(80_000.0, summary.dailyQuotaTarget, 0.001)
        assertEquals(-55_000.0, summary.netOperationalMargin, 0.001)
        assertEquals(-135_000.0, summary.pocketNet, 0.001)
    }

    @Test
    fun testZeroWorkDays_avoidsDivisionByZero() = runBlocking {
        fakeDriverProfileRepository.currentVehicle = VehicleProfile(quotaAmount = 600_000.0, workDaysWeek = 0)
        val summary = useCase().first()
        assertEquals(0.0, summary.dailyQuotaTarget, 0.001)
        assertEquals(summary.netOperationalMargin, summary.pocketNet, 0.001)
        assertFalse(summary.dailyQuotaTarget.isNaN())
        assertFalse(summary.dailyQuotaTarget.isInfinite())
    }

    @Test
    fun testTimelineIncludesTransfersAndReflectsInSettlement() = runBlocking {
        val now = System.currentTimeMillis()
        val trip = Trip(
            id = "t1", shiftId = "s1", platformType = PlatformType.UBER,
            grossAmount = 40_000.0, commissionAmount = 10_000.0, netAmount = 30_000.0,
            paymentMethod = PaymentMethod.APP_WALLET, paymentDestination = PaymentDestination.APP_RETAINED,
            timestamp = now
        )
        val transfer = WalletTransfer(
            id = "wt1", sourceWallet = WalletType.UBER, destinationWallet = WalletType.DIGITAL_WALLET,
            amount = 20_000.0, timestamp = now + 1000, transferType = TransferType.PLATFORM_PAYOUT,
            notes = "Dispersión semanal Uber a Nequi"
        )
        fakeShiftRepository.allTripsList.add(trip)
        fakeWalletTransferRepository.savedTransfers.add(transfer)

        val summary = useCase().first()
        assertEquals(2, summary.events.size)
        assertTrue(summary.events.any { it is DayActivityEvent.TransferItem })

        val settlement = summary.cashSettlement
        assertNotNull(settlement)
        assertEquals(10_000.0, settlement!!.uberWalletBalance, 0.001)
        assertEquals(20_000.0, settlement.digitalWalletBalance, 0.001)
        assertEquals(30_000.0, settlement.totalNetAssets, 0.001)
    }
}
