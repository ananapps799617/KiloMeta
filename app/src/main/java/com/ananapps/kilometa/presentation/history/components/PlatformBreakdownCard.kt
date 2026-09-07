// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/PlatformBreakdownCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Componente de interfaz que renderiza la barra segmentada multi-color y la tabla de
// retención de comisiones por plataforma (Uber blanco, DiDi naranja, InDrive lima, Particular morado).
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.PlatformAnalytics
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ObsidianBorder
import com.ananapps.kilometa.presentation.theme.PlatformDiDi
import com.ananapps.kilometa.presentation.theme.PlatformInDrive
import com.ananapps.kilometa.presentation.theme.PlatformPrivate
import com.ananapps.kilometa.presentation.theme.PlatformUber
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: TARJETA DE RENDIMIENTO MULTI-PLATAFORMA
// ------------------------------------------------------------------------------
/**
 * Renderiza la barra segmentada y tabla de auditoría contable por plataforma intermediaria.
 *
 * @param breakdowns Lista de métricas agregadas por cada plataforma.
 * @param modifier Modificador Compose.
 */
@Composable
fun PlatformBreakdownCard(
    breakdowns: List<PlatformAnalytics>,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    val activePlatforms = breakdowns.filter { it.tripsCount > 0 || it.grossIncome > 0.0 }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MIX DE PLATAFORMAS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${breakdowns.sumOf { it.tripsCount }} servicios",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barra Segmentada de Participación
            if (activePlatforms.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(DeepObsidianSurfaceVariant)
                ) {
                    activePlatforms.forEach { platform ->
                        val weight = (platform.revenueSharePercentage / 100.0).toFloat().coerceAtLeast(0.02f)
                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .height(14.dp)
                                .background(getPlatformColor(platform.platformType))
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(DeepObsidianSurfaceVariant)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabla de Retención y Márgenes
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                breakdowns.forEachIndexed { index, item ->
                    PlatformRowItem(
                        analytics = item,
                        currencyFormat = currencyFormat
                    )
                    if (index < breakdowns.lastIndex) {
                        HorizontalDivider(color = ObsidianBorder, thickness = 0.8.dp)
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: RENGLÓN DETALLADO DE PLATAFORMA
// ------------------------------------------------------------------------------
@Composable
private fun PlatformRowItem(
    analytics: PlatformAnalytics,
    currencyFormat: NumberFormat
) {
    val platformColor = getPlatformColor(analytics.platformType)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(platformColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = analytics.platformType.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${analytics.tripsCount} carreras · Ticket ${currencyFormat.format(analytics.averageTicket)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = currencyFormat.format(analytics.netIncome),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            val commissionLabel = if (analytics.commissionPaid > 0.0) {
                "-${currencyFormat.format(analytics.commissionPaid)} (${String.format(Locale.US, "%.1f", analytics.commissionRatePercentage)}%)"
            } else {
                "0% retención"
            }
            Text(
                text = commissionLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (analytics.commissionPaid > 0.0) StatusRed else TextSecondary
            )
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 3: ASIGNACIÓN DE COLOR IDENTITARIO POR PLATAFORMA
// ------------------------------------------------------------------------------
private fun getPlatformColor(platformType: PlatformType): Color {
    return when (platformType) {
        PlatformType.UBER -> PlatformUber
        PlatformType.DIDI -> PlatformDiDi
        PlatformType.INDRIVE -> PlatformInDrive
        PlatformType.PRIVATE -> PlatformPrivate
    }
}
