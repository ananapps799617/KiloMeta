// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/CockpitOperationalKpisRow.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Fila de 3 mini-tarjetas de indicadores operativos de cabina (Horas hoy,
// Kilómetros y Rendimiento $/km). Diseño simétrico con icono verde superior,
// etiqueta gris intermedia y valor blanco bold inferior (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.DayContinuousSummary
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun CockpitOperationalKpisRow(
    summary: DayContinuousSummary,
    modifier: Modifier = Modifier
) {
    val hoursDecimal = if (summary.firstEventTimestamp != null && summary.lastEventTimestamp != null && summary.lastEventTimestamp >= summary.firstEventTimestamp) {
        val diffMs = summary.lastEventTimestamp - summary.firstEventTimestamp
        maxOf(diffMs.toDouble() / 3600000.0, 0.1)
    } else {
        0.0
    }

    val hoursFormatted = if (hoursDecimal > 0.0) {
        String.format(Locale.US, "%.1f h", hoursDecimal)
    } else {
        "0.0 h"
    }

    // Rendimiento Operativo Real en Carretera (Orden #070)
    val ratePerKm = if (summary.distanceTraversedKm > 0) {
        summary.netOperationalMargin / summary.distanceTraversedKm
    } else {
        0.0
    }

    val rateFormatted = if (summary.distanceTraversedKm > 0) {
        if (ratePerKm >= 0.0) {
            "$ ${String.format(Locale.US, "%,.0f", ratePerKm)}/km"
        } else {
            "-$ ${String.format(Locale.US, "%,.0f", kotlin.math.abs(ratePerKm))}/km"
        }
    } else {
        "$ 0/km"
    }

    val rateColor = when {
        ratePerKm > 0.0 -> StatusGreen
        ratePerKm < 0.0 -> StatusRed
        else -> TextPrimary
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Tarjeta 1: Horas hoy
        KpiMiniCard(
            icon = "⏱️",
            label = "Horas hoy",
            value = hoursFormatted,
            modifier = Modifier.weight(1f)
        )

        // Tarjeta 2: Kilómetros
        KpiMiniCard(
            icon = "🛣️",
            label = "Kilómetros",
            value = "${summary.distanceTraversedKm} km",
            modifier = Modifier.weight(1f)
        )

        // Tarjeta 3: Rendimiento
        KpiMiniCard(
            icon = "📈",
            label = "Rendimiento",
            value = rateFormatted,
            valueColor = rateColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun KpiMiniCard(
    icon: String,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 84.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DeepObsidianSurface)
            .border(1.dp, DeepObsidianBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = icon,
                fontSize = 16.sp
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = valueColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
