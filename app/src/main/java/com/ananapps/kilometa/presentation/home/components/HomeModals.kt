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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    showTransferSheet: Boolean = false,
    onDismissTransferSheet: () -> Unit = {},
    transferSheetState: SheetState? = null,
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
    onSporadicFromPrompt: () -> Unit = {},
    lastKnownOdometer: Int = 0
) {
    val resolvedOdo = if (lastKnownOdometer > 0) {
        lastKnownOdometer
    } else {
        (uiState as? HomeUiState.ActiveShift)?.summary?.startOdometer ?: 0
    }

    if (showShiftPromptDialog && promptActionName != null) {
        ShiftPromptDialog(
            actionName = promptActionName,
            onDismiss = onDismissShiftPrompt,
            onStartShiftSelected = onStartShiftFromPrompt,
            onSporadicSelected = onSporadicFromPrompt
        )
    }
    if (showTripSheet) {
        val vehicleProfile by viewModel.vehicleProfile.collectAsState()
        RegisterTripSheet(
            sheetState = tripSheetState,
            vehicleProfile = vehicleProfile,
            lastKnownOdometer = resolvedOdo,
            onDismiss = onDismissTripSheet,
            onConfirmTrip = { platform, gross, commission, payment, destination, timestamp, startOdo, endOdo ->
                viewModel.registerTrip(
                    platform = platform,
                    gross = gross,
                    commission = commission,
                    payment = payment,
                    dest = destination,
                    timestamp = timestamp,
                    startOdometer = startOdo,
                    endOdometer = endOdo,
                    isSporadic = isSporadic
                )
            }
        )
    }

    if (showFuelingSheet) {
        val profile = viewModel.driverProfile.value
        RegisterFuelingSheet(
            currentOdometer = resolvedOdo,
            sheetState = fuelSheetState,
            onDismiss = onDismissFuelingSheet,
            propulsionType = profile.propulsionType,
            onConfirmFueling = { fuelType, amount, volume, odo, paymentSource ->
                viewModel.registerFueling(fuelType, amount, volume, odo, isSporadic = isSporadic, paymentSource = paymentSource)
            }
        )
    }

    if (showExpenseSheet && expenseSheetState != null) {
        RegisterExpenseSheet(
            sheetState = expenseSheetState,
            onDismiss = onDismissExpenseSheet,
            onConfirmExpense = { concept, amount, paymentSource ->
                viewModel.registerExpense(concept, amount, isSporadic = isSporadic, paymentSource = paymentSource)
            }
        )
    }

    if (showTransferSheet && transferSheetState != null) {
        TransferBetweenWalletsSheet(
            sheetState = transferSheetState,
            onDismiss = onDismissTransferSheet,
            onConfirmTransfer = { source, destination, amount, type, notes ->
                viewModel.registerWalletTransfer(source, destination, amount, type, notes)
            }
        )
    }

    if (showStartShiftDialog) {
        StartShiftDialog(
            initialOdometer = resolvedOdo,
            onDismiss = onDismissStartShiftDialog,
            onConfirm = { startOdo ->
                viewModel.startShift(startOdometer = startOdo)
                onDismissStartShiftDialog()
                onStartShiftConfirmed()
            }
        )
    }

    if (showCloseShiftDialog) {
        val startOdo = (uiState as? HomeUiState.ActiveShift)?.summary?.startOdometer ?: resolvedOdo
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
