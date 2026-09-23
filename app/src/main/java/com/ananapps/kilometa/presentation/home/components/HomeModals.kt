// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/HomeModals.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Orquestador modular de diálogos flotantes centrados de 3 capas para HomeScreen (Orden #037).
// Erradica los SheetStates y gestiona los diálogos de captura express y turnos (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ananapps.kilometa.presentation.home.HomeUiState
import com.ananapps.kilometa.presentation.home.HomeViewModel

@Composable
fun HomeCockpitModals(
    viewModel: HomeViewModel,
    uiState: HomeUiState,
    showTripDialog: Boolean,
    onDismissTripDialog: () -> Unit,
    showFuelingDialog: Boolean,
    onDismissFuelingDialog: () -> Unit,
    showExpenseDialog: Boolean = false,
    onDismissExpenseDialog: () -> Unit = {},
    showTransferDialog: Boolean = false,
    onDismissTransferDialog: () -> Unit = {},
    showMaintenanceDialog: Boolean = false,
    onDismissMaintenanceDialog: () -> Unit = {},
    isSporadic: Boolean = false,
    showStartShiftDialog: Boolean = false,
    onDismissStartShiftDialog: () -> Unit = {},
    onStartShiftConfirmed: () -> Unit = {},
    showCloseShiftDialog: Boolean = false,
    onDismissCloseShiftDialog: () -> Unit = {},
    showShiftPromptDialog: Boolean = false,
    promptActionName: String? = null,
    onDismissShiftPrompt: () -> Unit = {},
    onStartShiftFromPrompt: () -> Unit = {},
    onSporadicFromPrompt: () -> Unit = {},
    lastKnownOdometer: Int = 0,
    dayStartOdometer: Int? = null
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

    if (showTripDialog) {
        val vehicleProfile by viewModel.vehicleProfile.collectAsState()
        val scannedReceipt by viewModel.scannedReceipt.collectAsState()
        RegisterTripDialog(
            vehicleProfile = vehicleProfile,
            lastKnownOdometer = resolvedOdo,
            scannedReceipt = scannedReceipt,
            onDismiss = {
                viewModel.clearScannedReceipt()
                onDismissTripDialog()
            },
            onConfirmTrip = { platform, gross, commission, payment, destination, timestamp, startOdo, endOdo, distKm, durMin, promo ->
                viewModel.clearScannedReceipt()
                viewModel.registerTrip(
                    platform = platform,
                    gross = gross,
                    commission = commission,
                    payment = payment,
                    dest = destination,
                    timestamp = timestamp,
                    startOdometer = startOdo,
                    endOdometer = endOdo,
                    distanceKm = distKm,
                    durationMinutes = durMin,
                    platformPromoDiscount = promo,
                    isSporadic = isSporadic
                )
            }
        )
    }

    val savedPlaces by viewModel.savedPlaces.collectAsState()

    if (showFuelingDialog) {
        val profile = viewModel.driverProfile.value
        RegisterFuelingDialog(
            currentOdometer = resolvedOdo,
            onDismiss = onDismissFuelingDialog,
            propulsionType = profile.propulsionType,
            savedPlaces = savedPlaces,
            onUpdateStationPrice = { id, type, price -> viewModel.updateStationPrice(id, type, price) },
            onConfirmFueling = { fuelType, amount, volume, odo, paymentSource, timestamp ->
                viewModel.registerFueling(fuelType, amount, volume, odo, isSporadic = isSporadic, paymentSource = paymentSource, timestamp = timestamp)
            }
        )
    }

    if (showExpenseDialog) {
        RegisterExpenseDialog(
            onDismiss = onDismissExpenseDialog,
            savedPlaces = savedPlaces,
            onConfirmExpense = { concept, amount, paymentSource, timestamp ->
                viewModel.registerExpense(concept, amount, isSporadic = isSporadic, paymentSource = paymentSource, timestamp = timestamp)
            }
        )
    }

    if (showTransferDialog) {
        TransferBetweenWalletsDialog(
            onDismiss = onDismissTransferDialog,
            onConfirmTransfer = { source, destination, amount, type, notes ->
                viewModel.registerWalletTransfer(source, destination, amount, type, notes)
            }
        )
    }

    if (showMaintenanceDialog) {
        val vehicleProfile by viewModel.vehicleProfile.collectAsState()
        RegisterMaintenanceDialog(
            vehicleProfile = vehicleProfile,
            lastKnownOdometer = resolvedOdo,
            onDismiss = onDismissMaintenanceDialog,
            savedPlaces = savedPlaces,
            onConfirmMaintenance = { type, odo, cost, notes, paymentSource, title, location, timestamp ->
                viewModel.registerMaintenance(
                    type = type,
                    odometer = odo,
                    costCop = cost,
                    notes = notes,
                    paymentSource = paymentSource,
                    serviceTitle = title,
                    location = location,
                    timestamp = timestamp
                )
            }
        )
    }

    if (showStartShiftDialog) {
        StartShiftDialog(
            initialOdometer = resolvedOdo,
            onDismiss = onDismissStartShiftDialog,
            onConfirm = { startOdo, startTime ->
                viewModel.startShift(startOdometer = startOdo, startTime = startTime)
                onDismissStartShiftDialog()
                onStartShiftConfirmed()
            }
        )
    }

    if (showCloseShiftDialog) {
        val startOdo = dayStartOdometer?.takeIf { it > 0 }
            ?: (uiState as? HomeUiState.ActiveShift)?.summary?.startOdometer?.takeIf { it > 0 }
            ?: resolvedOdo
        CloseShiftDialog(
            startOdometer = startOdo,
            onDismiss = onDismissCloseShiftDialog,
            onConfirm = { endOdo, endTime ->
                viewModel.closeShift(endOdometer = endOdo, endTime = endTime)
                onDismissCloseShiftDialog()
            }
        )
    }
}
