// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/maintenance/components/MaintenanceAlertsCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tarjeta de alertas predictivas estilo KiloMeta. Muestra un semáforo mecánico
// del vehículo según los kilómetros restantes para cada servicio crítico.
// ==============================================================================

package com.ananapps.kilometa.presentation.maintenance.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.MaintenanceAlert
import com.ananapps.kilometa.domain.model.MaintenanceAlertLevel
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusAmber
import com.ananapps.kilometa.presentation.theme.StatusAmberContainer
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusGreenContainer
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.StatusRedContainer
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

@Composable
fun MaintenanceAlertsCard(
    alerts: List<MaintenanceAlert>,
    currentOdometer: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🚨 SEMÁFORO MECÁNICO PREVENTIVO",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
                Text(
                    text = "Odómetro: %,d km".format(currentOdometer),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (alerts.isEmpty()) {
                Text(
                    text = "Sin registros previos. Registra el primer mantenimiento para activar el semáforo predictivo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    alerts.forEach { alert ->
                        AlertItemRow(alert = alert)
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertItemRow(alert: MaintenanceAlert) {
    val (bgColor, textColor, borderCol) = when (alert.alertLevel) {
        MaintenanceAlertLevel.OK -> Triple(StatusGreenContainer, StatusGreen, StatusGreen.copy(alpha = 0.5f))
        MaintenanceAlertLevel.UPCOMING -> Triple(StatusAmberContainer, StatusAmber, StatusAmber.copy(alpha = 0.5f))
        MaintenanceAlertLevel.OVERDUE -> Triple(StatusRedContainer, StatusRed, StatusRed.copy(alpha = 0.5f))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = alert.type.icon)
                Column {
                    Text(
                        text = alert.type.displayName,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Vence en %,d km".format(alert.nextDueOdometer),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${alert.alertLevel.badgeIcon} ${alert.alertLevel.label}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
                val diffText = if (alert.kmRemaining >= 0) "Faltan %,d km".format(alert.kmRemaining) else "Excedido por %,d km".format(-alert.kmRemaining)
                Text(
                    text = diffText,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor
                )
            }
        }
    }
}
