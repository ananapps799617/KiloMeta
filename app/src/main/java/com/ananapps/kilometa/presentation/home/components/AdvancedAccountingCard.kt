// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/AdvancedAccountingCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Dashboard visual de Auditoría Contable Avanzada en cabina (Deep Obsidian).
// Muestra barra semafórica de cuota semanal (12dp), grid de 3 KPIs con touch targets >= 56dp:
// $/km real, eficiencia bi-combustible con chip Cyan y Sello Cloud con feedback háptico.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.AccountingAuditSummary
import com.ananapps.kilometa.presentation.theme.CyanSavings
import com.ananapps.kilometa.presentation.theme.CyanSavingsContainer
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ObsidianBorder
import com.ananapps.kilometa.presentation.theme.StatusAmber
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: COMPONENTE PRINCIPAL DE AUDITORÍA CONTABLE AVANZADA
// ------------------------------------------------------------------------------
@Composable
fun AdvancedAccountingCard(
    auditSummary: AccountingAuditSummary,
    modifier: Modifier = Modifier,
    onKpiClick: (kpiTitle: String) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val progress = auditSummary.quotaProgress

    // Determinación del color semafórico de la cuota
    val quotaColor = when {
        progress.progressPercentage >= 100.0 -> StatusGreen
        progress.progressPercentage >= 60.0 -> StatusAmber
        else -> StatusRed
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ObsidianBorder))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Encabezado con estado del ciclo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AUDITORÍA DE PRODUCCIÓN",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Text(
                    text = "Corte Martes 23:59",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra Semafórica de Cuota Semanal (12dp de alto para cabina vehicular)
            LinearProgressIndicator(
                progress = { progress.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = quotaColor,
                trackColor = DeepObsidianSurfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Indicador de avance porcentual y bolsa acumulada
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatCop(progress.accumulatedSoFar)} / ${formatCop(progress.weeklyTarget)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Text(
                    text = "${progress.progressPercentage.toInt()}% Meta",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = quotaColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid de 3 KPIs operacionales con touch targets >= 56dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // KPI 1: Costo / Ingreso por Kilómetro
                KpiItemBox(
                    title = "$/km Real",
                    mainValue = formatCopPerKm(auditSummary.costPerKilometer),
                    subValue = "Ing: ${formatCopPerKm(auditSummary.revenuePerKilometer)}",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKpiClick("$/km Real")
                    }
                )

                // KPI 2: Eficiencia Bi-Combustible
                KpiItemBox(
                    title = "Combustible",
                    mainValue = String.format(Locale.US, "%.1f%%", auditSummary.fuelExpenseToIncomeRatio),
                    chipText = if (auditSummary.isFuelEfficiencyOptimal) "Óptimo" else "Revisar",
                    chipColor = if (auditSummary.isFuelEfficiencyOptimal) CyanSavings else StatusAmber,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKpiClick("Eficiencia Combustible")
                    }
                )

                // KPI 3: Sello Cloud Firestore
                KpiItemBox(
                    title = "Sello Cloud",
                    mainValue = if (auditSummary.isCloudSyncHealthy) "Protegido" else "Pendiente",
                    subValue = if (auditSummary.isCloudSyncHealthy) "Firestore OK" else "Sin conexión",
                    valueColor = if (auditSummary.isCloudSyncHealthy) StatusGreen else StatusAmber,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onKpiClick("Sello Cloud")
                    }
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: ITEM DE KPI OPERACIONAL (TOUCH TARGET >= 56DP)
// ------------------------------------------------------------------------------
@Composable
private fun KpiItemBox(
    title: String,
    mainValue: String,
    modifier: Modifier = Modifier,
    subValue: String? = null,
    chipText: String? = null,
    chipColor: Color = CyanSavings,
    valueColor: Color = TextPrimary,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DeepObsidianSurfaceVariant)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = mainValue,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = valueColor
            )

            if (chipText != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyanSavingsContainer)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = chipText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = chipColor
                    )
                }
            } else if (subValue != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 3: FUNCIONES DE FORMATEO LOCALIZADO
// ------------------------------------------------------------------------------
private fun formatCop(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return String.format(Locale.GERMANY, "$%,d", rounded)
}

private fun formatCopPerKm(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return String.format(Locale.GERMANY, "$%,d/km", rounded)
}
