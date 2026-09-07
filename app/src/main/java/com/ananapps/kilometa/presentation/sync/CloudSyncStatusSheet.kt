// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/sync/CloudSyncStatusSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet de cabina que exhibe la telemetría detallada de sincronización,
// estado de la cuenta del conductor, botón de prueba en vivo y control de respaldo.
// ==============================================================================

package com.ananapps.kilometa.presentation.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.ConnectionDiagnostic
import com.ananapps.kilometa.domain.model.SyncState
import com.ananapps.kilometa.domain.model.SyncStatus
import com.ananapps.kilometa.domain.model.UserSession
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusAmber
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.ananapps.kilometa.domain.model.SyncPreferences
import com.ananapps.kilometa.presentation.sync.components.SyncPreferencesView

// ------------------------------------------------------------------------------
// BLOQUE 1: MODAL BOTTOM SHEET DE ESTADO DE SINCRONIZACIÓN
// ------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncStatusSheet(
    sheetState: SheetState,
    syncStatus: SyncStatus,
    userSession: UserSession?,
    connectionDiagnostic: ConnectionDiagnostic? = null,
    isPinging: Boolean = false,
    syncPreferences: SyncPreferences = SyncPreferences(),
    onToggleAutoSync: (Boolean) -> Unit = {},
    onSelectWifiOnly: (Boolean) -> Unit = {},
    onDismiss: () -> Unit,
    onSyncNow: () -> Unit,
    onTestConnection: () -> Unit = {},
    onOpenAuth: () -> Unit,
    onOpenRecovery: () -> Unit,
    onSignOut: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidianSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Respaldo y Nube (Cloud Sync)",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Telemetría en vivo y sincronización contable",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
            )

            // Panel de Cuenta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepObsidianSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(text = "Cuenta de Conductor", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                val accountLabel = userSession?.email
                    ?: if (userSession?.isAnonymous == true) "Sesión Anónima Temporal" else if (userSession != null) userSession.uid.take(8) else "Modo 100% Local (Sin cuenta)"
                Text(text = accountLabel, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Panel de Diagnóstico de Enlace en Vivo
            if (connectionDiagnostic != null) {
                val pingTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(connectionDiagnostic.timestampMillis))
                val latencyColor = when {
                    !connectionDiagnostic.isConnected -> StatusRed
                    (connectionDiagnostic.latencyMs ?: 0L) < 300 -> StatusGreen
                    else -> StatusAmber
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepObsidianSurfaceVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Prueba en Vivo:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(text = pingTime, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = if (connectionDiagnostic.isConnected) "Conectado" else "Sin Enlace", style = MaterialTheme.typography.titleSmall, color = latencyColor, fontWeight = FontWeight.Bold)
                        val latText = connectionDiagnostic.latencyMs?.let { "${it} ms" } ?: "N/A"
                        Text(text = "Latencia: $latText", style = MaterialTheme.typography.titleSmall, color = latencyColor, fontWeight = FontWeight.Bold)
                    }
                    Text(text = connectionDiagnostic.message, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Panel de Último Respaldo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepObsidianSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                val lastSyncStr = syncStatus.lastSyncTimestamp?.let {
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it))
                } ?: "Nunca"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Último Respaldo:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text(text = lastSyncStr, style = MaterialTheme.typography.bodyMedium, color = if (syncStatus.lastSyncTimestamp != null) StatusGreen else TextSecondary, fontWeight = FontWeight.Bold)
                }
                if (syncStatus.errorMessage != null) {
                    Text(text = syncStatus.errorMessage, style = MaterialTheme.typography.labelSmall, color = StatusRed)
                }
            }

            // Panel de Preferencias de Sincronización en Segundo Plano (WorkManager)
            SyncPreferencesView(
                preferences = syncPreferences,
                onToggleAutoSync = onToggleAutoSync,
                onSelectWifiOnly = onSelectWifiOnly
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Botones de Acción
            if (userSession != null) {
                // Botón Probar Conexión en Vivo
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTestConnection()
                    },
                    enabled = !isPinging,
                    modifier = Modifier.fillMaxWidth().height(Constants.MIN_TOUCH_TARGET_DP.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isPinging) {
                        CircularProgressIndicator(color = ElectricBluePrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(" Probando Enlace...", color = ElectricBluePrimary)
                    } else {
                        Text("Probar Conexión en Vivo ⚡", color = ElectricBluePrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSyncNow()
                    },
                    enabled = syncStatus.state != SyncState.SYNCING,
                    modifier = Modifier.fillMaxWidth().height(Constants.MIN_TOUCH_TARGET_DP.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    val label = if (syncStatus.state == SyncState.SYNCING) "Sincronizando..." else "Sincronizar Ahora ☁️"
                    Text(label, color = TextPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenRecovery()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Recuperar Datos (Disaster Recovery) 🚨", color = TextSecondary)
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSignOut()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cerrar Sesión", color = StatusRed)
                }
            } else {
                Button(
                    onClick = {
                        onDismiss()
                        onOpenAuth()
                    },
                    modifier = Modifier.fillMaxWidth().height(Constants.MIN_TOUCH_TARGET_DP.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Vincular Cuenta para Respaldo 🔐", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
