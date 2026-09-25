// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/DriverProfileFormState.kt
// DESCRIPCIÓN PEDAGÓGICA:
// State Holder para el formulario de perfil y vehículo en Compose.
// Encapsula los 19 parámetros del modelo de dominio con tipado estricto y Null-Safety.
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ananapps.kilometa.domain.model.DriverProfile
import com.ananapps.kilometa.domain.model.Gender
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.QuotaPeriodicity
import com.ananapps.kilometa.domain.model.TankSystem
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.domain.model.VehicleTenure
import com.ananapps.kilometa.domain.model.VehicleType

class DriverProfileFormState(
    initialDriver: DriverProfile,
    initialVehicle: VehicleProfile
) {
    var driverName by mutableStateOf(initialDriver.driverName)
    var phone by mutableStateOf(initialDriver.phone)
    var gender by mutableStateOf(initialDriver.gender)

    var vehiclePlate by mutableStateOf(initialVehicle.vehiclePlate)
    var vehicleType by mutableStateOf(initialVehicle.vehicleType)
    var tankSystem by mutableStateOf(initialVehicle.tankSystem)
    var brand by mutableStateOf(initialVehicle.brand)
    var model by mutableStateOf(initialVehicle.model)
    var propulsionType by mutableStateOf(initialVehicle.propulsionType)
    var tenureType by mutableStateOf(initialVehicle.tenureType)
    var initialOdometer by mutableIntStateOf(initialVehicle.initialOdometer)

    var quotaAmount by mutableDoubleStateOf(initialVehicle.quotaAmount)
    var quotaPeriodicity by mutableStateOf(initialVehicle.quotaPeriodicity)
    var workDaysWeek by mutableIntStateOf(initialVehicle.workDaysWeek)
    var quotaCutoffDay by mutableIntStateOf(initialVehicle.quotaCutoffDay)
    var quotaCutoffHour by mutableIntStateOf(initialVehicle.quotaCutoffHour)

    fun toUpdatedDriver(base: DriverProfile): DriverProfile = base.copy(
        driverName = driverName.trim(),
        phone = phone.trim(),
        gender = gender,
        vehiclePlate = vehiclePlate.trim().uppercase(),
        propulsionType = propulsionType,
        isProfileCompleted = true
    )

    fun toUpdatedVehicle(base: VehicleProfile): VehicleProfile = base.copy(
        vehiclePlate = vehiclePlate.trim().uppercase(),
        vehicleType = vehicleType,
        tankSystem = tankSystem,
        brand = brand.trim(),
        model = model.trim(),
        propulsionType = propulsionType,
        tenureType = tenureType,
        quotaAmount = quotaAmount,
        quotaPeriodicity = quotaPeriodicity,
        workDaysWeek = workDaysWeek,
        quotaCutoffDay = quotaCutoffDay,
        quotaCutoffHour = quotaCutoffHour,
        initialOdometer = initialOdometer
    )
}

@Composable
fun rememberDriverProfileFormState(
    initialDriver: DriverProfile,
    initialVehicle: VehicleProfile
): DriverProfileFormState {
    return remember(initialDriver.id, initialVehicle.id) {
        DriverProfileFormState(initialDriver, initialVehicle)
    }
}
