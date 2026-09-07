// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/ShiftControlDialogs.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogos modales para el control del ciclo de vida de los turnos de trabajo:
// apertura de jornada con odómetro inicial y cierre con odómetro final y validación de odometría.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary

// ------------------------------------------------------------------------------
// BLOQUE 1: DIÁLOGO DE INICIO DE TURNO
// ------------------------------------------------------------------------------
/**
 * Diálogo para captura del kilometraje de arranque al iniciar la jornada.
 */
@Composable
fun StartShiftDialog(
    initialOdometer: Int,
    onDismiss: () -> Unit,
    onConfirm: (startOdometer: Int) -> Unit
) {
    var odometerInput by remember { mutableIntStateOf(initialOdometer) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Iniciar Turno de Trabajo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ingresa el odómetro de arranque del vehículo (km):")
                OutlinedTextField(
                    value = odometerInput.toString(),
                    onValueChange = { input ->
                        input.filter { it.isDigit() }.toIntOrNull()?.let { odometerInput = it }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(odometerInput) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
            ) {
                Text("Arrancar Turno", color = DeepObsidianBackground)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// ------------------------------------------------------------------------------
// BLOQUE 2: DIÁLOGO DE CIERRE DE TURNO
// ------------------------------------------------------------------------------
/**
 * Diálogo para captura del kilometraje final y cierre definitivo de jornada.
 */
@Composable
fun CloseShiftDialog(
    startOdometer: Int,
    onDismiss: () -> Unit,
    onConfirm: (endOdometer: Int) -> Unit
) {
    var closeOdometer by remember { mutableIntStateOf(startOdometer) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finalizar Jornada") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Odómetro inicial: $startOdometer km")
                Text("Ingresa el odómetro final para cerrar el turno:")
                OutlinedTextField(
                    value = closeOdometer.toString(),
                    onValueChange = { input ->
                        input.filter { it.isDigit() }.toIntOrNull()?.let { closeOdometer = it }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(closeOdometer) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
            ) {
                Text("Cerrar Jornada", color = TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continuar en Turno")
            }
        }
    )
}
