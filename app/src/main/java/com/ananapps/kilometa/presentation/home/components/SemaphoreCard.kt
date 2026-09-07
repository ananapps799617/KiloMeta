// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/SemaphoreCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Componente principal de cabina que visualiza el Semáforo Financiero en tiempo real.
// Diseñado para lectura instantánea a 60 cm del tablero con Display Large (38sp, tnum),
// halo de pulso cromático animado en estado Verde (Turno Libre) y respuesta háptica.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.FinancialStatus
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.StatusAmber
import com.ananapps.kilometa.presentation.theme.StatusAmberContainer
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusGreenContainer
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.StatusRedContainer
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: COMPONENTE SEMAPHORE CARD DE ALTA VISIBILIDAD
// ------------------------------------------------------------------------------
/**
 * Renderiza el Semáforo Financiero central con tipografía de alto impacto y pulso en Verde.
 *
 * @param status Estado financiero actual ([FinancialStatus.Red], [FinancialStatus.Amber], [FinancialStatus.Green]).
 * @param amountRemaining Monto faltante en COP para alcanzar el Turno Libre.
 * @param surplusAmount Excedente de ganancia neta cuando se ha superado la cuota.
 * @param modifier Modificador Compose de diseño.
 */
@Composable
fun SemaphoreCard(
    status: FinancialStatus,
    amountRemaining: Double = status.remainingAmount,
    surplusAmount: Double = status.surplusAmount,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Disparo de retroalimentación háptica cuando se alcanza el estado Verde
    LaunchedEffect(status) {
        if (status is FinancialStatus.Green) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val (statusColor, containerColor) = when (status) {
        is FinancialStatus.Red -> StatusRed to StatusRedContainer
        is FinancialStatus.Amber -> StatusAmber to StatusAmberContainer
        is FinancialStatus.Green -> StatusGreen to StatusGreenContainer
    }

    // Halo de pulso animado para celebrar y destacar el Turno Libre en cabina
    val isGreen = status is FinancialStatus.Green
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = if (isGreen) 0.35f else 0.0f,
        targetValue = if (isGreen) 0.90f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloAlpha"
    )
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isGreen) 1.03f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(if (isGreen) haloScale else 1f)
            .height(Constants.SEMAPHORE_CARD_MIN_HEIGHT_DP.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (isGreen) statusColor.copy(alpha = haloAlpha) else statusColor),
            width = if (isGreen) 3.dp else 1.5.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Fila superior: Baliza luminosa y Título de Estado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                        .border(2.dp, containerColor, CircleShape)
                )

                Text(
                    text = if (isGreen) "¡TURNO LIBRE!" else status.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    ),
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila central: Display Large (38sp, tnum) del saldo en cabina
            val mainDisplayText = when (status) {
                is FinancialStatus.Green -> {
                    if (surplusAmount > 0.0) {
                        "+${formatCurrency(surplusAmount)}"
                    } else {
                        "¡META CUBIERTA!"
                    }
                }
                else -> {
                    "-${formatCurrency(amountRemaining)}"
                }
            }

            val supportingText = when (status) {
                is FinancialStatus.Green -> "Utilidad neta de bolsillo asegurada"
                is FinancialStatus.Amber -> "Faltan para entrar en Turno Libre"
                is FinancialStatus.Red -> "Faltan para cubrir cuota y costos"
            }

            Column {
                Text(
                    text = mainDisplayText,
                    style = TextStyle(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontFeatureSettings = "tnum"
                    ),
                    color = if (isGreen) StatusGreen else TextPrimary
                )

                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila inferior: Mensaje pedagógico del semáforo
            Text(
                text = status.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: FORMATEADOR DE MONEDA DETERMINISTA
// ------------------------------------------------------------------------------
private fun formatCurrency(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return String.format(Locale.GERMANY, "$%,d COP", rounded)
}
