// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/OnboardingStepIndicator.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Indicador visual de progreso del Onboarding Cockpit Flow en 3 pasos:
// 1. Conductor, 2. Vehículo, 3. Tu Meta. Incluye barra animada y badges ergonómicos.
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.ObsidianBorder
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

@Composable
fun OnboardingStepIndicator(
    currentStep: Int,
    totalSteps: Int = 3,
    onStepClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = ((currentStep + 1).toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "onboarding_progress"
    )

    val stepLabels = listOf(
        "1. Conductor" to "👤",
        "2. Vehículo" to "🚗",
        "3. Tu Meta" to "🎯"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Barra lineal animada de progreso
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = if (currentStep == totalSteps - 1) StatusGreen else ElectricBluePrimary,
            trackColor = DeepObsidianSurface
        )

        // Fila de Badges interactivos para los 3 pasos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stepLabels.forEachIndexed { index, (label, icon) ->
                val isCurrent = index == currentStep
                val isCompleted = index < currentStep
                val badgeColor = when {
                    isCurrent -> ElectricBluePrimary.copy(alpha = 0.2f)
                    isCompleted -> StatusGreen.copy(alpha = 0.15f)
                    else -> DeepObsidianSurface
                }
                val borderColor = when {
                    isCurrent -> ElectricBluePrimary
                    isCompleted -> StatusGreen
                    else -> ObsidianBorder
                }
                val textColor = when {
                    isCurrent -> ElectricBluePrimary
                    isCompleted -> StatusGreen
                    else -> TextSecondary
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeColor)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .clickable(enabled = onStepClick != null) { onStepClick?.invoke(index) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = if (isCompleted) "✓" else icon, fontSize = 12.sp)
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = textColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
