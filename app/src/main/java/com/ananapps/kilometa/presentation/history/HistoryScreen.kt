// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/HistoryScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla principal de Historial y Auditoría Contable Bi-Combustible.
// Conecta exportación mediante Storage Access Framework (SAF) y difusión por mensajería.
// Cumple con ergonomía cockpit (touch target >= 48/56dp) y diseño Deep Obsidian.
// ==============================================================================

package com.ananapps.kilometa.presentation.history

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ananapps.kilometa.domain.model.CsvReport
import com.ananapps.kilometa.domain.model.HistoryFilterPeriod
import com.ananapps.kilometa.presentation.history.components.ContinuousHistoryRoadList
import com.ananapps.kilometa.presentation.history.components.ExportReportSheet
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

// ------------------------------------------------------------------------------
// BLOQUE 1: PANTALLA PRINCIPAL DE HISTORIAL Y EXPORTACIÓN CONTABLE
// ------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val context = LocalContext.current

    var showExportSheet by remember { mutableStateOf(false) }
    var pendingReport by remember { mutableStateOf<CsvReport?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Storage Access Framework (SAF) para persistencia local de CSV
    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { targetUri ->
            pendingReport?.let { report ->
                try {
                    context.contentResolver.openOutputStream(targetUri)?.use { stream ->
                        stream.write(report.content.toByteArray(Charsets.UTF_8))
                        stream.flush()
                    }
                    Toast.makeText(context, "Reporte guardado exitosamente", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    pendingReport = null
                    viewModel.resetExportState()
                }
            }
        } ?: run {
            pendingReport = null
            viewModel.resetExportState()
        }
    }

    // Efecto reactivo para disparar SAF o Intent de mensajería
    LaunchedEffect(exportState) {
        when (val state = exportState) {
            is ExportUiState.CsvReady -> {
                showExportSheet = false
                pendingReport = state.report
                createDocLauncher.launch(state.report.fileName)
            }
            is ExportUiState.ShareReady -> {
                showExportSheet = false
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, state.summary.subject)
                    putExtra(Intent.EXTRA_TEXT, state.summary.messageText)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir Reporte KiloMeta"))
                viewModel.resetExportState()
            }
            is ExportUiState.Error -> {
                showExportSheet = false
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetExportState()
            }
            else -> Unit
        }
    }

    val activeFilter = when (val state = uiState) {
        is HistoryUiState.Success -> state.selectedFilter
        is HistoryUiState.Empty -> state.selectedFilter
        else -> HistoryFilterPeriod.ALL
    }

    Surface(modifier = modifier.fillMaxSize(), color = DeepObsidianBackground) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showExportSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepObsidianSurfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Exportar 📤",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricBluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            FilterPeriodSelector(
                selectedFilter = activeFilter,
                onSelectFilter = { viewModel.onFilterSelected(it) }
            )
            Spacer(modifier = Modifier.height(14.dp))

            when (val state = uiState) {
                is HistoryUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ElectricBluePrimary)
                    }
                }
                is HistoryUiState.Empty -> EmptyHistoryView(selectedFilter = state.selectedFilter)
                is HistoryUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error: ${state.message}", color = TextSecondary)
                    }
                }
                is HistoryUiState.Success -> ContinuousHistoryRoadList(
                    shifts = state.shifts,
                    onDeleteShift = { viewModel.onDeleteShift(it) },
                    onEditShift = { id, start, end, notes -> viewModel.onUpdateShift(id, start, end, notes) },
                    onConsolidateShifts = { viewModel.onConsolidateShiftsForDay(it) }
                )
            }
        }
    }

    if (showExportSheet) {
        ExportReportSheet(
            sheetState = sheetState,
            initialPeriod = activeFilter,
            onDismiss = { showExportSheet = false },
            onExportCsv = { period -> viewModel.onExportCsvClicked(period) },
            onShareSummary = { period -> viewModel.onShareSummaryClicked(period) }
        )
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: SELECTOR DE PERÍODO TEMPORAL ERGONÓMICO
// ------------------------------------------------------------------------------
@Composable
private fun FilterPeriodSelector(
    selectedFilter: HistoryFilterPeriod,
    onSelectFilter: (HistoryFilterPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepObsidianSurface, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HistoryFilterPeriod.entries.forEach { period ->
            val isSelected = period == selectedFilter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) ElectricBluePrimary else DeepObsidianSurface)
                    .clickable { onSelectFilter(period) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 3: VISTA DE ESTADO VACÍO
// ------------------------------------------------------------------------------
@Composable
private fun EmptyHistoryView(selectedFilter: HistoryFilterPeriod) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Sin jornadas registradas",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "No se encontraron turnos cerrados en el filtro '${selectedFilter.displayName}'.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
