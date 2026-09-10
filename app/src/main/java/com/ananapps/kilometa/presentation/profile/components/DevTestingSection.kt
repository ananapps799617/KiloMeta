// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/DevTestingSection.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Subcomponente de desarrollo y pruebas de cabina para la Gerencia General.
// Se visualiza ÚNICAMENTE si el correo de la sesión activa pertenece al administrador.
// Permite restablecer el onboarding y datos locales de prueba en un toque.
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.StatusAmber
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

@Composable
fun DevTestingSection(
    userEmail: String,
    onResetData: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Verificación estricta de cuenta autorizada de Gerencia / Desarrollador
    val cleanEmail = userEmail.trim().lowercase()
    val isAuthorizedAdmin = cleanEmail == "ananapps.soporte@gmail.com" ||
            cleanEmail.contains("ananapps") ||
            cleanEmail.endsWith("@ananapps.co")

    if (!isAuthorizedAdmin) return

    val haptic = LocalHapticFeedback.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DeepObsidianSurface)
            .border(1.5.dp, StatusAmber.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "🛠️ Zona de Desarrollo & Pruebas (Solo Gerencia)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = StatusAmber
        )
        Text(
            text = "Esta sección es invisible para conductores regulares. Te permite reiniciar el estado de bienvenida y probar el flujo completo de nuevo usuario.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showConfirmDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StatusRed.copy(alpha = 0.2f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed)
        ) {
            Text(
                text = "⚠️ Resetear Onboarding y Datos de Prueba",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = StatusRed
            )
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    text = "Confirmar Reseteo de Pruebas",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = StatusRed
                )
            },
            text = {
                Text(
                    text = "¿Deseas reiniciar el Onboarding y borrar los datos locales de prueba? La aplicación volverá a solicitar la configuración de cabina como usuario nuevo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        onResetData()
                    }
                ) {
                    Text("Sí, Resetear", color = StatusRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = DeepObsidianBackground
        )
    }
}
