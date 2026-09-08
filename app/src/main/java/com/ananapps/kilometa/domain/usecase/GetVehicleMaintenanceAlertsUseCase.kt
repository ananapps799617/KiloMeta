// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/GetVehicleMaintenanceAlertsUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso predictivo que calcula el semáforo de mantenimiento del vehículo.
// Compara el odómetro actual contra los próximos mantenimientos programados
// y clasifica el nivel de riesgo: OK (Verde), UPCOMING (Amarillo), OVERDUE (Rojo).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.MaintenanceAlert
import com.ananapps.kilometa.domain.model.MaintenanceAlertLevel
import com.ananapps.kilometa.domain.model.MaintenanceType
import com.ananapps.kilometa.domain.repository.MaintenanceRepository
import javax.inject.Inject

class GetVehicleMaintenanceAlertsUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) {

    suspend operator fun invoke(currentOdometer: Int): List<MaintenanceAlert> {
        val criticalTypes = listOf(
            MaintenanceType.OIL_CHANGE,
            MaintenanceType.BRAKES,
            MaintenanceType.FILTERS,
            MaintenanceType.TIRES,
            MaintenanceType.TIMING_BELT,
            MaintenanceType.GENERAL_CHECKUP
        )

        return criticalTypes.mapNotNull { type ->
            val latest = maintenanceRepository.getLatestMaintenanceByType(type) ?: return@mapNotNull null
            val nextDue = latest.nextDueOdometer ?: (latest.odometer + type.defaultIntervalKm)
            val kmRemaining = nextDue - currentOdometer

            val alertLevel = when {
                kmRemaining <= 0 -> MaintenanceAlertLevel.OVERDUE
                kmRemaining <= 500 -> MaintenanceAlertLevel.UPCOMING
                else -> MaintenanceAlertLevel.OK
            }

            MaintenanceAlert(
                type = type,
                lastOdometer = latest.odometer,
                nextDueOdometer = nextDue,
                kmRemaining = kmRemaining,
                alertLevel = alertLevel,
                lastServiceTimestamp = latest.timestamp
            )
        }.sortedBy { it.kmRemaining }
    }
}
