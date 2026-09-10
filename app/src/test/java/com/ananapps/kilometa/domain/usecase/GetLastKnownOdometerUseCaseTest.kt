// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/GetLastKnownOdometerUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pruebas unitarias para validar que el odómetro se resuelva dinámicamente
// desde la base de datos sin inventar números ni hardcodear 100.000.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.Shift
import com.ananapps.kilometa.domain.model.ShiftStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetLastKnownOdometerUseCaseTest {

    private lateinit var fakeShiftRepository: FakeShiftRepository
    private lateinit var getLastKnownOdometerUseCase: GetLastKnownOdometerUseCase

    @Before
    fun setUp() {
        fakeShiftRepository = FakeShiftRepository()
        getLastKnownOdometerUseCase = GetLastKnownOdometerUseCase(fakeShiftRepository)
    }

    @Test
    fun testEmptyDatabaseReturnsZero() = runBlocking {
        val odo = getLastKnownOdometerUseCase().first()
        assertEquals(0, odo)
    }

    @Test
    fun testShiftOdometerResolved() = runBlocking {
        fakeShiftRepository.shifts.add(
            Shift(
                id = "shift-1",
                startTime = System.currentTimeMillis(),
                startOdometer = 7975,
                endOdometer = 8050,
                dailyQuotaTarget = 80000.0,
                status = ShiftStatus.COMPLETED
            )
        )

        val odo = getLastKnownOdometerUseCase().first()
        assertEquals(8050, odo)
    }

    @Test
    fun testFuelingOdometerHigherThanShiftResolved() = runBlocking {
        fakeShiftRepository.shifts.add(
            Shift(
                id = "shift-1",
                startTime = System.currentTimeMillis() - 10000,
                startOdometer = 7975,
                endOdometer = 8050,
                dailyQuotaTarget = 80000.0,
                status = ShiftStatus.COMPLETED
            )
        )

        fakeShiftRepository.allFuelingsList.add(
            Fueling(
                id = "fuel-1",
                shiftId = "shift-1",
                fuelType = FuelType.GAS,
                amountPaid = 25000.0,
                volume = 11.36,
                odometer = 8120,
                timestamp = System.currentTimeMillis()
            )
        )

        val odo = getLastKnownOdometerUseCase().first()
        assertEquals(8120, odo)
    }
}
