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

    @Test
    fun whenMultiplePlatforms_calculatesIndependentWalletsCorrectly() {
        // Escenario real planteado por el Gerente General:
        // Carrera 1: Uber pago por app $10.000 neto
        // Carrera 2: Uber pago en efectivo $15.000, descuento app $3.000
        // Carrera 3: DiDi pago en efectivo $20.000, descuento app $4.000
        val trips = listOf(
            Trip(
                id = "u1",
                shiftId = "s1",
                platformType = PlatformType.UBER,
                grossAmount = 14_000.0,
                commissionAmount = 4_000.0,
                netAmount = 10_000.0,
                paymentMethod = PaymentMethod.APP_WALLET,
                paymentDestination = PaymentDestination.APP_RETAINED,
                timestamp = 1000L
            ),
            Trip(
                id = "u2",
                shiftId = "s1",
                platformType = PlatformType.UBER,
                grossAmount = 15_000.0,
                commissionAmount = 3_000.0,
                netAmount = 12_000.0,
                paymentMethod = PaymentMethod.CASH,
                paymentDestination = PaymentDestination.CASH_IN_HAND,
                timestamp = 2000L
            ),
            Trip(
                id = "d1",
                shiftId = "s1",
                platformType = PlatformType.DIDI,
                grossAmount = 20_000.0,
                commissionAmount = 4_000.0,
                netAmount = 16_000.0,
                paymentMethod = PaymentMethod.CASH,
                paymentDestination = PaymentDestination.CASH_IN_HAND,
                timestamp = 3000L
            )
        )

        val result = useCase(trips = trips, fuelings = emptyList(), expenses = emptyList())

        // 1. Efectivo físico en mano: 15.000 + 20.000 = 35.000
        assertEquals(35_000.0, result.cashInHandAvailable, 0.001)

        // 2. Billetera Uber independiente: 10.000 saldo digital - 3.000 comisión efectivo = +7.000 a favor
        assertEquals(10_000.0, result.uberWallet.netDisbursementPending, 0.001)
        assertEquals(3_000.0, result.uberWallet.cashCommissionOwed, 0.001)
        assertEquals(7_000.0, result.uberWallet.netBalance, 0.001)

        // 3. Billetera DiDi independiente: 0 saldo digital - 4.000 comisión efectivo = -4.000 adeudado
        assertEquals(0.0, result.didiWallet.netDisbursementPending, 0.001)
        assertEquals(4_000.0, result.didiWallet.cashCommissionOwed, 0.001)
        assertEquals(-4_000.0, result.didiWallet.netBalance, 0.001)

        // 4. Ganancia limpia total: 10.000 + 12.000 + 16.000 = 38.000 (o 35.000 cash + 7.000 uber - 4.000 didi = 38.000)
        assertEquals(38_000.0, result.totalNet, 0.001)
    }

    @Test
    fun whenTransfersAndPaymentSourcesApplied_calculatesAll4WalletsAccurately() {
        // Viaje 1: Uber pago app 20.000 bruto, 5.000 comision -> 15.000 neto Uber
        // Viaje 2: DiDi efectivo 30.000 bruto, 6.000 comision -> 30.000 cash físico, -6.000 deuda DiDi
        // Tanqueo: 10.000 pagado con Billetera Digital (Nequi)
        // Gasto: 5.000 pagado con Efectivo Físico
        // Transferencia: Dispersión de Uber a Nequi por 7.000
        val trips = listOf(
            Trip(
                id = "u1",
                shiftId = "s1",
                platformType = PlatformType.UBER,
                grossAmount = 20_000.0,
                commissionAmount = 5_000.0,
                netAmount = 15_000.0,
                paymentMethod = PaymentMethod.APP_WALLET,
                paymentDestination = PaymentDestination.APP_RETAINED,
                timestamp = 1000L
            ),
            Trip(
                id = "d1",
                shiftId = "s1",
                platformType = PlatformType.DIDI,
                grossAmount = 30_000.0,
                commissionAmount = 6_000.0,
                netAmount = 24_000.0,
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
                amountPaid = 10_000.0,
                volume = 4.0,
                odometer = 100_020,
                timestamp = 2500L,
                paymentSource = com.ananapps.kilometa.domain.model.WalletType.DIGITAL_WALLET
            )
        )

        val expenses = listOf(
            Expense(
                id = "e1",
                shiftId = "s1",
                concept = ExpenseConcept.WASH,
                amount = 5_000.0,
                timestamp = 2600L,
                paymentSource = com.ananapps.kilometa.domain.model.WalletType.CASH_PHYSICAL
            )
        )

        val transfers = listOf(
            com.ananapps.kilometa.domain.model.WalletTransfer(
                id = "wt1",
                timestamp = 2700L,
                sourceWallet = com.ananapps.kilometa.domain.model.WalletType.UBER,
                destinationWallet = com.ananapps.kilometa.domain.model.WalletType.DIGITAL_WALLET,
                amount = 7_000.0,
                transferType = com.ananapps.kilometa.domain.model.TransferType.PLATFORM_PAYOUT
            )
        )

        val result = useCase(trips = trips, fuelings = fuelings, expenses = expenses, transfers = transfers)

        // 1. Efectivo físico: 30.000 cash - 5.000 gasto = 25.000
        assertEquals(25_000.0, result.cashPhysicalBalance, 0.001)
        // 2. Digital (Nequi): 0 inicial - 10.000 tanqueo + 7.000 transfer = -3.000
        assertEquals(-3_000.0, result.digitalWalletBalance, 0.001)
        // 3. Uber: 15.000 saldo base - 7.000 transferido a Nequi = 8.000
        assertEquals(8_000.0, result.uberWalletBalance, 0.001)
        // 4. DiDi: -6.000 comisión adeudada
        assertEquals(-6_000.0, result.didiWalletBalance, 0.001)
        // 5. Total Net Assets: 25.000 - 3.000 + 8.000 - 6.000 = 24.000
        assertEquals(24_000.0, result.totalNetAssets, 0.001)
    }
}
