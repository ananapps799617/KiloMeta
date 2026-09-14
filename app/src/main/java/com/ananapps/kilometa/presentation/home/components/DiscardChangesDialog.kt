// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/DiscardChangesDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo de confirmación vehicular para evitar pérdida accidental de datos
// al presionar retroceso en las hojas modales de registro (Swipe-Down Lockdown).
// Regla sagrada: < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

@Composable
fun DiscardChangesDialog(
    title: String = "¿Descartar cambios?",
    message: String = "Tienes datos digitados sin guardar. ¿Deseas salir y perder la información ingresada?",
    onConfirmDiscard: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title, fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = { Text(message, color = TextSecondary) },
        confirmButton = {
            Button(
                onClick = onConfirmDiscard,
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
            ) {
                Text("Descartar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Continuar Editando", color = TextSecondary)
            }
        },
        containerColor = DeepObsidianSurface
    )
}
