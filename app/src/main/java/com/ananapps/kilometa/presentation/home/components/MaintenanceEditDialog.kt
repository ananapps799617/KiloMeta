// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/MaintenanceEditDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo de edición de servicios y mantenimientos en cabina (Orden #041).
// Redirige al formulario unificado RegisterMaintenanceDialog garantizando 100% de paridad simétrica.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.runtime.Composable
import com.ananapps.kilometa.domain.model.MaintenanceRecord
import com.ananapps.kilometa.domain.model.Place
import com.ananapps.kilometa.domain.model.VehicleProfile

@Composable
fun MaintenanceEditDialog(
    maintenance: MaintenanceRecord,
    vehicleProfile: VehicleProfile = VehicleProfile(),
    onDismiss: () -> Unit,
    savedPlaces: List<Place> = emptyList(),
    onConfirm: (MaintenanceRecord) -> Unit
) {
    RegisterMaintenanceDialog(
        vehicleProfile = vehicleProfile,
        lastKnownOdometer = maintenance.odometer,
        existingMaintenance = maintenance,
        savedPlaces = savedPlaces,
        onDismiss = onDismiss,
        onConfirmUpdate = onConfirm
    )
}
