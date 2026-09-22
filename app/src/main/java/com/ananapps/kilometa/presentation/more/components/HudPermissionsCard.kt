// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/more/components/HudPermissionsCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tarjeta reactiva de monitoreo de permisos en vivo para el Copiloto Flotante (Orden #047).
// Observa el ciclo de vida (ON_RESUME) y actualiza al instante el estado visual (Verde/Rojo).
// ==============================================================================

package com.ananapps.kilometa.presentation.more.components

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ananapps.kilometa.presentation.common.PermissionHelper
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun HudPermissionsCard(
    modifier: Modifier = Modifier
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

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DeepObsidianSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🛡️ Estado de Permisos en Vivo",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = "Ambos permisos deben estar en verde para que la burbuja lea ofertas de Uber, DiDi e inDrive:",
                color = TextSecondary,
                fontSize = 12.sp
            )

            PermissionStatusRow(
                title = "1. Superposición de Pantalla",
                subtitle = "Permite mostrar la burbuja flotante",
                isGranted = hasOverlayPermission,
                actionLabel = if (hasOverlayPermission) "Concedido" else "Activar",
                onAction = { PermissionHelper.openOverlaySettings(context) }
            )

            PermissionStatusRow(
                title = "2. Lector Pasivo (Accesibilidad)",
                subtitle = "Lee tarifas y rutas de las apps",
                isGranted = hasAccessibilityPermission,
                actionLabel = if (hasAccessibilityPermission) "Concedido" else "Activar",
                onAction = { PermissionHelper.openAccessibilitySettings(context) }
            )

            if (!hasOverlayPermission || !hasAccessibilityPermission) {
                Surface(
                    color = StatusAmber.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "⚠️ Si el interruptor aparece gris o inhabilitado (Android 13/14):",
                            color = StatusAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "1. Toca 'Desbloquear Permisos' abajo.\n2. Toca los 3 puntos (⋮) arriba a la derecha.\n3. Selecciona 'Permitir ajustes restringidos' y vuelve a activar.",
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
        }
    }
}

@Composable
private fun PermissionStatusRow(
    title: String,
    subtitle: String,
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
            Text(text = subtitle, color = TextSecondary, fontSize = 10.sp)
            Text(
                text = if (isGranted) "🟢 Habilitado y Funcional" else "🔴 Inactivo (Requiere activación)",
                color = if (isGranted) StatusGreen else StatusRed,
                fontWeight = if (isGranted) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            )
        }
        OutlinedButton(
            onClick = onAction,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = actionLabel,
                fontSize = 12.sp,
                color = if (isGranted) StatusGreen else StatusAmber,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
