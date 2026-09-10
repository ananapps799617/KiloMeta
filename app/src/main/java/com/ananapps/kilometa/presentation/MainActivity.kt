// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/MainActivity.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Actividad principal única (Single Activity Architecture) de KiloMeta.
// Gestiona el flujo de autenticación obligatoria (LoginScreen) y la navegación
// de cabina mediante Menú Lateral Hamburguesa (KiloMetaDrawer) y TopBar descongestionada.
// ==============================================================================

package com.ananapps.kilometa.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ananapps.kilometa.presentation.auth.LoginScreen
import com.ananapps.kilometa.presentation.expenses.ExpensesScreen
import com.ananapps.kilometa.presentation.history.HistoryScreen
import com.ananapps.kilometa.presentation.home.HomeScreen
import com.ananapps.kilometa.presentation.home.HomeViewModel
import com.ananapps.kilometa.presentation.maintenance.MaintenanceScreen
import com.ananapps.kilometa.presentation.navigation.AppDestination
import com.ananapps.kilometa.presentation.navigation.KiloMetaDrawerContent
import com.ananapps.kilometa.presentation.navigation.KiloMetaTopBar
import com.ananapps.kilometa.presentation.profile.DriverProfileScreen
import com.ananapps.kilometa.presentation.sync.CloudSyncStatusSheet
import com.ananapps.kilometa.presentation.sync.HydrationRecoveryScreen
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.KiloMetaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// ------------------------------------------------------------------------------
// BLOQUE 1: ACTIVIDAD PRINCIPAL DE CABINA
// ------------------------------------------------------------------------------
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KiloMetaTheme {
                MainAppHost()
            }
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: HOST DE APLICACIÓN CON AUTH Y MODAL NAVIGATION DRAWER
// ------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppHost(
    viewModel: HomeViewModel = viewModel()
) {
    val userSession by viewModel.userSession.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()

    // 1. Bloqueo de cabina: Si no hay sesión autenticada, se exige inicio de sesión
    if (userSession == null) {
        LoginScreen(
            onSignInWithGoogleToken = { token ->
                viewModel.signInWithGoogle(token)
            },
            onGoogleSignInError = { errorMsg ->
                viewModel.setAuthError(errorMsg)
            },
            onSignInWithEmail = { email, pass ->
                viewModel.signInWithEmail(email, pass)
            },
            authError = authError,
            isLoading = isAuthLoading
        )
        return
    }

    // 2. Sesión activa: Cockpit vehicular con Hamburguesa / Drawer (100% alto disponible)
    val syncStatus by viewModel.syncStatus.collectAsState()
    val driverProfile by viewModel.driverProfile.collectAsState()
    val syncPreferences by viewModel.syncPreferences.collectAsState()
    val connectionDiagnostic by viewModel.connectionDiagnostic.collectAsState()
    val isPingingCloud by viewModel.isPingingCloud.collectAsState()
    val restoreProgress by viewModel.restoreProgress.collectAsState()

    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.COCKPIT) }
    var showSyncSheet by rememberSaveable { mutableStateOf(false) }
    var showRecoveryScreen by rememberSaveable { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val syncSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            KiloMetaDrawerContent(
                currentDestination = currentDestination,
                driverName = driverProfile.driverName.ifBlank { userSession?.displayName ?: "" },
                driverEmail = userSession?.email ?: "conductor@kilometa.co",
                vehiclePlate = driverProfile.vehiclePlate,
                propulsionName = driverProfile.propulsionType.displayName,
                onSelectDestination = { destination ->
                    if (destination == AppDestination.BACKUP) {
                        showSyncSheet = true
                    } else {
                        currentDestination = destination
                    }
                },
                onSignOut = {
                    viewModel.signOut()
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                KiloMetaTopBar(
                    currentDestination = currentDestination,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNavigateToProfile = { currentDestination = AppDestination.PROFILE }
                )
            },
            containerColor = DeepObsidianBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    AppDestination.COCKPIT -> HomeScreen(viewModel = viewModel, showTopBar = false)
                    AppDestination.HISTORY -> HistoryScreen()
                    AppDestination.MAINTENANCE -> MaintenanceScreen(
                        onNavigateBack = { currentDestination = AppDestination.COCKPIT }
                    )
                    AppDestination.EXPENSES -> ExpensesScreen(
                        onNavigateBack = { currentDestination = AppDestination.COCKPIT }
                    )
                    AppDestination.PROFILE -> DriverProfileScreen(
                        onNavigateBack = { currentDestination = AppDestination.COCKPIT }
                    )
                    AppDestination.BACKUP -> HomeScreen(viewModel = viewModel, showTopBar = false)
                }
            }
        }
    }

    // Modal de sincronización en la nube (accesible desde Drawer)
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
            onOpenAuth = { showSyncSheet = false },
            onOpenRecovery = {
                showSyncSheet = false
                showRecoveryScreen = true
            },
            onSignOut = {
                showSyncSheet = false
                viewModel.signOut()
            }
        )
    }
}
