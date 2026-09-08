// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/CalculateDynamicQuotaUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para calcular la cuota diaria exigida para un vehículo arrendado
// en función de los días de trabajo efectivo configurados a la semana.
// Fórmula canónica: Cuota Diaria = Monto Total / Días Laborados.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.domain.model.VehicleTenure
import javax.inject.Inject

class CalculateDynamicQuotaUseCase @Inject constructor() {

    operator fun invoke(vehicle: VehicleProfile): Double {
        if (vehicle.quotaAmount <= 0.0) return 0.0
        val effectiveDays = vehicle.workDaysWeek.coerceIn(1, 7)

        return if (vehicle.tenureType == VehicleTenure.RENTED) {
            // En arriendo: el valor se prorratea estrictamente entre los días laborados
            vehicle.quotaAmount / effectiveDays
        } else {
            // En propio: meta de ahorro/cuota aspiracional diaria
            vehicle.quotaAmount / effectiveDays
        }
    }
}
