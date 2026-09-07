// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/HomeViews.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Vistas modulares de cabina para HomeScreen (NoActiveShiftView, ActiveShiftCockpitView
// y StandbyPlaceholderView). Mantiene la arquitectura desacoplada y archivos < 280 líneas.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.presentation.home.HomeUiState
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

// ------------------------------------------------------------------------------
// BLOQUE 1: VISTA DE CABINA SIN TURNO ACTIVO
// ------------------------------------------------------------------------------
@Composable
fun NoActiveShiftView(
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
        if (state.accountingAuditSummary != null) {
            AdvancedAccountingCard(auditSummary = state.accountingAuditSummary)
        } else {
            WeeklyQuotaCard(progress = state.weeklyProgress)
        }
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
// BLOQUE 2: VISTA DE CABINA CON TURNO ACTIVO
// ------------------------------------------------------------------------------
@Composable
fun ActiveShiftCockpitView(state: HomeUiState.ActiveShift) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SemaphoreCard(status = state.status)
        if (state.accountingAuditSummary != null) {
            AdvancedAccountingCard(auditSummary = state.accountingAuditSummary)
        } else {
            WeeklyQuotaCard(progress = state.weeklyProgress)
        }
        CockpitKpisCard(summary = state.summary, biFuelMetrics = state.biFuelMetrics)
        Spacer(modifier = Modifier.height(64.dp))
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 3: VISTA DE CONTINGENCIA STANDBY
// ------------------------------------------------------------------------------
@Composable
fun StandbyPlaceholderView(state: HomeUiState.StandbyPlaceholder) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = state.notice, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            SemaphoreCard(status = state.status)
        }
    }
}
