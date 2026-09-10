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
    showExpenseSheet: Boolean = false,
    onDismissExpenseSheet: () -> Unit = {},
    expenseSheetState: SheetState? = null,
    isSporadic: Boolean = false,
    showStartShiftDialog: Boolean,
    onDismissStartShiftDialog: () -> Unit,
    onStartShiftConfirmed: () -> Unit = {},
    showCloseShiftDialog: Boolean,
    onDismissCloseShiftDialog: () -> Unit,
    showShiftPromptDialog: Boolean = false,
    promptActionName: String? = null,
    onDismissShiftPrompt: () -> Unit = {},
    onStartShiftFromPrompt: () -> Unit = {},
    onSporadicFromPrompt: () -> Unit = {}
) {
    if (showShiftPromptDialog && promptActionName != null) {
        ShiftPromptDialog(
            actionName = promptActionName,
            onDismiss = onDismissShiftPrompt,
            onStartShiftSelected = onStartShiftFromPrompt,
            onSporadicSelected = onSporadicFromPrompt
        )
    }
    if (showTripSheet) {
        RegisterTripSheet(
            sheetState = tripSheetState,
            onDismiss = onDismissTripSheet,
            onConfirmTrip = { platform, gross, commission, payment, destination ->
                viewModel.registerTrip(platform, gross, commission, payment, destination, isSporadic = isSporadic)
            }
        )
    }

    if (showFuelingSheet) {
        val currentOdo = (uiState as? HomeUiState.ActiveShift)?.summary?.startOdometer ?: 100_000
        val profile = viewModel.driverProfile.value
        RegisterFuelingSheet(
            currentOdometer = currentOdo,
            sheetState = fuelSheetState,
            onDismiss = onDismissFuelingSheet,
            propulsionType = profile.propulsionType,
            onConfirmFueling = { fuelType, amount, volume, odo ->
                viewModel.registerFueling(fuelType, amount, volume, odo, isSporadic = isSporadic)
            }
        )
    }

    if (showExpenseSheet && expenseSheetState != null) {
        RegisterExpenseSheet(
            sheetState = expenseSheetState,
            onDismiss = onDismissExpenseSheet,
            onConfirmExpense = { concept, amount ->
                viewModel.registerExpense(concept, amount, isSporadic = isSporadic)
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
                onStartShiftConfirmed()
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
