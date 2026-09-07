// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/WeeklyQuotaCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Componente de cabina que visualiza el progreso acumulado de la meta semanal ($500.000 COP).
// Ilustra la barra de avance, los días restantes hasta el corte del martes a las 23:59:59
// y el prorrateo sugerido por jornada para evitar descalces financieros en el hogar.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.WeeklyQuotaProgress
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: COMPONENTE DE LA BOLSA SEMANAL PRORRATEADA
// ------------------------------------------------------------------------------
/**
 * Tarjeta de seguimiento de la meta semanal de $500.000 COP con corte los martes.
 *
 * @param progress Avance calculado ([WeeklyQuotaProgress]).
 * @param modifier Modificador Compose.
 */
@Composable
fun WeeklyQuotaCard(
    progress: WeeklyQuotaProgress,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Fila de encabezado: Título y aviso de corte de liquidación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BOLSA SEMANAL",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )

                Text(
                    text = "Corte: Martes 23:59",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra de progreso visual
            LinearProgressIndicator(
                progress = { progress.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (progress.isWeeklyTargetAchieved) StatusGreen else ElectricBluePrimary,
                trackColor = DeepObsidianSurface.copy(alpha = 0.6f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Métricas de progreso: Acumulado / Meta y porcentaje
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatCop(progress.accumulatedSoFar)} / ${formatCop(progress.weeklyTarget)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )

                Text(
                    text = "${progress.progressPercentage.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (progress.isWeeklyTargetAchieved) StatusGreen else TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Desglose de días restantes y sugerido diario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${progress.daysRemaining} días restantes",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                Text(
                    text = "Sugerido: ${formatCop(progress.suggestedDailyQuota)} / día",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: AYUDANTE DE FORMATEO LOCALIZADO
// ------------------------------------------------------------------------------
private fun formatCop(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return String.format(Locale.GERMANY, "$%,d", rounded)
}
