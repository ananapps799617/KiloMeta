// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/HomeScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla principal (Cockpit) del conductor en KiloMeta.
// Estructura una interfaz ergonómica con TopBar de estado Offline-First,
// visualizador del Semáforo Financiero y Botonera Inferior (Bottom Dock) con
// objetivos táctiles superiores a 56dp para operación segura en cabina.
// ==============================================================================

package com.ananapps.kilometa.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.R
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.presentation.home.components.SemaphoreCard
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.FuelGasoline
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

// ------------------------------------------------------------------------------
// BLOQUE 1: PANTALLA PRINCIPAL DE CABINA (HOME SCREEN)
// ------------------------------------------------------------------------------
/**
 * Composable raíz de la vista de cabina de KiloMeta.
 *
 * @param state Estado actual de la pantalla ([HomeUiState]).
 * @param onQuickIncomeClick Callback para registro rápido de ingreso.
 * @param onQuickExpenseClick Callback para registro rápido de gasto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState = HomeUiState.StandbyPlaceholder(),
    onQuickIncomeClick: () -> Unit = {},
    onQuickExpenseClick: () -> Unit = {}
) {
    val currentState = when (state) {
        is HomeUiState.StandbyPlaceholder -> state
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepObsidianSurface
                )
            )
        },
        bottomBar = {
            // ------------------------------------------------------------------
            // BLOQUE 2: BOTTOM DOCK ERGONÓMICO (TOUCH TARGET >= 56DP)
            // ------------------------------------------------------------------
            CockpitBottomDock(
                onQuickIncome = onQuickIncomeClick,
                onQuickExpense = onQuickExpenseClick
            )
        },
        containerColor = DeepObsidianBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Semáforo Financiero Central
            SemaphoreCard(status = currentState.status)

            // Panel de Resumen Provisional (Cero Mentiras Técnicas)
            NoticeCard(notice = currentState.notice)

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 3: TARJETA DE AVISO ARQUITECTURAL PROVISIONAL
// ------------------------------------------------------------------------------
@Composable
private fun NoticeCard(
    notice: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepObsidianSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "ESTADO DE CONSTRUCCIÓN",
                style = MaterialTheme.typography.labelSmall,
                color = FuelGasoline,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = notice,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Orden #002 completada: Base limpia y arquitectura reactiva lista para Room en Orden #003.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 4: BOTONERA DE ACCIÓN RÁPIDA EN CABINA
// ------------------------------------------------------------------------------
@Composable
private fun CockpitBottomDock(
    onQuickIncome: () -> Unit,
    onQuickExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DeepObsidianSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onQuickExpense,
                modifier = Modifier
                    .weight(1f)
                    .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusRed
                )
            ) {
                Text(
                    text = stringResource(R.string.btn_quick_expense),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
            }

            Button(
                onClick = onQuickIncome,
                modifier = Modifier
                    .weight(1f)
                    .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusGreen
                )
            ) {
                Text(
                    text = stringResource(R.string.btn_quick_income),
                    style = MaterialTheme.typography.labelLarge,
                    color = DeepObsidianBackground
                )
            }
        }
    }
}
