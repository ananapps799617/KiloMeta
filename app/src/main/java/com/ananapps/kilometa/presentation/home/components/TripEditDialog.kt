// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/TripEditDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo de edición de carreras en cabina (Orden #041).
// Redirige al formulario unificado RegisterTripDialog garantizando 100% de paridad simétrica.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.runtime.Composable
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.domain.model.VehicleProfile

@Composable
fun TripEditDialog(
    trip: Trip,
    onDismiss: () -> Unit,
    vehicleProfile: VehicleProfile = VehicleProfile(),
    onConfirm: (Trip) -> Unit
) {
    RegisterTripDialog(
        vehicleProfile = vehicleProfile,
        existingTrip = trip,
        onDismiss = onDismiss,
        onConfirmUpdate = onConfirm
    )
}
