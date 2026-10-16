// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/DayHeroCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tarjeta maestra "En tu bolsillo hoy" Cockpit Dark (99.9% fiel a la lámina de diseño).
// Presenta el neto libre en bolsillo (pocketNet), la meta diaria con anillo circular
// de porcentaje y la barra lineal de avance esmeralda (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.DayContinuousSummary
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun DayHeroCard(
    summary: DayContinuousSummary,
    vehicle: VehicleProfile,
    modifier: Modifier = Modifier
) {
    val dailyTarget = if (summary.dailyQuotaTarget > 0.0) {
        summary.dailyQuotaTarget
    } else if (vehicle.workDaysWeek > 0 && vehicle.quotaAmount > 0.0) {
        vehicle.quotaAmount / vehicle.workDaysWeek
    } else {
        0.0
    }

    // Breakeven canónico de la Orden #038: meta cumplida cuando pocketNet >= 0
    val isDailyQuotaMet = summary.pocketNet >= 0.0 || dailyTarget == 0.0

    val operatingCovered = if (summary.netOperationalMargin > 0.0) {
        summary.netOperationalMargin
    } else {
        maxOf(0.0, summary.pocketNet + dailyTarget)
    }

    val quotaProgress = if (dailyTarget > 0.0) {
        (operatingCovered / dailyTarget).toFloat().coerceIn(0f, 1f)
    } else {
        1f
    }

    val percentInt = (quotaProgress * 100).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, DeepObsidianBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Etiqueta superior
            Text(
                text = "En tu bolsillo hoy",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = TextSecondary
            )

            // 2. Fila Principal: Cifra en blanco + Bloque de Meta Diaria y Anillo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Columna izquierda: Importe libre gigante en blanco + COP
                Column {
                    Text(
                        text = if (summary.pocketNet < 0.0) {
                            "-$ ${String.format(Locale.US, "%,.0f", -summary.pocketNet)}"
                        } else {
                            "$ ${String.format(Locale.US, "%,.0f", summary.pocketNet)}"
                        },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = if (summary.pocketNet >= 0.0) TextPrimary else StatusRed,
                        letterSpacing = (-0.5).sp
                    )

                    Text(
                        text = "COP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = TextSecondary
                    )
                }

                // Bloque derecho: Meta Diaria + Anillo de Porcentaje
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Meta diaria",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                        Text(
                            text = "$ ${String.format(Locale.US, "%,.0f", dailyTarget)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            ),
                            color = TextPrimary
                        )
                    }

                    // Anillo de Progreso Circular con Porcentaje en el Centro
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(50.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { quotaProgress },
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.5.dp,
                            color = if (isDailyQuotaMet) StatusGreen else StatusGreen.copy(alpha = 0.85f),
                            trackColor = DeepObsidianSurfaceVariant
                        )
                        Text(
                            text = "$percentInt%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = TextPrimary
                        )
                    }
                }
            }

            // 3. Barra de Progreso Lineal Horizontal Continua
            LinearProgressIndicator(
                progress = { quotaProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = StatusGreen,
                trackColor = DeepObsidianSurfaceVariant
            )
        }
    }
}
