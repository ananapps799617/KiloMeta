// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/PocketLedgerDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modal flotante de cabina que presenta el Libro Mayor y Extracto Contable de un bolsillo específico (Orden #044).
// Ofrece selector semanal con flechas [ ◀ ] y [ ▶ ], balance neto del ciclo, desglose de ingresos y egresos,
// y la lista cronológica exhaustiva de movimientos con tipado estricto.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ananapps.kilometa.domain.model.PocketLedgerSummary
import com.ananapps.kilometa.presentation.reports.components.PeriodCycleStepper
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun PocketLedgerDialog(
    summary: PocketLedgerSummary?,
    onDismiss: () -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (summary == null) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .border(1.dp, DeepObsidianBorder.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = DeepObsidianSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Cabecera fija
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricBluePrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = summary.pocketType.icon, fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                text = summary.pocketType.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "LIBRO MAYOR & EXTRACTO",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = ElectricBluePrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar extracto",
                            tint = TextSecondary
                        )
                    }
                }

                // Navegaci?n de Ciclos Semanales (Retroactiva)
                PeriodCycleStepper(
                    title = summary.cycleTitle,
                    subtitle = summary.cycleSubtitle,
                    canGoNext = summary.canGoNext,
                    onPreviousClick = onPreviousWeek,
                    onNextClick = onNextWeek
                )

                // Resumen Financiero del Per?odo
                LedgerKpiCard(summary = summary)

                // Lista de Movimientos Contables
                Text(
                    text = "MOVIMIENTOS REGISTRADOS (${summary.transactions.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = TextSecondary
                )

                if (summary.transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DeepObsidianSurfaceVariant)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay carreras ni movimientos registrados en este período.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(summary.transactions, key = { it.id }) { item ->
                            TransactionRowItem(item = item)
                        }
                    }
                }

                // Bot?n de Cierre
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Cerrar Extracto",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
