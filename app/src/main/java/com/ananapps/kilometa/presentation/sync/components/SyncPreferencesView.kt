// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/sync/components/SyncPreferencesView.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Componente de interfaz de usuario para configurar las preferencias de sincronización
// en segundo plano (Auto-Sync switch y selector de política de red Híbrida vs Wi-Fi).
// Garantiza altura mínima táctil >= 56dp (Constants.MIN_TOUCH_TARGET_DP).
// ==============================================================================

package com.ananapps.kilometa.presentation.sync.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.SyncPreferences
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

// ------------------------------------------------------------------------------
// BLOQUE 1: COMPONENTE DE PREFERENCIAS DE SINCRONIZACIÓN
// ------------------------------------------------------------------------------
/**
 * Panel interactivo para conmutar la sincronización en segundo plano con WorkManager
 * y alternar la política de conectividad requerida por el conductor.
 */
@Composable
fun SyncPreferencesView(
    preferences: SyncPreferences,
    onToggleAutoSync: (Boolean) -> Unit,
    onSelectWifiOnly: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DeepObsidianSurfaceVariant, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // Encabezado
        Text(
            text = "Segundo Plano (WorkManager)",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )

        // Fila 1: Switch de Auto-Sync (Touch Target >= 56dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Constants.MIN_TOUCH_TARGET_DP.dp)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleAutoSync(!preferences.isAutoSyncEnabled)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sincronización Automática",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (preferences.isAutoSyncEnabled) "Activa periódicamente (cada 15 min)" else "Pausada",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (preferences.isAutoSyncEnabled) StatusGreen else TextSecondary
                )
            }
            Switch(
                checked = preferences.isAutoSyncEnabled,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleAutoSync(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimary,
                    checkedTrackColor = ElectricBluePrimary
                )
            )
        }

        if (preferences.isAutoSyncEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Política de Red:",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Opción 1: Híbrido (Datos Móviles + Wi-Fi) [Recomendado]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Constants.MIN_TOUCH_TARGET_DP.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectWifiOnly(false)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = !preferences.requireWifiOnly,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectWifiOnly(false)
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = ElectricBluePrimary)
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "Híbrido (Datos + Wi-Fi) [Recomendado]",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Garantiza respaldo continuo en ruta y casa",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            // Opción 2: Solo con Wi-Fi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Constants.MIN_TOUCH_TARGET_DP.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectWifiOnly(true)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = preferences.requireWifiOnly,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectWifiOnly(true)
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = ElectricBluePrimary)
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "Solo con Wi-Fi",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Espera a red no medida para sincronizar",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
