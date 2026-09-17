// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/maintenance/MaintenanceViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel para el Módulo de Mantenimiento Preventivo (@HiltViewModel).
// Orquesta alertas de cambio de aceite/frenos, cálculo de odómetro e historial KiloMeta.
// ==============================================================================

package com.ananapps.kilometa.presentation.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.MaintenanceType
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.repository.MaintenanceRepository
import com.ananapps.kilometa.domain.usecase.GetLastKnownOdometerUseCase
import com.ananapps.kilometa.domain.usecase.GetMaintenanceSummaryUseCase
import com.ananapps.kilometa.domain.usecase.GetVehicleMaintenanceAlertsUseCase
import com.ananapps.kilometa.domain.usecase.RegisterMaintenanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val getMaintenanceSummaryUseCase: GetMaintenanceSummaryUseCase,
    private val getVehicleMaintenanceAlertsUseCase: GetVehicleMaintenanceAlertsUseCase,
    private val registerMaintenanceUseCase: RegisterMaintenanceUseCase,
    private val maintenanceRepository: MaintenanceRepository,
    private val driverProfileRepository: DriverProfileRepository,
    private val getLastKnownOdometerUseCase: GetLastKnownOdometerUseCase,
    private val placeRepository: com.ananapps.kilometa.domain.repository.PlaceRepository
) : ViewModel() {

    val savedPlaces: StateFlow<List<com.ananapps.kilometa.domain.model.Place>> = placeRepository.getAllPlaces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    val uiState: StateFlow<MaintenanceUiState> = combine(
        getMaintenanceSummaryUseCase(),
        driverProfileRepository.getProfile(),
        getLastKnownOdometerUseCase()
    ) { dashboard: com.ananapps.kilometa.domain.usecase.MaintenanceDashboardData, profile: com.ananapps.kilometa.domain.model.DriverProfile, currentOdometer: Int ->
        val alerts = getVehicleMaintenanceAlertsUseCase(currentOdometer)

        MaintenanceUiState(
            isLoading = false,
            records = dashboard.records,
            alerts = alerts,
            totalInvestmentCop = dashboard.totalInvestmentCop,
            currentOdometer = currentOdometer,
            vehiclePlate = profile.vehiclePlate
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), MaintenanceUiState())

    fun registerMaintenance(
        type: MaintenanceType,
        odometer: Int,
        costCop: Double,
        notes: String? = null,
        customNextDueOdo: Int? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val profile = driverProfileRepository.getProfile().first()
            val vehicleId = profile.activeVehicleId.ifBlank { "default_vehicle" }
            registerMaintenanceUseCase(
                vehicleId = vehicleId,
                type = type,
                odometer = odometer,
                costCop = costCop,
                notes = notes,
                customNextDueOdo = customNextDueOdo,
                timestamp = timestamp
            )
        }
    }

    fun deleteMaintenance(id: String) {
        viewModelScope.launch {
            maintenanceRepository.deleteMaintenance(id)
        }
    }
}
