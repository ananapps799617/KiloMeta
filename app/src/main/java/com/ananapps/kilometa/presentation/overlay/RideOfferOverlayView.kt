// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/overlay/RideOfferOverlayView.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Vista Compose flotante (HUD) de evaluación financiera instantánea de ofertas (Orden #047).
// Despliega semáforo verde/ámbar/rojo, ganancia limpia en COP y rendimiento por km y hora.
// ==============================================================================

package com.ananapps.kilometa.presentation.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.OfferSemaphoreTier
import com.ananapps.kilometa.domain.model.TripOfferEvaluation
import com.ananapps.kilometa.presentation.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RideOfferOverlayView(
    evaluation: TripOfferEvaluation,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (tierColor, tierLabel) = when (evaluation.semaphoreTier) {
        OfferSemaphoreTier.GREEN_PROFITABLE -> Pair(StatusGreen, "🟢 RENTABLE")
        OfferSemaphoreTier.YELLOW_BREAK_EVEN -> Pair(StatusAmber, "🟡 EQUILIBRIO")
        OfferSemaphoreTier.RED_UNFAVORABLE -> Pair(StatusRed, "🔴 DESFAVORABLE")
    }

    Surface(
        modifier = modifier
            .width(320.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, tierColor, RoundedCornerShape(20.dp)),
        color = DeepObsidianSurface,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Fila de Cabecera: Plataforma + Semáforo + Cerrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = evaluation.offer.platform.visualToken.backgroundColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = evaluation.offer.platform.displayName.uppercase(),
                        color = evaluation.offer.platform.visualToken.contentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = tierColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = tierLabel,
                        color = tierColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C2C2E))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Ganancia Limpia Gigante
            Column {
                Text(
                    text = "VALOR A COBRAR",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatCop(evaluation.estimatedNetEarnings),
                    color = tierColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Fila de Métricas: $/km | $/hora | Distancia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(label = "$ / km", value = formatCop(evaluation.earningsPerKm))
                MetricItem(label = "$ / hora", value = formatCop(evaluation.earningsPerHour))
                MetricItem(label = "Distancia", value = "%.1f km".format(evaluation.totalDistanceKm))
            }

            // Alertas secundarias
            if (evaluation.alerts.isNotEmpty()) {
                Text(
                    text = "⚠️ " + evaluation.alerts.joinToString(" • "),
                    color = StatusAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column {
        Text(text = label, color = TextSecondary, fontSize = 10.sp)
        Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatCop(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }
    return formatter.format(amount)
}
