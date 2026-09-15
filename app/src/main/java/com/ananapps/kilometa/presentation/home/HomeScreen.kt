// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/HomeScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla principal y Cockpit de cabina para el conductor en KiloMeta.
// Integra el Semáforo Financiero dinámico, la tarjeta de Bolsa Semanal prorrateada,
// el badge táctil de sincronización en la nube (Cloud Sync) y modales de captura express.
// ==============================================================================

package com.ananapps.kilometa.presentation.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.presentation.home.components.ContinuousTimelineCockpitView
import com.ananapps.kilometa.presentation.home.components.ExpenseEditDialog
import com.ananapps.kilometa.presentation.home.components.FuelingEditDialog
import com.ananapps.kilometa.presentation.home.components.HomeCockpitModals
import com.ananapps.kilometa.presentation.home.components.StandbyPlaceholderView
import com.ananapps.kilometa.presentation.home.components.TripEditDialog
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary

// ------------------------------------------------------------------------------
// BLOQUE 1: PANTALLA PRINCIPAL DE CABINA (HOME COCKPIT)
// ------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    showTopBar: Boolean = false,
    triggerQuickTrip: Boolean = false, onResetQuickTripTrigger: () -> Unit = {},
    triggerQuickFueling: Boolean = false, onResetQuickFuelingTrigger: () -> Unit = {},
    triggerQuickExpense: Boolean = false, onResetQuickExpenseTrigger: () -> Unit = {},
    triggerQuickMaintenance: Boolean = false, onResetQuickMaintenanceTrigger: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val vehicle by viewModel.vehicleProfile.collectAsState()
    val lastKnownOdometer by viewModel.lastKnownOdometer.collectAsState()
    val dayContinuousTimeline by viewModel.dayContinuousTimeline.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> /* Degradación elegante */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var showTripSheet by remember { mutableStateOf(false) }
    var showFuelingSheet by remember { mutableStateOf(false) }
    var showExpenseSheet by remember { mutableStateOf(false) }
    var showMaintenanceSheet by remember { mutableStateOf(false) }
    var showTransferSheet by remember { mutableStateOf(false) }
    var editingTrip by remember { mutableStateOf<Trip?>(null) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var editingFueling by remember { mutableStateOf<Fueling?>(null) }

    LaunchedEffect(triggerQuickTrip) { if (triggerQuickTrip) { showTripSheet = true; onResetQuickTripTrigger() } }
    LaunchedEffect(triggerQuickFueling) { if (triggerQuickFueling) { showFuelingSheet = true; onResetQuickFuelingTrigger() } }
    LaunchedEffect(triggerQuickExpense) { if (triggerQuickExpense) { showExpenseSheet = true; onResetQuickExpenseTrigger() } }
    LaunchedEffect(triggerQuickMaintenance) { if (triggerQuickMaintenance) { showMaintenanceSheet = true; onResetQuickMaintenanceTrigger() } }

    val tripSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { false })
    val fuelSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { false })
    val expenseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { false })
    val maintenanceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { false })
    val transferSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true, confirmValueChange = { false })

    Scaffold(
        containerColor = DeepObsidianBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ElectricBluePrimary)
                    }
                }
                is HomeUiState.NoActiveShift, is HomeUiState.ActiveShift -> {
                    val cashSettlement = (state as? HomeUiState.ActiveShift)?.cashSettlement
                        ?: dayContinuousTimeline.cashSettlement
                    val weeklyProgress = when (state) {
                        is HomeUiState.NoActiveShift -> state.weeklyProgress
                        is HomeUiState.ActiveShift -> state.weeklyProgress
                        else -> null
                    }
                    val auditSummary = when (state) {
                        is HomeUiState.NoActiveShift -> state.accountingAuditSummary
                        is HomeUiState.ActiveShift -> state.accountingAuditSummary
                        else -> null
                    }
                    ContinuousTimelineCockpitView(
                        vehicle = vehicle,
                        lastKnownOdometer = lastKnownOdometer,
                        timelineSummary = dayContinuousTimeline,
                        weeklyProgress = weeklyProgress,
                        auditSummary = auditSummary,
                        cashSettlement = cashSettlement,
                        onDeleteTrip = { tripId -> viewModel.deleteTrip(tripId) },
                        onDeleteExpense = { expenseId -> viewModel.deleteExpense(expenseId) },
                        onDeleteFueling = { fuelingId -> viewModel.deleteFueling(fuelingId) },
                        onDeleteTransfer = { transferId -> viewModel.deleteWalletTransfer(transferId) },
                        onDeleteMaintenance = { maintenanceId -> viewModel.deleteMaintenance(maintenanceId) },
                        onEditTrip = { editingTrip = it },
                        onEditExpense = { editingExpense = it },
                        onEditFueling = { editingFueling = it },
                        onTransferClick = { showTransferSheet = true }
                    )
                }
                is HomeUiState.StandbyPlaceholder -> {
                    StandbyPlaceholderView(state = state)
                }
            }
        }
    }

    HomeCockpitModals(
        viewModel = viewModel,
        uiState = uiState,
        showTripSheet = showTripSheet,
        onDismissTripSheet = { showTripSheet = false },
        tripSheetState = tripSheetState,
        showFuelingSheet = showFuelingSheet,
        onDismissFuelingSheet = { showFuelingSheet = false },
        fuelSheetState = fuelSheetState,
        showExpenseSheet = showExpenseSheet,
        onDismissExpenseSheet = { showExpenseSheet = false },
        expenseSheetState = expenseSheetState,
        showMaintenanceSheet = showMaintenanceSheet,
        onDismissMaintenanceSheet = { showMaintenanceSheet = false },
        maintenanceSheetState = maintenanceSheetState,
        showTransferSheet = showTransferSheet,
        onDismissTransferSheet = { showTransferSheet = false },
        transferSheetState = transferSheetState,
        showStartShiftDialog = false,
        onDismissStartShiftDialog = {},
        showCloseShiftDialog = false,
        onDismissCloseShiftDialog = {},
        lastKnownOdometer = lastKnownOdometer
    )

    editingTrip?.let { trip ->
        TripEditDialog(
            trip = trip,
            onDismiss = { editingTrip = null },
            onConfirm = { updated ->
                viewModel.updateTrip(updated)
                editingTrip = null
            }
        )
    }

    editingExpense?.let { expense ->
        ExpenseEditDialog(
            expense = expense,
            onDismiss = { editingExpense = null },
            onConfirm = { updated ->
                viewModel.updateExpense(updated)
                editingExpense = null
            }
        )
    }

    editingFueling?.let { fueling ->
        FuelingEditDialog(
            fueling = fueling,
            onDismiss = { editingFueling = null },
            onConfirm = { updated ->
                viewModel.updateFueling(updated)
                editingFueling = null
            }
        )
    }
}
