// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/FuelingEditDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo de edición de tanqueo de combustible (Orden #041).
// Redirige al formulario unificado RegisterFuelingDialog garantizando 100% de paridad simétrica.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.runtime.Composable
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.Place
import com.ananapps.kilometa.domain.model.PropulsionType

@Composable
fun FuelingEditDialog(
    fueling: Fueling,
    onDismiss: () -> Unit,
    savedPlaces: List<Place> = emptyList(),
    onConfirm: (Fueling) -> Unit
) {
    RegisterFuelingDialog(
        currentOdometer = fueling.odometer,
        onDismiss = onDismiss,
        propulsionType = PropulsionType.BI_FUEL,
        savedPlaces = savedPlaces,
        existingFueling = fueling,
        onConfirmUpdate = onConfirm
    )
}
