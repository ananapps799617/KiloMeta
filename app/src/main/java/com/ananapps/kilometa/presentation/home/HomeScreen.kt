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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ananapps.kilometa.presentation.home.components.ActiveShiftCockpitView
import com.ananapps.kilometa.presentation.home.components.HomeCockpitModals
import com.ananapps.kilometa.presentation.home.components.NoActiveShiftView
import com.ananapps.kilometa.presentation.home.components.StandbyPlaceholderView
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
    triggerCloseShift: Boolean = false,
    onResetCloseShiftTrigger: () -> Unit = {},
    triggerQuickTrip: Boolean = false,
    onResetQuickTripTrigger: () -> Unit = {},
    triggerQuickFueling: Boolean = false,
    onResetQuickFuelingTrigger: () -> Unit = {},
    triggerQuickExpense: Boolean = false,
    onResetQuickExpenseTrigger: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Solicitud elegante de permisos de notificación vehicular en Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> /* Degradación elegante: KiloMeta sigue operando localmente */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var showTripSheet by remember { mutableStateOf(false) }
    var showFuelingSheet by remember { mutableStateOf(false) }
    var showExpenseSheet by remember { mutableStateOf(false) }
    var showStartShiftDialog by remember { mutableStateOf(false) }
    var showCloseShiftDialog by remember { mutableStateOf(false) }
    var showShiftPromptDialog by remember { mutableStateOf(false) }
    var pendingPromptAction by remember { mutableStateOf<String?>(null) }
    var isSporadicAction by remember { mutableStateOf(false) }

    LaunchedEffect(triggerCloseShift) {
        if (triggerCloseShift) {
            showCloseShiftDialog = true
            onResetCloseShiftTrigger()
        }
    }

    LaunchedEffect(triggerQuickTrip) {
        if (triggerQuickTrip) {
            if (uiState is HomeUiState.ActiveShift) {
                isSporadicAction = false
                showTripSheet = true
            } else {
                pendingPromptAction = "carrera"
                showShiftPromptDialog = true
            }
            onResetQuickTripTrigger()
        }
    }

    LaunchedEffect(triggerQuickFueling) {
        if (triggerQuickFueling) {
            if (uiState is HomeUiState.ActiveShift) {
                isSporadicAction = false
                showFuelingSheet = true
            } else {
                pendingPromptAction = "tanqueo"
                showShiftPromptDialog = true
            }
            onResetQuickFuelingTrigger()
        }
    }

    LaunchedEffect(triggerQuickExpense) {
        if (triggerQuickExpense) {
            if (uiState is HomeUiState.ActiveShift) {
                isSporadicAction = false
                showExpenseSheet = true
            } else {
                pendingPromptAction = "gasto"
                showShiftPromptDialog = true
            }
            onResetQuickExpenseTrigger()
        }
    }

    val tripSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fuelSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val expenseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                is HomeUiState.NoActiveShift -> {
                    NoActiveShiftView(
                        state = state,
                        onStartShiftClick = { showStartShiftDialog = true }
                    )
                }
                is HomeUiState.ActiveShift -> {
                    ActiveShiftCockpitView(
                        state = state,
                        onDeleteTrip = { tripId -> viewModel.deleteTrip(tripId) },
                        onCloseShiftClick = { showCloseShiftDialog = true }
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
        onDismissTripSheet = { showTripSheet = false; isSporadicAction = false },
        tripSheetState = tripSheetState,
        showFuelingSheet = showFuelingSheet,
        onDismissFuelingSheet = { showFuelingSheet = false; isSporadicAction = false },
        fuelSheetState = fuelSheetState,
        showExpenseSheet = showExpenseSheet,
        onDismissExpenseSheet = { showExpenseSheet = false; isSporadicAction = false },
        expenseSheetState = expenseSheetState,
        isSporadic = isSporadicAction,
        showStartShiftDialog = showStartShiftDialog,
        onDismissStartShiftDialog = { showStartShiftDialog = false },
        onStartShiftConfirmed = {
            isSporadicAction = false
            when (pendingPromptAction) {
                "carrera" -> showTripSheet = true
                "tanqueo" -> showFuelingSheet = true
                "gasto" -> showExpenseSheet = true
            }
            pendingPromptAction = null
        },
        showCloseShiftDialog = showCloseShiftDialog,
        onDismissCloseShiftDialog = { showCloseShiftDialog = false },
        showShiftPromptDialog = showShiftPromptDialog,
        promptActionName = pendingPromptAction,
        onDismissShiftPrompt = {
            showShiftPromptDialog = false
            pendingPromptAction = null
        },
        onStartShiftFromPrompt = {
            showShiftPromptDialog = false
            showStartShiftDialog = true
        },
        onSporadicFromPrompt = {
            showShiftPromptDialog = false
            isSporadicAction = true
            when (pendingPromptAction) {
                "carrera" -> showTripSheet = true
                "tanqueo" -> showFuelingSheet = true
                "gasto" -> showExpenseSheet = true
            }
            pendingPromptAction = null
        }
    )
}
