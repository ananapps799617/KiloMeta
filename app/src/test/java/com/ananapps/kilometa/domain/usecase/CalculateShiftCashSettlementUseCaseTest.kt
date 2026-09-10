// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/CalculateShiftCashSettlementUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pruebas unitarias para la liquidación de doble bolsillo en cabina
// (Efectivo en Mano vs Saldo Retenido en Billeteras de Plataforma).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.Trip
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateShiftCashSettlementUseCaseTest {

    private lateinit var useCase: CalculateShiftCashSettlementUseCase

    @Before
    fun setup() {
        useCase = CalculateShiftCashSettlementUseCase()
    }

    @Test
    fun whenAllTripsInCash_allEarningsAreCashInHand() {
        val trips = listOf(
            Trip(
                id = "t1",
                shiftId = "s1",
                platformType = PlatformType.PRIVATE,
                grossAmount = 20_000.0,
                commissionAmount = 0.0,
                netAmount = 20_000.0,
                paymentMethod = PaymentMethod.CASH,
                paymentDestination = PaymentDestination.CASH_IN_HAND,
                timestamp = 1000L
            ),
            Trip(
                id = "t2",
                shiftId = "s1",
                platformType = PlatformType.INDRIVE,
                grossAmount = 30_000.0,
                commissionAmount = 3_000.0,
                netAmount = 27_000.0,
                paymentMethod = PaymentMethod.CASH,
                paymentDestination = PaymentDestination.CASH_IN_HAND,
                timestamp = 2000L
            )
        )

        val result = useCase(trips = trips, fuelings = emptyList(), expenses = emptyList())

        assertEquals(50_000.0, result.cashInHandGross, 0.001)
        assertEquals(0.0, result.digitalBalanceGross, 0.001)
        assertEquals(47_000.0, result.totalNet, 0.001)
        assertEquals(50_000.0, result.cashInHandAvailable, 0.001)
        assertEquals(0.0, result.digitalPendingDisbursement, 0.001)
        assertEquals(3_000.0, result.cashCommissionsOwed, 0.001)
        assertEquals(-3_000.0, result.netPlatformBalance, 0.001)
    }

    @Test
    fun whenTripsAreMixed_properlySeparatesCashAndDigitalBalance() {
        val trips = listOf(
            Trip(
                id = "t1",
                shiftId = "s1",
                platformType = PlatformType.UBER,
                grossAmount = 40_000.0,
                commissionAmount = 10_000.0,
                netAmount = 30_000.0,
                paymentMethod = PaymentMethod.APP_WALLET,
                paymentDestination = PaymentDestination.APP_RETAINED,
                timestamp = 1000L
            ),
            Trip(
                id = "t2",
                shiftId = "s1",
                platformType = PlatformType.DIDI,
                grossAmount = 20_000.0,
                commissionAmount = 4_000.0,
                netAmount = 16_000.0,
                paymentMethod = PaymentMethod.CASH,
                paymentDestination = PaymentDestination.CASH_IN_HAND,
                timestamp = 2000L
            )
        )

        val fuelings = listOf(
            Fueling(
                id = "f1",
                shiftId = "s1",
                fuelType = FuelType.GAS,
                amountPaid = 15_000.0,
                volume = 5.0,
                odometer = 100_050,
                timestamp = 1500L
            )
        )

        val expenses = listOf(
            Expense(
                id = "e1",
                shiftId = "s1",
                concept = ExpenseConcept.FOOD,
                amount = 8_000.0,
                timestamp = 1600L
            )
        )

        val result = useCase(trips = trips, fuelings = fuelings, expenses = expenses)

        assertEquals(20_000.0, result.cashInHandGross, 0.001)
        assertEquals(40_000.0, result.digitalBalanceGross, 0.001)
        assertEquals(46_000.0, result.totalNet, 0.001) // 60k gross - 14k comm = 46k net
        // Cash available: 20k cashGross - 15k fuel - 8k food = -3,000 COP
        assertEquals(-3_000.0, result.cashInHandAvailable, 0.001)
        // Digital pending: 40k gross - 10k comm = 30k COP
        assertEquals(30_000.0, result.digitalPendingDisbursement, 0.001)
        // Cash commission owed from DiDi cash trip: 4,000 COP
        assertEquals(4_000.0, result.cashCommissionsOwed, 0.001)
        // Net platform balance: 30k digital net - 4k cash commission = +26,000 COP
        assertEquals(26_000.0, result.netPlatformBalance, 0.001)
    }
}
