// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/MainActivity.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Actividad principal única (Single Activity Architecture) de KiloMeta 2.0.
// Implementa el Scaffold de cabina con barra de navegación inferior de 5 destinos,
// botón flotante central (+) para acciones rápidas y pestaña Más (cero menú hamburguesa).
// ==============================================================================

package com.ananapps.kilometa.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ananapps.kilometa.presentation.auth.LoginScreen
import com.ananapps.kilometa.presentation.expenses.ExpensesScreen
import com.ananapps.kilometa.presentation.history.HistoryScreen
import com.ananapps.kilometa.presentation.home.HomeScreen
import com.ananapps.kilometa.presentation.home.HomeViewModel
import com.ananapps.kilometa.presentation.home.components.QuickActionSheet
import com.ananapps.kilometa.presentation.maintenance.MaintenanceScreen
import com.ananapps.kilometa.presentation.more.MoreScreen
import com.ananapps.kilometa.presentation.navigation.*
import com.ananapps.kilometa.presentation.profile.DriverProfileScreen
import com.ananapps.kilometa.presentation.sync.CloudSyncStatusSheet
import com.ananapps.kilometa.presentation.sync.HydrationRecoveryScreen
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.KiloMetaTheme
import dagger.hilt.android.AndroidEntryPoint

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppHost(
    viewModel: HomeViewModel = viewModel()
) {
    val userSession by viewModel.userSession.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()

    // 1. Gatekeeper de Autenticación
    if (userSession == null) {
        LoginScreen(
            onSignInWithGoogleToken = { viewModel.signInWithGoogle(it) },
            onGoogleSignInError = { viewModel.setAuthError(it) },
            onSignInWithEmail = { email, pass -> viewModel.signInWithEmail(email, pass) },
            authError = authError,
            isLoading = isAuthLoading
        )
        return
    }

    val driverProfile by viewModel.driverProfile.collectAsState()

    // 2. Onboarding Obligatorio para Conductor Nuevo
    if (!driverProfile.isReadyForCabina) {
        DriverProfileScreen(
            isOnboardingMode = true,
            userEmail = userSession?.email ?: "",
            onNavigateBack = { /* Desbloqueo reactivo hacia cabina */ }
        )
        return
    }

    var currentNavTab by rememberSaveable { mutableStateOf(KiloMetaNavTab.ROUTE) }
    var activeSubDestination by rememberSaveable { mutableStateOf<AppDestination?>(null) }
    var showQuickActionSheet by rememberSaveable { mutableStateOf(false) }
    var triggerQuickTrip by rememberSaveable { mutableStateOf(false) }
    var triggerQuickFueling by rememberSaveable { mutableStateOf(false) }
    var triggerQuickExpense by rememberSaveable { mutableStateOf(false) }

    var showSyncSheet by rememberSaveable { mutableStateOf(false) }
    var showRecoveryScreen by rememberSaveable { mutableStateOf(false) }

    val syncStatus by viewModel.syncStatus.collectAsState()
    val syncPreferences by viewModel.syncPreferences.collectAsState()
    val connectionDiagnostic by viewModel.connectionDiagnostic.collectAsState()
    val isPingingCloud by viewModel.isPingingCloud.collectAsState()
    val restoreProgress by viewModel.restoreProgress.collectAsState()

    val quickActionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val syncSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            KiloMetaTopBar(
                currentDestination = when {
                    activeSubDestination != null -> activeSubDestination!!
                    currentNavTab == KiloMetaNavTab.ROUTE -> AppDestination.COCKPIT
                    currentNavTab == KiloMetaNavTab.REPORTS -> AppDestination.HISTORY
                    currentNavTab == KiloMetaNavTab.REMINDERS -> AppDestination.MAINTENANCE
                    currentNavTab == KiloMetaNavTab.MORE -> AppDestination.PROFILE
                    else -> AppDestination.COCKPIT
                },
                onNavigateToProfile = { activeSubDestination = AppDestination.PROFILE }
            )
        },
        bottomBar = {
            KiloMetaBottomBar(
                currentTab = currentNavTab,
                onTabSelected = { tab ->
                    activeSubDestination = null
                    currentNavTab = tab
                },
                onQuickActionClick = { showQuickActionSheet = true }
            )
        },
        containerColor = DeepObsidianBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                activeSubDestination == AppDestination.PROFILE -> {
                    DriverProfileScreen(
                        onNavigateBack = { activeSubDestination = null },
                        isOnboardingMode = false,
                        userEmail = userSession?.email ?: ""
                    )
                }
                activeSubDestination == AppDestination.EXPENSES -> {
                    ExpensesScreen(
                        onNavigateBack = { activeSubDestination = null }
                    )
                }
                currentNavTab == KiloMetaNavTab.ROUTE -> {
                    HomeScreen(
                        viewModel = viewModel,
                        showTopBar = false,
                        triggerQuickTrip = triggerQuickTrip,
                        onResetQuickTripTrigger = { triggerQuickTrip = false },
                        triggerQuickFueling = triggerQuickFueling,
                        onResetQuickFuelingTrigger = { triggerQuickFueling = false },
                        triggerQuickExpense = triggerQuickExpense,
                        onResetQuickExpenseTrigger = { triggerQuickExpense = false }
                    )
                }
                currentNavTab == KiloMetaNavTab.REPORTS -> {
                    HistoryScreen()
                }
                currentNavTab == KiloMetaNavTab.REMINDERS -> {
                    MaintenanceScreen(
                        onNavigateBack = { currentNavTab = KiloMetaNavTab.ROUTE }
                    )
                }
                currentNavTab == KiloMetaNavTab.MORE -> {
                    MoreScreen(
                        onNavigateToProfile = { activeSubDestination = AppDestination.PROFILE },
                        onNavigateToMaintenance = { currentNavTab = KiloMetaNavTab.REMINDERS },
                        onNavigateToExpenses = { activeSubDestination = AppDestination.EXPENSES },
                        onNavigateToSync = { showSyncSheet = true },
                        onSignOut = { viewModel.signOut() }
                    )
                }
            }
        }
    }

    if (showQuickActionSheet) {
        QuickActionSheet(
            sheetState = quickActionSheetState,
            onDismiss = { showQuickActionSheet = false },
            onSelectTrip = {
                currentNavTab = KiloMetaNavTab.ROUTE
                activeSubDestination = null
                triggerQuickTrip = true
            },
            onSelectFueling = {
                currentNavTab = KiloMetaNavTab.ROUTE
                activeSubDestination = null
                triggerQuickFueling = true
            },
            onSelectExpense = {
                currentNavTab = KiloMetaNavTab.ROUTE
                activeSubDestination = null
                triggerQuickExpense = true
            }
        )
    }

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
