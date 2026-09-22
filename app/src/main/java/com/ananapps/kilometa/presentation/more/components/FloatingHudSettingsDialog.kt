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

@Composable
fun FloatingHudSettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

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
                    text = "La burbuja acompaña tu turno activo sobre Uber, DiDi y Waze. Para operar requiere 2 permisos del sistema:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                // Permiso 1: Superposición de pantalla
                PermissionStatusRow(
                    title = "1. Dibujar sobre otras apps",
                    isGranted = hasOverlayPermission,
                    actionLabel = if (hasOverlayPermission) "Concedido" else "Activar",
                    onAction = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                        context.startActivity(intent)
                    }
                )

                // Permiso 2: Accesibilidad Pasiva
                PermissionStatusRow(
                    title = "2. Lector Pasivo de Ofertas",
                    isGranted = true, // Guía hacia ajustes
                    actionLabel = "Ajustes",
                    onAction = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                )

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
