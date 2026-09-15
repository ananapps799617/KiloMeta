// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/reports/ReportsScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla de Informes y Analíticas de alto nivel desacoplada del Historial.
// Renderiza la matriz KPI 2x2, gráfico de dona de costos, rendimiento multi-plataforma,
// auditoría bi-combustible y exportación de reportes SAF/mensajería.
// ==============================================================================

package com.ananapps.kilometa.presentation.reports

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ananapps.kilometa.domain.model.CsvReport
import com.ananapps.kilometa.domain.model.HistoryFilterPeriod
import com.ananapps.kilometa.presentation.history.ExportUiState
import com.ananapps.kilometa.presentation.history.components.BiFuelSavingsCard
import com.ananapps.kilometa.presentation.history.components.ExportReportSheet
import com.ananapps.kilometa.presentation.history.components.PlatformBreakdownCard
import com.ananapps.kilometa.presentation.reports.components.ExpensesDonutChartCard
import com.ananapps.kilometa.presentation.reports.components.KpiSummaryMatrix
import com.ananapps.kilometa.presentation.reports.components.RevenueVsCostsBarChart
import com.ananapps.kilometa.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val context = LocalContext.current

    var showExportSheet by remember { mutableStateOf(false) }
    var pendingReport by remember { mutableStateOf<CsvReport?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    Surface(modifier = modifier.fillMaxSize(), color = DeepObsidianBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera con Botón de Exportación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Informes & Analíticas",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showExportSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepObsidianSurfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Exportar 📤", style = MaterialTheme.typography.labelMedium, color = ElectricBluePrimary, fontWeight = FontWeight.Bold)
                }
            }

            // Selector de Período
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepObsidianSurface, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReportPeriod.entries.forEach { period ->
                    val isSelected = period == selectedPeriod
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ElectricBluePrimary else DeepObsidianSurface)
                            .clickable { viewModel.onPeriodSelected(period) },
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

            when (val state = uiState) {
                is ReportsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ElectricBluePrimary)
                    }
                }
                is ReportsUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Sin registros para ${state.selectedPeriod.displayName}", color = TextSecondary)
                    }
                }
                is ReportsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = StatusRed)
                    }
                }
                is ReportsUiState.Success -> {
                    // 1. Matriz KPI 2x2
                    KpiSummaryMatrix(data = state.kpiData)

                    // 2. Gráfica de Barras Comparativa (Producido vs. Costos)
                    RevenueVsCostsBarChart(
                        grossIncome = state.kpiData.grossIncome,
                        totalCosts = state.kpiData.totalCosts
                    )

                    // 3. Gráfico de Dona de Costos
                    ExpensesDonutChartCard(slices = state.expenseSlices)

                    // 4. Rendimiento Multi-Plataforma
                    if (state.platformBreakdowns.isNotEmpty()) {
                        PlatformBreakdownCard(breakdowns = state.platformBreakdowns)
                    }

                    // 5. Auditoría Bi-Combustible GNV vs Gasolina
                    BiFuelSavingsCard(metrics = state.biFuelSavings)
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (showExportSheet) {
        ExportReportSheet(
            sheetState = sheetState,
            initialPeriod = HistoryFilterPeriod.ALL,
            onDismiss = { showExportSheet = false },
            onExportCsv = { viewModel.onExportCsvClicked() },
            onShareSummary = { viewModel.onShareSummaryClicked() }
        )
    }
}
