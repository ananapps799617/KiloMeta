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
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.usecase.GetMaintenanceSummaryUseCase
import com.ananapps.kilometa.domain.usecase.GetVehicleMaintenanceAlertsUseCase
import com.ananapps.kilometa.domain.usecase.RegisterMaintenanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val shiftRepository: ShiftRepository,
    private val calculateDailyQuotaUseCase: com.ananapps.kilometa.domain.usecase.CalculateDailyQuotaUseCase
) : ViewModel() {

    private val _currentOdo = MutableStateFlow(100_000)

    val uiState: StateFlow<MaintenanceUiState> = combine(
        getMaintenanceSummaryUseCase(),
        driverProfileRepository.getProfile(),
        shiftRepository.getActiveShift(),
        _currentOdo
    ) { dashboard: com.ananapps.kilometa.domain.usecase.MaintenanceDashboardData, profile: com.ananapps.kilometa.domain.model.DriverProfile, activeShift: com.ananapps.kilometa.domain.model.Shift?, manualOdo: Int ->
        val effectiveOdo = activeShift?.startOdometer ?: manualOdo
        val alerts = getVehicleMaintenanceAlertsUseCase(effectiveOdo)
        val quotaProgress = calculateDailyQuotaUseCase()

        MaintenanceUiState(
            isLoading = false,
            records = dashboard.records,
            alerts = alerts,
            totalInvestmentCop = dashboard.totalInvestmentCop,
            currentOdometer = effectiveOdo,
            vehiclePlate = profile.vehiclePlate,
            weeklyQuotaProgress = quotaProgress
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), MaintenanceUiState())

    fun registerMaintenance(
        type: MaintenanceType,
        odometer: Int,
        costCop: Double,
        notes: String? = null,
        customNextDueOdo: Int? = null
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
                customNextDueOdo = customNextDueOdo
            )
            _currentOdo.value = odometer
        }
    }

    fun deleteMaintenance(id: String) {
        viewModelScope.launch {
            maintenanceRepository.deleteMaintenance(id)
        }
    }
}
