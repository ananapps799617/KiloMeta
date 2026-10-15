// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/OnboardingNavControls.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Controles de navegación en la Thumb Zone para el flujo Cockpit Dark.
// Botón esmeralda de confirmación [Siguiente ➔] / [Finalizar configuración] y botón [← Anterior] (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun OnboardingNavControls(
    currentStep: Int,
    totalSteps: Int = 3,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onFinishClick: () -> Unit,
    isNextEnabled: Boolean = true,
    onExitOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isLastStep = currentStep >= totalSteps - 1

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón [← Anterior] visible a partir del Paso 1
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBackClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ObsidianBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DeepObsidianSurface,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(
                        text = "← Anterior",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            // Botón Principal [Siguiente ➔] o [Finalizar configuración]
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isLastStep) onFinishClick() else onNextClick()
                },
                enabled = isNextEnabled,
                modifier = Modifier
                    .weight(if (currentStep > 0) 1.5f else 1f)
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = EmeraldContainer,
                    disabledContentColor = TextSecondary.copy(alpha = 0.6f)
                )
            ) {
                Text(
                    text = if (isLastStep) "Finalizar configuración" else "Siguiente ➔",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                )
            }
        }

        // Opción [← Volver] exclusiva del Paso 0 para cancelar/salir
        if (currentStep == 0) {
            TextButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onExitOnboarding()
                },
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = "← Volver",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextSecondary
                )
            }
        }
    }
}
