// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/CalculateShiftDidiTrimodalSettlementTest.kt
// DESCRIPCIÓN: Prueba unitaria para el cruce de cuentas trimodal de DiDi (Micro-Orden #051).
// Valida: Andrés (Tarjeta), Daniela (Subsidio Nequi) y Mery (Efectivo con deuda de comisión).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.Trip
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateShiftDidiTrimodalSettlementTest {

    private lateinit var useCase: CalculateShiftCashSettlementUseCase

    @Before
    fun setup() {
        useCase = CalculateShiftCashSettlementUseCase()
    }

    @Test
    fun whenTrimodalDidiTripsOccur_calculatesCrossSettlementAndBalancesAccurately() {
        // 1. Andrés (Tarjeta): Cobrado $24.000, comisión $6.000, limpio $18.000 (Saldo App)
        val tripAndres = Trip(
            id = "t_andres",
            shiftId = "s1",
            platformType = PlatformType.DIDI,
            grossAmount = 24_000.0,
            commissionAmount = 6_000.0,
            netAmount = 18_000.0,
            paymentMethod = PaymentMethod.APP_WALLET,
            paymentDestination = PaymentDestination.APP_RETAINED,
            timestamp = 1000L
        )

        // 2. Daniela (Subsidio): Cliente pagó $16.720 en Nequi, DiDi abonó +$2.299 en app, limpia $19.019
        val tripDaniela = Trip(
            id = "t_daniela",
            shiftId = "s1",
            platformType = PlatformType.DIDI,
            grossAmount = 16_720.0,
            commissionAmount = 0.0,
            netAmount = 19_019.0,
            paymentMethod = PaymentMethod.NEQUI,
            paymentDestination = PaymentDestination.CASH_IN_HAND,
            platformPromoDiscount = 2_299.0,
            timestamp = 2000L
        )

        // 3. Mery (Efectivo): Cobrado $11.000 en mano, comisión -$3.034 adeuda a DiDi, limpia $7.966
        val tripMery = Trip(
            id = "t_mery",
            shiftId = "s1",
            platformType = PlatformType.DIDI,
            grossAmount = 11_000.0,
            commissionAmount = 3_034.0,
            netAmount = 7_966.0,
            paymentMethod = PaymentMethod.CASH,
            paymentDestination = PaymentDestination.CASH_IN_HAND,
            timestamp = 3000L
        )

        val result = useCase(
            trips = listOf(tripAndres, tripDaniela, tripMery),
            fuelings = emptyList(),
            expenses = emptyList()
        )

        // Verificación de recaudo físico en mano
        assertEquals(11_000.0, result.cashPhysicalBalance, 0.001)

        // Verificación de recaudo digital (Nequi / Daviplata)
        assertEquals(16_720.0, result.digitalWalletBalance, 0.001)

        // Verificación de Billetera DiDi:
        // Saldo neto = Digital limpio (18.000) + Subsidio app (2.299) - Deuda comisión cash (3.034) = +$17.265
        val didiWallet = result.didiWallet
        assertEquals(24_000.0, didiWallet.digitalGross, 0.001)
        assertEquals(6_000.0, didiWallet.digitalCommission, 0.001)
        assertEquals(2_299.0, didiWallet.platformSubsidy, 0.001)
        assertEquals(3_034.0, didiWallet.cashCommissionOwed, 0.001)
        assertEquals(17_265.0, didiWallet.netBalance, 0.001)
        assertEquals(17_265.0, result.didiWalletBalance, 0.001)

        // Verificación de patrimonio total neto acumulado:
        // 11.000 (mano) + 16.720 (nequi) + 17.265 (didi app) = $44.985
        assertEquals(44_985.0, result.totalNetAssets, 0.001)

        // Suma de ganancias netas de las 3 carreras:
        // 18.000 + 19.019 + 7.966 = $44.985
        assertEquals(44_985.0, result.totalNet, 0.001)
    }
}
