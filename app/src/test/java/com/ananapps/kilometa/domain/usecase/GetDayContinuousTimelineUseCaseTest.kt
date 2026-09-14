// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/GetDayContinuousTimelineUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pruebas unitarias para validar el cálculo automático de jornada y resumen continuo
// al estilo KiloMeta (desde el primer registro del día al último).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class GetDayContinuousTimelineUseCaseTest {

    private lateinit var fakeShiftRepository: FakeShiftRepository
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var fakeWalletTransferRepository: FakeWalletTransferRepository
    private lateinit var useCase: GetDayContinuousTimelineUseCase

    @Before
    fun setUp() {
        fakeShiftRepository = FakeShiftRepository()
        fakeExpenseRepository = FakeExpenseRepository()
        fakeWalletTransferRepository = FakeWalletTransferRepository()
        useCase = GetDayContinuousTimelineUseCase(
            fakeShiftRepository,
            fakeExpenseRepository,
            fakeWalletTransferRepository,
            CalculateShiftCashSettlementUseCase()
        )
    }

    @Test
    fun testEmptyDayProducesCleanSummary() = runBlocking {
        val summary = useCase().first()
        assertEquals(0.0, summary.totalNetIncome, 0.001)
        assertEquals(0.0, summary.totalFuelSpent, 0.001)
        assertEquals(0.0, summary.pocketNet, 0.001)
        assertEquals(0, summary.totalTripsCount)
        assertTrue(summary.events.isEmpty())
        assertFalse(summary.hasActiveShift)
    }

    @Test
    fun testDayEventsCalculatesShiftExtremesAndFinances() = runBlocking {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
        }
        val morningTime = calendar.timeInMillis

        // Carrera 1 a las 6:00 AM
        val trip1 = Trip(
            id = "t1",
            shiftId = "shift-auto-1",
            platformType = PlatformType.UBER,
            grossAmount = 30000.0,
            commissionAmount = 5000.0,
            netAmount = 25000.0,
            paymentMethod = PaymentMethod.APP_WALLET,
            paymentDestination = PaymentDestination.APP_RETAINED,
            timestamp = morningTime
        )

        // Tanqueo a las 8:30 AM
        val fueling = Fueling(
            id = "f1",
            shiftId = "shift-auto-1",
            fuelType = FuelType.GAS,
            amountPaid = 20000.0,
            volume = 9.09,
            odometer = 7980,
            timestamp = morningTime + (2 * 3600 * 1000)
        )

        // Carrera 2 a las 11:30 AM
        val trip2 = Trip(
            id = "t2",
            shiftId = "shift-auto-1",
            platformType = PlatformType.DIDI,
            grossAmount = 50000.0,
            commissionAmount = 10000.0,
            netAmount = 40000.0,
            paymentMethod = PaymentMethod.CASH,
            paymentDestination = PaymentDestination.CASH_IN_HAND,
            timestamp = morningTime + (5 * 3600 * 1000) + (30 * 60 * 1000)
        )

        fakeShiftRepository.allTripsList.add(trip1)
        fakeShiftRepository.allTripsList.add(trip2)
        fakeShiftRepository.allFuelingsList.add(fueling)

        val summary = useCase().first()

        assertEquals(2, summary.totalTripsCount)
        assertEquals(3, summary.events.size)
        assertEquals(65000.0, summary.totalNetIncome, 0.001) // 25k + 40k
        assertEquals(80000.0, summary.totalGrossProduced, 0.001) // 30k + 50k
        assertEquals(20000.0, summary.totalFuelSpent, 0.001)
        assertEquals(45000.0, summary.pocketNet, 0.001) // 65k - 20k
        assertEquals(morningTime, summary.firstEventTimestamp)
        assertEquals("5h 30m", summary.elapsedHoursFormatted)

        // Verificación de liquidación continua de caja y billeteras
        assertNotNull(summary.cashSettlement)
        val settlement = summary.cashSettlement!!
        assertEquals(30000.0, settlement.cashInHandAvailable, 0.001) // 50k cash - 20k fuel
        assertEquals(25000.0, settlement.uberWallet.netBalance, 0.001) // 25k app wallet
        assertEquals(-10000.0, settlement.didiWallet.netBalance, 0.001) // -10k debt
    }

    @Test
    fun testTimelineIncludesTransfersAndReflectsInSettlement() = runBlocking {
        val now = System.currentTimeMillis()
        val trip = Trip(
            id = "t1",
            shiftId = "s1",
            platformType = PlatformType.UBER,
            grossAmount = 40000.0,
            commissionAmount = 10000.0,
            netAmount = 30000.0,
            paymentMethod = PaymentMethod.APP_WALLET,
            paymentDestination = PaymentDestination.APP_RETAINED,
            timestamp = now
        )
        val transfer = WalletTransfer(
            id = "wt1",
            sourceWallet = WalletType.UBER,
            destinationWallet = WalletType.DIGITAL_WALLET,
            amount = 20000.0,
            timestamp = now + 1000,
            transferType = TransferType.PLATFORM_PAYOUT,
            notes = "Dispersión semanal Uber a Nequi"
        )
        fakeShiftRepository.allTripsList.add(trip)
        fakeWalletTransferRepository.savedTransfers.add(transfer)

        val summary = useCase().first()
        assertEquals(2, summary.events.size)
        assertTrue(summary.events.any { it is DayActivityEvent.TransferItem })

        val settlement = summary.cashSettlement
        assertNotNull(settlement)
        assertEquals(10000.0, settlement!!.uberWalletBalance, 0.001) // 30k neto - 20k transferido
        assertEquals(20000.0, settlement.digitalWalletBalance, 0.001) // 20k recibido en Nequi
        assertEquals(30000.0, settlement.totalNetAssets, 0.001) // 10k Uber + 20k Nequi
    }
}
