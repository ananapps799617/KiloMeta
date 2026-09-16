// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/UpdateMaintenanceUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para la actualización y modificación de registros de mantenimiento preventivo (Orden #041).
// Valida costo positivo e invariantes de odómetro antes de persistir en Room SQLite.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.MaintenanceRecord
import com.ananapps.kilometa.domain.repository.MaintenanceRepository
import javax.inject.Inject

/**
 * Modifica un registro de mantenimiento existente validando costo y odómetro.
 */
class UpdateMaintenanceUseCase @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository
) {

    /**
     * Ejecuta la actualización de un registro de mantenimiento.
     *
     * @param maintenance Registro de mantenimiento con datos actualizados.
     */
    suspend operator fun invoke(maintenance: MaintenanceRecord) {
        require(maintenance.id.isNotBlank()) { "El identificador del mantenimiento no puede estar vacío" }
        require(maintenance.costCop >= 0.0) { "El costo del mantenimiento no puede ser negativo" }
        require(maintenance.odometer >= 0) { "El odómetro del mantenimiento no puede ser negativo" }

        maintenanceRepository.updateMaintenance(maintenance)
    }
}
