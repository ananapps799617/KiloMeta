// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/HeroDailyQuotaThermometer.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Termómetro de cabina para la meta diaria prorrateada (Orden #038).
// Divide la cuota semanal entre los días laborados configurados y señala
// en verde esmeralda neón cuando el conductor entra en "Turno Libre".
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
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
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun HeroDailyQuotaThermometer(
    pocketNet: Double,
    vehicle: VehicleProfile,
    modifier: Modifier = Modifier
) {
    val dailyTarget = if (vehicle.workDaysWeek > 0) vehicle.quotaAmount / vehicle.workDaysWeek else 0.0
    val isDailyQuotaMet = pocketNet >= dailyTarget && dailyTarget > 0.0
    val progress = if (dailyTarget > 0.0) (pocketNet / dailyTarget).toFloat().coerceIn(0f, 1f) else 0f
    val remaining = if (dailyTarget > pocketNet) dailyTarget - pocketNet else 0.0
    val surplus = if (pocketNet > dailyTarget) pocketNet - dailyTarget else 0.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDailyQuotaMet) StatusGreen.copy(alpha = 0.12f) else DeepObsidianSurfaceVariant)
            .border(
                1.dp,
                if (isDailyQuotaMet) StatusGreen.copy(alpha = 0.4f) else DeepObsidianBorder.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isDailyQuotaMet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🟢 ¡CUOTA DE HOY CUMPLIDA! · EN TURNO LIBRE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = StatusGreen
                )
                if (surplus > 0.0) {
                    Text(
                        text = "+$ ${String.format(Locale.US, "%,.0f", surplus)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = StatusGreen
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 Meta de Hoy (${vehicle.workDaysWeek}d/sem): $ ${String.format(Locale.US, "%,.0f", dailyTarget)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary
                )
                Text(
                    text = "Faltan: $ ${String.format(Locale.US, "%,.0f", remaining)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = StatusAmber
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = StatusAmber,
                trackColor = DeepObsidianBorder
            )
        }
    }
}
