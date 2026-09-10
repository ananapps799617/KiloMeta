// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/ShiftPromptDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal de asistencia para conductores al registrar acciones fuera de turno.
// Pregunta pedagógicamente si desea iniciar jornada o registrar un movimiento esporádico.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun ShiftPromptDialog(
    actionName: String, // "carrera", "tanqueo" o "gasto"
    onDismiss: () -> Unit,
    onStartShiftSelected: () -> Unit,
    onSporadicSelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepObsidianSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🚦 ¿Iniciar Jornada Laboral?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "No tienes un turno activo en este momento.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ElectricBluePrimary
                )
                Text(
                    text = "¿Deseas iniciar tu jornada laboral ahora para registrar este $actionName, o es un movimiento esporádico / personal fuera de turno?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onStartShiftSelected,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Iniciar Turno 🚀",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onSporadicSelected,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusGreen),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(StatusGreen)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Es Esporádico ⚡",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}
