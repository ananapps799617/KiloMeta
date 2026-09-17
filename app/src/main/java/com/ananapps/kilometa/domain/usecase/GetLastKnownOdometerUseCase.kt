// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/GetLastKnownOdometerUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso que actúa como Única Fuente de la Verdad (SSOT) para el odómetro.
// Consulta Room SQLite para obtener el mayor kilometraje registrado entre turnos,
// tanqueos y mantenimientos. Erradica cualquier valor hardcodeado o inventado.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE ODÓMETRO INTELIGENTE
// ------------------------------------------------------------------------------
class GetLastKnownOdometerUseCase @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val driverProfileRepository: DriverProfileRepository? = null
) {

    /**
     * Emite de forma reactiva el último odómetro conocido registrado en el sistema.
     * Si no hay odómetros en la base de datos de actividades, utiliza el odómetro base
     * registrado en el perfil del vehículo como suelo canónico.
     */
    operator fun invoke(): Flow<Int> {
        val shiftOdoFlow = shiftRepository.getLastKnownOdometer()
        val profileRepo = driverProfileRepository
        return if (profileRepo != null) {
            combine(shiftOdoFlow, profileRepo.getVehicleProfile()) { lastOdo, vehicle ->
                val odo = lastOdo ?: 0
                if (odo > 0) odo else vehicle.initialOdometer
            }
        } else {
            shiftOdoFlow.map { it ?: 0 }
        }
    }

    /**
     * Obtiene de forma síncrona/suspendida el último odómetro conocido.
     */
    suspend fun getSync(): Int {
        return invoke().first()
    }
}
