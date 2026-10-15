// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/OnboardingStepIndicator.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Stepper circular numerado de 3 etapas para el registro de cabina («Cockpit Dark Flow»).
// Nodos interactivos de 36dp con halos esmeralda y líneas conectoras fluidas (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.*

private data class StepInfo(val number: Int, val title: String)

@Composable
fun OnboardingStepIndicator(
    currentStep: Int,
    totalSteps: Int = 3,
    onStepClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        StepInfo(1, "Personal"),
        StepInfo(2, "Vehículo"),
        StepInfo(3, "Metas")
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isCurrent = index == currentStep
            val isCompleted = index < currentStep

            // Nodo circular con etiqueta inferior
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .wrapContentWidth()
                    .clickable(enabled = onStepClick != null && isCompleted) {
                        onStepClick?.invoke(index)
                    }
            ) {
                // Círculo numerado
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .then(
                            if (isCurrent) {
                                Modifier.shadow(8.dp, CircleShape, spotColor = EmeraldAccent)
                            } else Modifier
                        )
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> EmeraldPrimary
                                isCompleted -> EmeraldContainer
                                else -> DeepObsidianSurface
                            }
                        )
                        .border(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = when {
                                isCurrent -> EmeraldAccent
                                isCompleted -> EmeraldPrimary
                                else -> ObsidianBorder
                            },
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = if (isCompleted) "✓" else "${step.number}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = when {
                            isCurrent -> Color.White
                            isCompleted -> EmeraldAccent
                            else -> TextSecondary
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Etiqueta del paso
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                    ),
                    color = when {
                        isCurrent -> EmeraldAccent
                        isCompleted -> TextPrimary
                        else -> TextSecondary.copy(alpha = 0.7f)
                    },
                    textAlign = TextAlign.Center
                )
            }

            // Línea conectora entre nodos
            if (index < steps.size - 1) {
                val isLineCompleted = index < currentStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 0.dp)
                        .offset(y = (-10).dp) // Alinear con el centro de los círculos
                        .height(2.5.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLineCompleted) EmeraldPrimary else ObsidianBorder.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}
