// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/HomeScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla principal y Cockpit de cabina para el conductor en KiloMeta.
// Conecta el ViewModel con StateFlow, orquesta el Semáforo Financiero dinámico,
// la tarjeta de Bolsa Semanal prorrateada, los KPIs bi-combustible en ruta,
// los Bottom Sheets de captura express a 3 toques y la botonera inferior ergonómica (>=56dp).
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
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.presentation.home.components.CloseShiftDialog
import com.ananapps.kilometa.presentation.home.components.CockpitBottomDock
import com.ananapps.kilometa.presentation.home.components.CockpitKpisCard
import com.ananapps.kilometa.presentation.home.components.RegisterFuelingSheet
import com.ananapps.kilometa.presentation.home.components.RegisterTripSheet
import com.ananapps.kilometa.presentation.home.components.SemaphoreCard
import com.ananapps.kilometa.presentation.home.components.StartShiftDialog
import com.ananapps.kilometa.presentation.home.components.WeeklyQuotaCard
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusAmber
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

// ------------------------------------------------------------------------------
// BLOQUE 1: PANTALLA PRINCIPAL DE CABINA (HOME COCKPIT)
// ------------------------------------------------------------------------------
/**
 * Composable raíz de cabina. Conecta el StateFlow reactivo del ViewModel y orquesta
 * los controles del Semáforo Financiero y captura express.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showTripSheet by remember { mutableStateOf(false) }
    var showFuelingSheet by remember { mutableStateOf(false) }
    var showStartShiftDialog by remember { mutableStateOf(false) }
    var showCloseShiftDialog by remember { mutableStateOf(false) }

    val tripSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fuelSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    if (uiState is HomeUiState.ActiveShift) {
                        OutlinedButton(
                            onClick = { showCloseShiftDialog = true },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "Cerrar Turno",
                                color = StatusRed,
                                style = MaterialTheme.typography.labelSmall
                            )
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WeeklyQuotaCard(progress = state.weeklyProgress)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "CABINA EN ESPERA",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = StatusAmber
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Inicia tu turno para activar el Semáforo Financiero y el cálculo en vivo de Turno Libre.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showStartShiftDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                                ) {
                                    Text(
                                        text = "Iniciar Jornada Laboral",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = DeepObsidianBackground
                                    )
                                }
                            }
                        }
                    }
                }

                is HomeUiState.ActiveShift -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SemaphoreCard(
                            status = state.status,
                            amountRemaining = state.amountRemainingForFreeShift,
                            surplusAmount = state.status.surplusAmount
                        )

                        WeeklyQuotaCard(progress = state.weeklyProgress)

                        CockpitKpisCard(summary = state.summary, metrics = state.biFuelMetrics)
                    }
                }

                is HomeUiState.StandbyPlaceholder -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SemaphoreCard(status = state.status)
                    }
                }
            }
        }
    }

    // Modal Bottom Sheets de captura express
    if (showTripSheet) {
        RegisterTripSheet(
            sheetState = tripSheetState,
            onDismiss = { showTripSheet = false },
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
            onDismiss = { showFuelingSheet = false },
            onConfirmFueling = { fuelType, amount, volume, odo ->
                viewModel.registerFueling(fuelType, amount, volume, odo)
            }
        )
    }

    // Diálogos de control de jornada
    if (showStartShiftDialog) {
        StartShiftDialog(
            initialOdometer = 100_000,
            onDismiss = { showStartShiftDialog = false },
            onConfirm = { startOdo ->
                viewModel.startShift(startOdometer = startOdo)
                showStartShiftDialog = false
            }
        )
    }

    if (showCloseShiftDialog) {
        val startOdo = (uiState as? HomeUiState.ActiveShift)?.summary?.startOdometer ?: 100_000
        CloseShiftDialog(
            startOdometer = startOdo,
            onDismiss = { showCloseShiftDialog = false },
            onConfirm = { endOdo ->
                viewModel.closeShift(endOdometer = endOdo)
                showCloseShiftDialog = false
            }
        )
    }
}
