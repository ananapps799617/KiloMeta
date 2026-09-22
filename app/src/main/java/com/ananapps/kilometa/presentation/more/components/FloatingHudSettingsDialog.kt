// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/more/components/FloatingHudSettingsDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo de configuración y verificación de permisos para la Burbuja Flotante y HUD (Orden #047).
// Guía al conductor a activar Superposición de Pantalla y Accesibilidad sin fricción.
// ==============================================================================

package com.ananapps.kilometa.presentation.more.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.*

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ananapps.kilometa.presentation.common.PermissionHelper

@Composable
fun FloatingHudSettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasOverlayPermission by remember { mutableStateOf(PermissionHelper.canDrawOverlays(context)) }
    var hasAccessibilityPermission by remember { mutableStateOf(PermissionHelper.isAccessibilityServiceEnabled(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = PermissionHelper.canDrawOverlays(context)
                hasAccessibilityPermission = PermissionHelper.isAccessibilityServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🫧 Copiloto & Burbuja Flotante",
                fontWeight = FontWeight.Bold,
                color = StatusGreen
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Para calcular tus ganancias sobre Uber, DiDi y Waze, el Copiloto requiere 2 permisos en verde:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                // Permiso 1: Superposición de pantalla
                PermissionStatusRow(
                    title = "1. Dibujar sobre otras apps",
                    isGranted = hasOverlayPermission,
                    actionLabel = if (hasOverlayPermission) "Concedido" else "Activar",
                    onAction = { PermissionHelper.openOverlaySettings(context) }
                )

                // Permiso 2: Accesibilidad Pasiva
                PermissionStatusRow(
                    title = "2. Lector Pasivo (Uber/DiDi)",
                    isGranted = hasAccessibilityPermission,
                    actionLabel = if (hasAccessibilityPermission) "Concedido" else "Activar",
                    onAction = { PermissionHelper.openAccessibilitySettings(context) }
                )

                if (!hasOverlayPermission || !hasAccessibilityPermission) {
                    Surface(
                        color = StatusAmber.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "⚠️ Si el interruptor aparece gris o inhabilitado (Android 13/14):",
                                color = StatusAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "1. Toca 'Desbloquear Permisos' abajo.\n2. Toca los 3 puntos (⋮) arriba a la derecha.\n3. Elige 'Permitir ajustes restringidos' y vuelve a activar.",
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                            OutlinedButton(
                                onClick = { PermissionHelper.openAppSettings(context) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("🔓 Desbloquear Permisos (3 puntos)", color = StatusAmber, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Text(
                    text = "🛡️ Modo 100% Pasivo Anti-Baneo: Cero clics automáticos, no interfiere con Uber ni DiDi.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
            ) {
                Text("Listo", color = DeepObsidianBackground, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DeepObsidianSurface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun PermissionStatusRow(
    title: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(
                text = if (isGranted) "🟢 Habilitado" else "🔴 Requiere activación",
                color = if (isGranted) StatusGreen else StatusRed,
                fontSize = 11.sp
            )
        }
        OutlinedButton(
            onClick = onAction,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(text = actionLabel, fontSize = 12.sp, color = StatusGreen)
        }
    }
}
