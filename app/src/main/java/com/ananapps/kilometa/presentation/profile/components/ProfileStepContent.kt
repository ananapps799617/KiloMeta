// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/ProfileStepContent.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contenedor del contenido del paso activo (0: Conductor, 1: Vehículo, 2: Meta).
// Consume DriverProfileFormState para delegar limpiamente en los 3 subcomponentes.
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface

@Composable
fun ProfileStepContent(
    step: Int,
    state: DriverProfileFormState,
    email: String,
    onRequestPasswordReset: () -> Unit,
    onResetData: () -> Unit,
    isOnboardingMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            when (step) {
                0 -> OnboardingPersonalStep(
                    driverName = state.driverName,
                    onDriverNameChange = { state.driverName = it },
                    email = email,
                    phone = state.phone,
                    onPhoneChange = { state.phone = it },
                    gender = state.gender,
                    onGenderChange = { state.gender = it },
                    onRequestPasswordReset = onRequestPasswordReset,
                    isOnboardingMode = isOnboardingMode
                )
                1 -> OnboardingVehicleStep(
                    vehiclePlate = state.vehiclePlate,
                    onVehiclePlateChange = { state.vehiclePlate = it },
                    vehicleType = state.vehicleType,
                    onVehicleTypeChange = { state.vehicleType = it },
                    tankSystem = state.tankSystem,
                    onTankSystemChange = { state.tankSystem = it },
                    brand = state.brand,
                    onBrandChange = { state.brand = it },
                    model = state.model,
                    onModelChange = { state.model = it },
                    propulsionType = state.propulsionType,
                    onPropulsionChange = { state.propulsionType = it },
                    tenureType = state.tenureType,
                    onTenureChange = { state.tenureType = it },
                    initialOdometer = state.initialOdometer,
                    onInitialOdometerChange = { state.initialOdometer = it }
                )
                else -> OnboardingFinancialStep(
                    tenureType = state.tenureType,
                    quotaAmount = state.quotaAmount,
                    onQuotaAmountChange = { state.quotaAmount = it },
                    quotaPeriodicity = state.quotaPeriodicity,
                    onQuotaPeriodicityChange = { state.quotaPeriodicity = it },
                    workDaysWeek = state.workDaysWeek,
                    onWorkDaysWeekChange = { state.workDaysWeek = it },
                    quotaCutoffDay = state.quotaCutoffDay,
                    onQuotaCutoffDayChange = { state.quotaCutoffDay = it },
                    quotaCutoffHour = state.quotaCutoffHour,
                    onQuotaCutoffHourChange = { state.quotaCutoffHour = it },
                    userEmail = email,
                    onResetData = onResetData
                )
            }
        }
    }
}
