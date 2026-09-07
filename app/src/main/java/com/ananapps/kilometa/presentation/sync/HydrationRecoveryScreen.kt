// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/sync/HydrationRecoveryScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla de recuperación ante desastre (Disaster Recovery) para hidratación post-robo.
// Proporciona barra de progreso animada y auditoría de entidades reconstruidas.
// ==============================================================================

package com.ananapps.kilometa.presentation.sync

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.RestoreProgress
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

// ------------------------------------------------------------------------------
// BLOQUE 1: PANTALLA DE RECUPERACIÓN ANTE DESASTRE
// ------------------------------------------------------------------------------
@Composable
fun HydrationRecoveryScreen(
    progress: RestoreProgress,
    onStartRecovery: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = DeepObsidianBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Recuperación de Desastre 🚨",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Restaura tu historial contable completo tras pérdida o cambio de equipo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                when (progress) {
                    is RestoreProgress.Idle -> IdleRecoveryView()
                    is RestoreProgress.Running -> RunningRecoveryView(progress = progress)
                    is RestoreProgress.Completed -> CompletedRecoveryView(progress = progress)
                    is RestoreProgress.Failed -> FailedRecoveryView(progress = progress)
                }
            }

            // Barra inferior de botones de acción
            Column {
                when (progress) {
                    is RestoreProgress.Idle -> {
                        Button(
                            onClick = onStartRecovery,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Iniciar Reconstrucción desde Nube ☁️", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Volver", color = TextSecondary)
                        }
                    }
                    is RestoreProgress.Running -> {
                        // En ejecución se bloquea la salida
                    }
                    is RestoreProgress.Completed -> {
                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Ir a Cabina Principal 🚕", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    is RestoreProgress.Failed -> {
                        Button(
                            onClick = onStartRecovery,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reintentar Restauración 🔄", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: VISTAS MODULARES DE ESTADO DE RESTAURACIÓN
// ------------------------------------------------------------------------------
@Composable
private fun IdleRecoveryView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepObsidianSurface, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text(text = "🛡️ Integridad Garantizada", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "El proceso descargará tus Jornadas, Carreras, Abastecimientos de GNV/Gasolina y Gastos operativos en estricto orden de llaves relacionales para no corromper la base local.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun RunningRecoveryView(progress: RestoreProgress.Running) {
    val animatedProgress by animateFloatAsState(targetValue = progress.percentage, label = "recoveryProgress")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepObsidianSurface, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text(text = "Restaurando...", style = MaterialTheme.typography.titleMedium, color = ElectricBluePrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = progress.currentEntity, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = ElectricBluePrimary,
            trackColor = DeepObsidianSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "${(progress.percentage * 100).toInt()}% completado",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun CompletedRecoveryView(progress: RestoreProgress.Completed) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepObsidianSurface, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text(text = "✅ Reconstrucción Exitosa", style = MaterialTheme.typography.titleMedium, color = StatusGreen, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "• Jornadas restauradas: ${progress.totalShifts}", color = TextPrimary)
        Text(text = "• Carreras restauradas: ${progress.totalTrips}", color = TextPrimary)
        Text(text = "• Tanqueos bi-combustible: ${progress.totalFuelings}", color = TextPrimary)
        Text(text = "• Gastos en ruta: ${progress.totalExpenses}", color = TextPrimary)
    }
}

@Composable
private fun FailedRecoveryView(progress: RestoreProgress.Failed) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepObsidianSurface, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text(text = "❌ Error en Restauración", style = MaterialTheme.typography.titleMedium, color = StatusRed, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = progress.reason, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}
