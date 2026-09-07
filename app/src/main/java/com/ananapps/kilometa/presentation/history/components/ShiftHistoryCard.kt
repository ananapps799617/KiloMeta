// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/ShiftHistoryCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tarjeta individual para representar una jornada de conducción finalizada (COMPLETED).
// Expone fecha, duración horaria, badge de color del semáforo financiero y métricas de neto y $/km.
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ObsidianBorder
import com.ananapps.kilometa.presentation.theme.StatusAmber
import com.ananapps.kilometa.presentation.theme.StatusAmberContainer
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusGreenContainer
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.StatusRedContainer
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: TARJETA DE TURNO CERRADO
// ------------------------------------------------------------------------------
/**
 * Renderiza el resumen auditado de una jornada concluida.
 *
 * @param shift Resumen consolidado del turno finalizado.
 * @param modifier Modificador Compose.
 */
@Composable
fun ShiftHistoryCard(
    shift: ShiftFinancialSummary,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    val dateFormatter = SimpleDateFormat("EEE, d MMM yyyy", Locale("es", "CO"))
    val shiftDate = dateFormatter.format(Date(shift.startTime))

    // Cálculo de duración de turno
    val durationMillis = if (shift.endTime != null && shift.endTime > shift.startTime) {
        shift.endTime - shift.startTime
    } else {
        0L
    }
    val hours = durationMillis / (1000 * 60 * 60)
    val minutes = (durationMillis / (1000 * 60)) % 60
    val durationLabel = if (hours > 0) "${hours}h ${minutes}m" else "${minutes} min"

    val (badgeText, badgeColor, badgeContainer) = when {
        shift.isQuotaAchieved -> Triple("TURNO LIBRE", StatusGreen, StatusGreenContainer)
        shift.quotaProgressPercentage >= 85.0 -> Triple("EN RUTA", StatusAmber, StatusAmberContainer)
        else -> Triple("DÉFICIT", StatusRed, StatusRedContainer)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Fila superior: Fecha, duración y Badge del Semáforo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = shiftDate.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Jornada: $durationLabel · ${shift.tripsCount} carreras",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .background(badgeContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fila de Métricas Principales: Ganancia Neta y Rendimiento por Km
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepObsidianSurfaceVariant, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ganancia Neta",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = currencyFormat.format(shift.netProfit),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (shift.netProfit >= 0) StatusGreen else StatusRed,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Rendimiento",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${currencyFormat.format(shift.netIncomePerKilometer)}/km",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Distancia",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${shift.totalKilometers} km",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
