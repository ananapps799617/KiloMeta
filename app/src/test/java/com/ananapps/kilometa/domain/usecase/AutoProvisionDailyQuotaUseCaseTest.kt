// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/AutoProvisionDailyQuotaUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pruebas unitarias para el aprovisionamiento automático de cuota de vehículo hacia Cuota Carro.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AutoProvisionDailyQuotaUseCaseTest {

    private lateinit var fakeDriverProfileRepo: FakeDriverProfileRepository
    private lateinit var fakeShiftRepo: FakeShiftRepository
    private lateinit var fakeWalletTransferRepo: FakeWalletTransferRepository
    private lateinit var useCase: AutoProvisionDailyQuotaUseCase

    @Before
    fun setUp() {
        fakeDriverProfileRepo = FakeDriverProfileRepository()
        fakeShiftRepo = FakeShiftRepository()
        fakeWalletTransferRepo = FakeWalletTransferRepository()

        useCase = AutoProvisionDailyQuotaUseCase(
            driverProfileRepository = fakeDriverProfileRepo,
            shiftRepository = fakeShiftRepo,
            walletTransferRepository = fakeWalletTransferRepo,
            calculateWeeklyCycleBoundsUseCase = CalculateWeeklyCycleBoundsUseCase(),
            calculateDynamicQuotaUseCase = CalculateDynamicQuotaUseCase()
        )
    }

    @Test
    fun testAutoProvisionDividesQuotaByWorkingDaysForActiveDays() = runBlocking {
        // Conductor trabaja 6 días y la cuota semanal es $600.000 COP -> $100.000 diarios
        val vehicle = VehicleProfile(
            vehiclePlate = "ABC-123",
            quotaAmount = 600_000.0,
            workDaysWeek = 6,
            quotaCutoffDay = 2 // Martes
        )
        fakeDriverProfileRepo.currentVehicle = vehicle

        val now = System.currentTimeMillis()
        val trip = Trip(
            id = "trip1",
            shiftId = "s1",
            platformType = PlatformType.UBER,
            grossAmount = 50_000.0,
            commissionAmount = 10_000.0,
            netAmount = 40_000.0,
            paymentMethod = PaymentMethod.APP_WALLET,
            paymentDestination = PaymentDestination.APP_RETAINED,
            timestamp = now
        )
        fakeShiftRepo.allTripsList.add(trip)

        useCase(now)

        assertEquals(1, fakeWalletTransferRepo.savedTransfers.size)
        val transfer = fakeWalletTransferRepo.savedTransfers.first()
        assertEquals(WalletType.DIGITAL_WALLET, transfer.sourceWallet)
        assertEquals(WalletType.CAR_QUOTA, transfer.destinationWallet)
        assertEquals(100_000.0, transfer.amount, 0.001)
        assertEquals(TransferType.QUOTA_PROVISION, transfer.transferType)

        // Idempotencia: ejecutar de nuevo no duplica la transferencia
        useCase(now)
        assertEquals(1, fakeWalletTransferRepo.savedTransfers.size)
    }

    @Test
    fun testAutoProvisionDoesNothingWhenNoTrips() = runBlocking {
        fakeDriverProfileRepo.currentVehicle = VehicleProfile(
            quotaAmount = 500_000.0,
            workDaysWeek = 6
        )

        useCase()
        assertTrue(fakeWalletTransferRepo.savedTransfers.isEmpty())
    }
}
