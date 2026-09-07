// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/HomeScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla principal y Cockpit de cabina para el conductor en KiloMeta.
// Integra el Semáforo Financiero dinámico, la tarjeta de Bolsa Semanal prorrateada,
// el badge táctil de sincronización en la nube (Cloud Sync) y modales de captura express.
// ==============================================================================

package com.ananapps.kilometa.presentation.home

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ananapps.kilometa.R
import com.ananapps.kilometa.presentation.common.CockpitCloudBadge
import com.ananapps.kilometa.presentation.home.components.ActiveShiftCockpitView
import com.ananapps.kilometa.presentation.home.components.CockpitBottomDock
import com.ananapps.kilometa.presentation.home.components.HomeCockpitModals
import com.ananapps.kilometa.presentation.home.components.NoActiveShiftView
import com.ananapps.kilometa.presentation.home.components.StandbyPlaceholderView
import com.ananapps.kilometa.presentation.sync.AuthBottomSheet
import com.ananapps.kilometa.presentation.sync.CloudSyncStatusSheet
import com.ananapps.kilometa.presentation.sync.HydrationRecoveryScreen
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary

// ------------------------------------------------------------------------------
// BLOQUE 1: PANTALLA PRINCIPAL DE CABINA (HOME COCKPIT)
// ------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val userSession by viewModel.userSession.collectAsState()
    val restoreProgress by viewModel.restoreProgress.collectAsState()
    val isPingingCloud by viewModel.isPingingCloud.collectAsState()
    val connectionDiagnostic by viewModel.connectionDiagnostic.collectAsState()

    var showTripSheet by remember { mutableStateOf(false) }
    var showFuelingSheet by remember { mutableStateOf(false) }
    var showStartShiftDialog by remember { mutableStateOf(false) }
    var showCloseShiftDialog by remember { mutableStateOf(false) }
    var showSyncSheet by remember { mutableStateOf(false) }
    var showAuthSheet by remember { mutableStateOf(false) }
    var showRecoveryScreen by remember { mutableStateOf(false) }

    val tripSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fuelSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val syncSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val authSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showRecoveryScreen) {
        HydrationRecoveryScreen(
            progress = restoreProgress,
            onStartRecovery = { viewModel.startDisasterRecovery() },
            onBack = {
                viewModel.resetRecoveryProgress()
                showRecoveryScreen = false
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.status_offline_banner),
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusGreen
                        )
                    }
                },
                actions = {
                    CockpitCloudBadge(
                        syncStatus = syncStatus,
                        userSession = userSession,
                        onClick = { showSyncSheet = true },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    if (uiState is HomeUiState.ActiveShift) {
                        OutlinedButton(
                            onClick = { showCloseShiftDialog = true },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(text = "Cerrar", color = StatusRed, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepObsidianSurface)
            )
        },
        bottomBar = {
            if (uiState is HomeUiState.ActiveShift) {
                CockpitBottomDock(
                    onQuickTrip = { showTripSheet = true },
                    onQuickFueling = { showFuelingSheet = true }
                )
            }
        },
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
                    ActiveShiftCockpitView(state = state)
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
        showStartShiftDialog = showStartShiftDialog,
        onDismissStartShiftDialog = { showStartShiftDialog = false },
        showCloseShiftDialog = showCloseShiftDialog,
        onDismissCloseShiftDialog = { showCloseShiftDialog = false }
    )

    val syncPreferences by viewModel.syncPreferences.collectAsState()

    if (showSyncSheet) {
        CloudSyncStatusSheet(
            sheetState = syncSheetState,
            syncStatus = syncStatus,
            userSession = userSession,
            connectionDiagnostic = connectionDiagnostic,
            isPinging = isPingingCloud,
            syncPreferences = syncPreferences,
            onToggleAutoSync = { viewModel.onToggleAutoSync(it) },
            onSelectWifiOnly = { viewModel.onSelectNetworkPreference(it) },
            onDismiss = { showSyncSheet = false },
            onSyncNow = { viewModel.syncNow() },
            onTestConnection = { viewModel.onTestLiveConnection() },
            onOpenAuth = {
                showSyncSheet = false
                showAuthSheet = true
            },
            onOpenRecovery = {
                showSyncSheet = false
                showRecoveryScreen = true
            },
            onSignOut = { viewModel.signOut() }
        )
    }

    if (showAuthSheet) {
        AuthBottomSheet(
            sheetState = authSheetState,
            onDismiss = { showAuthSheet = false },
            onSignInWithGoogle = {
                viewModel.signInWithGoogle()
                showAuthSheet = false
            },
            onSignInWithEmail = { email, pass ->
                viewModel.signInWithEmail(email, pass)
                showAuthSheet = false
            },
            onSignInAnonymously = {
                viewModel.signInAnonymously()
                showAuthSheet = false
            }
        )
    }
}
