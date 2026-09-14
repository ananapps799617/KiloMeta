// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/RegisterMaintenanceUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para registrar eventos de mantenimiento preventivo y correctivo.
// Aplica Domain Guards contables y proyecta automáticamente el siguiente cambio
// según los intervalos recomendados de kilometraje.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.MaintenanceRecord
import com.ananapps.kilometa.domain.model.MaintenanceType
import com.ananapps.kilometa.domain.repository.MaintenanceRepository
import java.util.UUID
import javax.inject.Inject

class RegisterMaintenanceUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) {

    suspend operator fun invoke(
        vehicleId: String,
        type: MaintenanceType,
        odometer: Int,
        costCop: Double,
        notes: String? = null,
        customNextDueOdo: Int? = null,
        nextDueDateMillis: Long? = null,
        timestamp: Long = System.currentTimeMillis(),
        serviceTitle: String? = null,
        location: String? = null,
        paymentSource: com.ananapps.kilometa.domain.model.WalletType = com.ananapps.kilometa.domain.model.WalletType.CASH_PHYSICAL
    ): MaintenanceRecord {
        require(vehicleId.isNotBlank()) { "El identificador del vehículo no puede estar vacío" }
        require(odometer >= 0) { "El odómetro no puede ser negativo" }
        require(costCop >= 0.0) { "El costo del mantenimiento no puede ser negativo" }

        val computedNextDue = customNextDueOdo ?: (odometer + type.defaultIntervalKm)

        val record = MaintenanceRecord(
            id = UUID.randomUUID().toString(),
            vehicleId = vehicleId,
            type = type,
            odometer = odometer,
            costCop = costCop,
            notes = notes,
            nextDueOdometer = computedNextDue,
            nextDueDateMillis = nextDueDateMillis,
            timestamp = timestamp,
            serviceTitle = serviceTitle,
            location = location,
            paymentSource = paymentSource
        )

        maintenanceRepository.addMaintenance(record)
        return record
    }
}
