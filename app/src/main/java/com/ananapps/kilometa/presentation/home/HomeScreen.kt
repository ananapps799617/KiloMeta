// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/HomeScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla principal y Cockpit de cabina para el conductor en KiloMeta.
// Integra el Semáforo Financiero dinámico, la tarjeta de Bolsa Semanal prorrateada,
// el badge táctil de sincronización en la nube (Cloud Sync) y modales de captura express.
// ==============================================================================

package com.ananapps.kilometa.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ananapps.kilometa.R
import com.ananapps.kilometa.presentation.common.CockpitCloudBadge
import com.ananapps.kilometa.presentation.home.components.CockpitBottomDock
import com.ananapps.kilometa.presentation.home.components.CockpitKpisCard
import com.ananapps.kilometa.presentation.home.components.HomeCockpitModals
import com.ananapps.kilometa.presentation.home.components.SemaphoreCard
import com.ananapps.kilometa.presentation.home.components.WeeklyQuotaCard
import com.ananapps.kilometa.presentation.sync.AuthBottomSheet
import com.ananapps.kilometa.presentation.sync.CloudSyncStatusSheet
import com.ananapps.kilometa.presentation.sync.HydrationRecoveryScreen
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

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

    if (showSyncSheet) {
        CloudSyncStatusSheet(
            sheetState = syncSheetState,
            syncStatus = syncStatus,
            userSession = userSession,
            onDismiss = { showSyncSheet = false },
            onSyncNow = { viewModel.syncNow() },
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

// ------------------------------------------------------------------------------
// BLOQUE 2: VISTA DE CABINA SIN TURNO ACTIVO
// ------------------------------------------------------------------------------
@Composable
private fun NoActiveShiftView(
    state: HomeUiState.NoActiveShift,
    onStartShiftClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WeeklyQuotaCard(progress = state.weeklyProgress)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Cabina en Espera", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Inicia tu jornada para registrar kilometraje y habilitar el Semáforo Financiero.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onStartShiftClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Iniciar Turno de Trabajo 🚀", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 3: VISTA DE CABINA CON TURNO ACTIVO
// ------------------------------------------------------------------------------
@Composable
private fun ActiveShiftCockpitView(state: HomeUiState.ActiveShift) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SemaphoreCard(status = state.status)
        WeeklyQuotaCard(progress = state.weeklyProgress)
        CockpitKpisCard(summary = state.summary, biFuelMetrics = state.biFuelMetrics)
        Spacer(modifier = Modifier.height(64.dp))
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 4: VISTA DE CONTINGENCIA STANDBY
// ------------------------------------------------------------------------------
@Composable
private fun StandbyPlaceholderView(state: HomeUiState.StandbyPlaceholder) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = state.notice, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            SemaphoreCard(status = state.status)
        }
    }
}
