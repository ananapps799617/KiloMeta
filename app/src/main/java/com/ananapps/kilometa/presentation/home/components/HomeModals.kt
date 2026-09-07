// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/HomeModals.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contenedor modular de diálogos y BottomSheets de captura express para HomeScreen.
// Desacopla la lógica modal de la cabina manteniendo los archivos < 280 líneas.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import com.ananapps.kilometa.presentation.home.HomeUiState
import com.ananapps.kilometa.presentation.home.HomeViewModel

// ------------------------------------------------------------------------------
// BLOQUE 1: ORQUESTADOR DE HOJAS MODALES Y DIÁLOGOS DE CABINA
// ------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCockpitModals(
    viewModel: HomeViewModel,
    uiState: HomeUiState,
    showTripSheet: Boolean,
    onDismissTripSheet: () -> Unit,
    tripSheetState: SheetState,
    showFuelingSheet: Boolean,
    onDismissFuelingSheet: () -> Unit,
    fuelSheetState: SheetState,
    showStartShiftDialog: Boolean,
    onDismissStartShiftDialog: () -> Unit,
    showCloseShiftDialog: Boolean,
    onDismissCloseShiftDialog: () -> Unit
) {
    if (showTripSheet) {
        RegisterTripSheet(
            sheetState = tripSheetState,
            onDismiss = onDismissTripSheet,
            onConfirmTrip = { platform, gross, commission, payment ->
                viewModel.registerTrip(platform, gross, commission, payment)
            }
        )
    }

    if (showFuelingSheet) {
        val currentOdo = (uiState as? HomeUiState.ActiveShift)?.summary?.startOdometer ?: 100_000
        RegisterFuelingSheet(
            currentOdometer = currentOdo,
            sheetState = fuelSheetState,
            onDismiss = onDismissFuelingSheet,
            onConfirmFueling = { fuelType, amount, volume, odo ->
                viewModel.registerFueling(fuelType, amount, volume, odo)
            }
        )
    }

    if (showStartShiftDialog) {
        StartShiftDialog(
            initialOdometer = 100_000,
            onDismiss = onDismissStartShiftDialog,
            onConfirm = { startOdo ->
                viewModel.startShift(startOdometer = startOdo)
                onDismissStartShiftDialog()
            }
        )
    }

    if (showCloseShiftDialog) {
        val startOdo = (uiState as? HomeUiState.ActiveShift)?.summary?.startOdometer ?: 100_000
        CloseShiftDialog(
            startOdometer = startOdo,
            onDismiss = onDismissCloseShiftDialog,
            onConfirm = { endOdo ->
                viewModel.closeShift(endOdometer = endOdo)
                onDismissCloseShiftDialog()
            }
        )
    }
}
