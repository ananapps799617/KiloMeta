// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/OnboardingNavControls.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Controles de navegación ergonómicos en la zona natural del pulgar (Thumb Zone).
// Soporta transición hacia adelante, retorno al paso anterior y finalización de alta.
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary

@Composable
fun OnboardingNavControls(
    currentStep: Int,
    totalSteps: Int = 3,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onFinishClick: () -> Unit,
    isNextEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isLastStep = currentStep >= totalSteps - 1

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botón "Atrás" disponible a partir del Paso 2
        if (currentStep > 0) {
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onBackClick()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "← ATRÁS",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        }

        // Botón "Continuar" o "Finalizar"
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (isLastStep) onFinishClick() else onNextClick()
            },
            enabled = isNextEnabled,
            modifier = Modifier
                .weight(if (currentStep > 0) 1.5f else 1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLastStep) StatusGreen else ElectricBluePrimary
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = if (isLastStep) "🏁 FINALIZAR" else "CONTINUAR ➔",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DeepObsidianBackground
            )
        }
    }
}
