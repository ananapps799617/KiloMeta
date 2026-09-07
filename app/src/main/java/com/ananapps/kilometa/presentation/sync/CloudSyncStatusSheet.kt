// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/sync/CloudSyncStatusSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet de cabina que exhibe la telemetría detallada de sincronización,
// estado de la cuenta del conductor y disparador manual de respaldo en la nube.
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
import com.ananapps.kilometa.domain.model.SyncState
import com.ananapps.kilometa.domain.model.SyncStatus
import com.ananapps.kilometa.domain.model.UserSession
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: MODAL BOTTOM SHEET DE ESTADO DE SINCRONIZACIÓN
// ------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSyncStatusSheet(
    sheetState: SheetState,
    syncStatus: SyncStatus,
    userSession: UserSession?,
    onDismiss: () -> Unit,
    onSyncNow: () -> Unit,
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
                .padding(24.dp)
        ) {
            Text(
                text = "Respaldo y Nube (Cloud Sync)",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Protección anti-pérdida y sincronización de datos contables",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Panel de Cuenta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepObsidianSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Cuenta de Conductor",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (userSession != null) {
                    val accountLabel = userSession.email
                        ?: if (userSession.isAnonymous) "Sesión Anónima Temporal" else userSession.uid.take(8)
                    Text(
                        text = accountLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Modo 100% Local (Sin cuenta vinculada)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Panel de Última Sincronización
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepObsidianSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Último Respaldo:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    val lastSyncStr = syncStatus.lastSyncTimestamp?.let {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it))
                    } ?: "Nunca"
                    Text(
                        text = lastSyncStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (syncStatus.lastSyncTimestamp != null) StatusGreen else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (syncStatus.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = syncStatus.errorMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón de Sincronizar Ahora o Vincular Cuenta
            if (userSession != null) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSyncNow()
                    },
                    enabled = syncStatus.state != SyncState.SYNCING,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (syncStatus.state == SyncState.SYNCING) {
                        CircularProgressIndicator(color = TextPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(" Sincronizando...", color = TextPrimary, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Sincronizar Ahora ☁️", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenRecovery()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Recuperar Base de Datos (Disaster Recovery) 🚨", color = ElectricBluePrimary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSignOut()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Constants.MIN_TOUCH_TARGET_DP.dp),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Vincular Cuenta para Respaldo 🔐", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
