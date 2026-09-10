// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/DriverProfileRepository.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contrato desacoplado para consulta y persistencia de conductor y vehículo.
// ==============================================================================

package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.DriverProfile
import com.ananapps.kilometa.domain.model.VehicleProfile
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------------
// BLOQUE 1: CONTRATO DE GESTIÓN DE PERFIL Y VEHÍCULO
// ------------------------------------------------------------------------------
interface DriverProfileRepository {
    fun getProfile(): Flow<DriverProfile>
    suspend fun saveProfile(profile: DriverProfile)
    fun getVehicleProfile(): Flow<VehicleProfile>
    suspend fun saveVehicleProfile(vehicle: VehicleProfile)
    suspend fun saveFullProfile(driver: DriverProfile, vehicle: VehicleProfile)
    suspend fun resetProfileForTesting()
    suspend fun fetchAndHydrateRemoteProfile(userId: String): Result<Boolean>
}
