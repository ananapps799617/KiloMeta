// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/MaintenanceUseCasesTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pruebas unitarias para los casos de uso del módulo de mantenimiento preventivo (KiloMeta).
// Evalúa semáforo mecánico predictivo, proyecciones de kilometraje e integridad contable.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.MaintenanceAlertLevel
import com.ananapps.kilometa.domain.model.MaintenanceRecord
import com.ananapps.kilometa.domain.model.MaintenanceType
import com.ananapps.kilometa.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class FakeMaintenanceRepository : MaintenanceRepository {
    val records = mutableListOf<MaintenanceRecord>()

    override fun getAllMaintenances(): Flow<List<MaintenanceRecord>> = flowOf(records)

    override fun getMaintenancesByVehicle(vehicleId: String): Flow<List<MaintenanceRecord>> =
        flowOf(records.filter { it.vehicleId == vehicleId })

    override suspend fun getLatestMaintenanceByType(type: MaintenanceType): MaintenanceRecord? =
        records.filter { it.type == type }.maxByOrNull { it.odometer }

    override fun getTotalMaintenanceCost(): Flow<Double> =
        flowOf(records.sumOf { it.costCop })

    override suspend fun addMaintenance(record: MaintenanceRecord) {
        records.add(record)
    }

    override suspend fun updateMaintenance(record: MaintenanceRecord) {
        val index = records.indexOfFirst { it.id == record.id }
        if (index != -1) {
            records[index] = record
        }
    }

    override suspend fun deleteMaintenance(id: String) {
        records.removeAll { it.id == id }
    }
}

class MaintenanceUseCasesTest {

    private lateinit var fakeRepository: FakeMaintenanceRepository
    private lateinit var registerUseCase: RegisterMaintenanceUseCase
    private lateinit var alertsUseCase: GetVehicleMaintenanceAlertsUseCase
    private lateinit var summaryUseCase: GetMaintenanceSummaryUseCase

    @Before
    fun setup() {
        fakeRepository = FakeMaintenanceRepository()
        registerUseCase = RegisterMaintenanceUseCase(fakeRepository)
        alertsUseCase = GetVehicleMaintenanceAlertsUseCase(fakeRepository)
        summaryUseCase = GetMaintenanceSummaryUseCase(fakeRepository)
    }

    @Test
    fun registerMaintenance_calculatesDefaultNextDueOdometerCorrectly() = runBlocking {
        val record = registerUseCase(
            vehicleId = "veh_01",
            type = MaintenanceType.OIL_CHANGE,
            odometer = 100_000,
            costCop = 120_000.0,
            notes = "Aceite sintético 10W-30"
        )

        assertNotNull(record.id)
        assertEquals(105_000, record.nextDueOdometer)
        assertEquals(1, fakeRepository.records.size)
    }

    @Test
    fun getVehicleMaintenanceAlerts_evaluatesStatusLevelsCorrectly() = runBlocking {
        // Aceite cambiado a los 100,000 km -> vence a los 105,000 km
        registerUseCase("veh_01", MaintenanceType.OIL_CHANGE, 100_000, 100_000.0)
        // Frenos cambiados a los 90,000 km -> vence a los 110,000 km
        registerUseCase("veh_01", MaintenanceType.BRAKES, 90_000, 180_000.0)

        // Caso 1: Odómetro en 101,000 km -> Aceite faltan 4,000 km (OK)
        val alertsOk = alertsUseCase(101_000)
        val oilAlertOk = alertsOk.first { it.type == MaintenanceType.OIL_CHANGE }
        assertEquals(MaintenanceAlertLevel.OK, oilAlertOk.alertLevel)
        assertEquals(4_000, oilAlertOk.kmRemaining)

        // Caso 2: Odómetro en 104,800 km -> Aceite faltan 200 km (UPCOMING <= 500)
        val alertsUpcoming = alertsUseCase(104_800)
        val oilAlertUpcoming = alertsUpcoming.first { it.type == MaintenanceType.OIL_CHANGE }
        assertEquals(MaintenanceAlertLevel.UPCOMING, oilAlertUpcoming.alertLevel)
        assertEquals(200, oilAlertUpcoming.kmRemaining)

        // Caso 3: Odómetro en 105,500 km -> Aceite vencido por 500 km (OVERDUE <= 0)
        val alertsOverdue = alertsUseCase(105_500)
        val oilAlertOverdue = alertsOverdue.first { it.type == MaintenanceType.OIL_CHANGE }
        assertEquals(MaintenanceAlertLevel.OVERDUE, oilAlertOverdue.alertLevel)
        assertEquals(-500, oilAlertOverdue.kmRemaining)
    }

    @Test
    fun getMaintenanceSummary_aggregatesTotalsCorrectly() = runBlocking {
        registerUseCase("veh_01", MaintenanceType.OIL_CHANGE, 100_000, 120_000.0)
        registerUseCase("veh_01", MaintenanceType.FILTERS, 100_000, 45_000.0)

        val data = summaryUseCase().first()
        assertEquals(2, data.recordsCount)
        assertEquals(165_000.0, data.totalInvestmentCop, 0.001)
    }
}
