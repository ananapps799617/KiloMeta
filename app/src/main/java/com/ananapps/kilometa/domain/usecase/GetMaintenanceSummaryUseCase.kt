// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/GetMaintenanceSummaryUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso que genera el consolidado financiero de inversiones en taller
// e historial completo de intervenciones mecánicas del vehículo.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.MaintenanceRecord
import com.ananapps.kilometa.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class MaintenanceDashboardData(
    val records: List<MaintenanceRecord>,
    val totalInvestmentCop: Double,
    val recordsCount: Int
)

class GetMaintenanceSummaryUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) {

    operator fun invoke(): Flow<MaintenanceDashboardData> {
        return combine(
            maintenanceRepository.getAllMaintenances(),
            maintenanceRepository.getTotalMaintenanceCost()
        ) { records, totalCost ->
            MaintenanceDashboardData(
                records = records,
                totalInvestmentCop = totalCost,
                recordsCount = records.size
            )
        }
    }
}
