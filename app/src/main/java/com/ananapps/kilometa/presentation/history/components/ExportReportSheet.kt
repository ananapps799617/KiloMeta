// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/ExportReportSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet ergonómico para la selección del formato contable (CSV vs Mensajería)
// y alcance temporal (Semana, Mes, Historial Completo). Cumple ergonomía de cabina (>=56dp)
// y diseño cockpit dark (Deep Obsidian).
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.HistoryFilterPeriod
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.ObsidianBorder
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

// ------------------------------------------------------------------------------
// BLOQUE 1: ENUMERACIÓN INTERNA DE MODALIDAD DE EXPORTACIÓN
// ------------------------------------------------------------------------------
enum class ExportFormat(val title: String, val subtitle: String, val icon: String) {
    CSV("CSV Contable", "Excel / Google Sheets con BOM UTF-8", "📊"),
    SHARE("Resumen Mensajería", "WhatsApp / Telegram con emojis", "💬")
}

// ------------------------------------------------------------------------------
// BLOQUE 2: MODAL BOTTOM SHEET DE EXPORTACIÓN CONTABLE
// ------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportSheet(
    sheetState: SheetState,
    initialPeriod: HistoryFilterPeriod,
    onDismiss: () -> Unit,
    onExportCsv: (HistoryFilterPeriod) -> Unit,
    onShareSummary: (HistoryFilterPeriod) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedFormat by remember { mutableStateOf(ExportFormat.CSV) }
    var selectedPeriod by remember { mutableStateOf(initialPeriod) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidianSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Exportar Reportes Financieros",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Descarga o comparte la auditoría contable de tus jornadas",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Selector de Formato
            Text(
                text = "1. Selecciona el Formato",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExportFormat.entries.forEach { format ->
                    val isSelected = format == selectedFormat
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedFormat = format
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) DeepObsidianSurfaceVariant else DeepObsidianSurface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) ElectricBluePrimary else ObsidianBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = format.icon, style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = format.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isSelected) ElectricBluePrimary else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = format.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Selector de Alcance Temporal
            Text(
                text = "2. Selecciona el Alcance",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepObsidianSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HistoryFilterPeriod.entries.forEach { period ->
                    val isSelected = period == selectedPeriod
                    val label = when (period) {
                        HistoryFilterPeriod.WEEK -> "Semana Actual"
                        HistoryFilterPeriod.MONTH -> "Mes Actual"
                        HistoryFilterPeriod.ALL -> "Todo el Historial"
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ElectricBluePrimary else DeepObsidianSurfaceVariant)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedPeriod = period
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Principal de Acción Ergonómico (>=56dp)
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    when (selectedFormat) {
                        ExportFormat.CSV -> onExportCsv(selectedPeriod)
                        ExportFormat.SHARE -> onShareSummary(selectedPeriod)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                val actionTitle = if (selectedFormat == ExportFormat.CSV) {
                    "Descargar Archivo CSV"
                } else {
                    "Compartir por WhatsApp / Telegram"
                }
                Text(
                    text = actionTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
