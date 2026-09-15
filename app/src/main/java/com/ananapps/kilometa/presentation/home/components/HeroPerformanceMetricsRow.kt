// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/HeroPerformanceMetricsRow.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Fila de telemetría de rendimiento reactivo para la tarjeta Hero (Orden #038).
// Calcula en tiempo real la ganancia neta por km recorrido ($/km) y por hora
// trabajada ($/h) con base en la cobertura viva del día.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun HeroPerformanceMetricsRow(
    pocketNet: Double,
    distanceKm: Int,
    elapsedHoursFormatted: String,
    firstTimestamp: Long?,
    lastTimestamp: Long?,
    modifier: Modifier = Modifier
) {
    val hoursDecimal = if (firstTimestamp != null && lastTimestamp != null && lastTimestamp >= firstTimestamp) {
        val diffMs = lastTimestamp - firstTimestamp
        maxOf(diffMs.toDouble() / 3600000.0, 0.1)
    } else {
        0.0
    }

    val perKm = if (distanceKm > 0) pocketNet / distanceKm else 0.0
    val perHour = if (hoursDecimal > 0.0) pocketNet / hoursDecimal else 0.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DeepObsidianSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Fila 1: Cobertura Horaria y Kilometraje
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏱️ Cobertura de Hoy",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = ElectricBluePrimary
            )
            Text(
                text = "$elapsedHoursFormatted · $distanceKm km",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                ),
                color = TextPrimary
            )
        }

        // Fila 2: Rentabilidad por Distancia ($/km) y por Tiempo ($/h)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Rent. Distancia:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "$ ${String.format(Locale.US, "%,.0f", perKm)} / km",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = if (perKm >= 0) StatusGreen else StatusRed
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Rent. Tiempo:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "$ ${String.format(Locale.US, "%,.0f", perHour)} / h",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = if (perHour >= 0) StatusGreen else StatusRed
                )
            }
        }
    }
}
