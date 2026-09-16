// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/HistoryScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla principal de Historial y Auditoría Contable Bi-Combustible.
// Conecta exportación mediante Storage Access Framework (SAF) y difusión por mensajería.
// Cumple con ergonomía cockpit (touch target >= 48/56dp) y diseño Deep Obsidian.
// ==============================================================================

package com.ananapps.kilometa.presentation.history

import android.app.DatePickerDialog
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ananapps.kilometa.domain.model.*
import com.ananapps.kilometa.presentation.history.components.*
import com.ananapps.kilometa.presentation.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val lastKnownOdometer by viewModel.lastKnownOdometer.collectAsState()
    val vehicleProfile by viewModel.vehicleProfile.collectAsState()
    val context = LocalContext.current

    var showExportSheet by remember { mutableStateOf(false) }
    var editingTrip by remember { mutableStateOf<Trip?>(null) }
    var editingFueling by remember { mutableStateOf<Fueling?>(null) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var editingMaintenance by remember { mutableStateOf<MaintenanceRecord?>(null) }
    var editingDayMetrics by remember { mutableStateOf<DayHistorySummary?>(null) }

    var registeringTripTimestamp by remember { mutableStateOf<Long?>(null) }
    var registeringFuelingTimestamp by remember { mutableStateOf<Long?>(null) }
    var registeringExpenseTimestamp by remember { mutableStateOf<Long?>(null) }
    var registeringMaintenanceTimestamp by remember { mutableStateOf<Long?>(null) }
    var showActionChoiceForDate by remember { mutableStateOf<Long?>(null) }

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

    val activeFilter = when (val state = uiState) {
        is HistoryUiState.Success -> state.selectedFilter
        is HistoryUiState.Empty -> state.selectedFilter
        else -> HistoryFilterPeriod.ALL
    }

    val onOpenPastDatePicker = {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, y, m, d ->
                val chosen = Calendar.getInstance().apply { set(y, m, d, 12, 0, 0) }
                showActionChoiceForDate = chosen.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpenPastDatePicker,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepObsidianSurfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "📅 + Fecha", style = MaterialTheme.typography.labelMedium, color = ElectricBluePrimary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showExportSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepObsidianSurfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Exportar 📤", style = MaterialTheme.typography.labelMedium, color = ElectricBluePrimary, fontWeight = FontWeight.Bold)
                    }
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
                    days = state.days,
                    onEditTrip = { editingTrip = it },
                    onEditFueling = { editingFueling = it },
                    onEditExpense = { editingExpense = it },
                    onEditMaintenance = { editingMaintenance = it },
                    onDeleteTrip = { viewModel.onDeleteTrip(it) },
                    onDeleteFueling = { viewModel.onDeleteFueling(it) },
                    onDeleteExpense = { viewModel.onDeleteExpense(it) },
                    onDeleteMaintenance = { viewModel.onDeleteMaintenance(it) },
                    onAddTrip = { registeringTripTimestamp = it },
                    onAddFueling = { registeringFuelingTimestamp = it },
                    onAddExpense = { registeringExpenseTimestamp = it },
                    onAddMaintenance = { registeringMaintenanceTimestamp = it },
                    onEditDayMetrics = { editingDayMetrics = it }
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

    showActionChoiceForDate?.let { dateMillis ->
        PastDateActionPickerDialog(
            dateMillis = dateMillis,
            onSelectAction = { type ->
                showActionChoiceForDate = null
                when (type) {
                    HistoryActivityType.TRIP -> registeringTripTimestamp = dateMillis
                    HistoryActivityType.FUELING -> registeringFuelingTimestamp = dateMillis
                    HistoryActivityType.EXPENSE -> registeringExpenseTimestamp = dateMillis
                    HistoryActivityType.MAINTENANCE -> registeringMaintenanceTimestamp = dateMillis
                }
            },
            onDismiss = { showActionChoiceForDate = null }
        )
    }

    HistoryDialogsHost(
        editingTrip = editingTrip,
        editingFueling = editingFueling,
        editingExpense = editingExpense,
        editingMaintenance = editingMaintenance,
        editingDayMetrics = editingDayMetrics,
        registeringTripTimestamp = registeringTripTimestamp,
        registeringFuelingTimestamp = registeringFuelingTimestamp,
        registeringExpenseTimestamp = registeringExpenseTimestamp,
        registeringMaintenanceTimestamp = registeringMaintenanceTimestamp,
        lastKnownOdometer = lastKnownOdometer,
        vehicleProfile = vehicleProfile,
        onDismissTripEdit = { editingTrip = null },
        onDismissFuelingEdit = { editingFueling = null },
        onDismissExpenseEdit = { editingExpense = null },
        onDismissMaintenanceEdit = { editingMaintenance = null },
        onDismissDayMetricsEdit = { editingDayMetrics = null },
        onDismissRegisterTrip = { registeringTripTimestamp = null },
        onDismissRegisterFueling = { registeringFuelingTimestamp = null },
        onDismissRegisterExpense = { registeringExpenseTimestamp = null },
        onDismissRegisterMaintenance = { registeringMaintenanceTimestamp = null },
        onUpdateTrip = { viewModel.onUpdateTrip(it); editingTrip = null },
        onUpdateFueling = { viewModel.onUpdateFueling(it); editingFueling = null },
        onUpdateExpense = { viewModel.onUpdateExpense(it); editingExpense = null },
        onUpdateMaintenance = { viewModel.onUpdateMaintenance(it); editingMaintenance = null },
        onUpdateDayMetrics = { sOdo, eOdo, sTime, eTime ->
            editingDayMetrics?.let { day ->
                viewModel.onUpdateDayMetrics(day.dateMillis, sOdo, eOdo, sTime, eTime)
            }
            editingDayMetrics = null
        },
        onConfirmAddTrip = { plat, gross, comm, pay, dest, time, sOdo, eOdo ->
            viewModel.onAddTrip(plat, gross, comm, pay, dest, time, sOdo, eOdo)
            registeringTripTimestamp = null
        },
        onConfirmAddFueling = { type, amount, vol, odo, src, time ->
            viewModel.onAddFueling(type, amount, vol, odo, src, time)
            registeringFuelingTimestamp = null
        },
        onConfirmAddExpense = { concept, amount, src, time ->
            viewModel.onAddExpense(concept, amount, src, time)
            registeringExpenseTimestamp = null
        },
        onConfirmAddMaintenance = { type, odo, cost, notes, src, title, loc, time ->
            viewModel.onAddMaintenance(type, odo, cost, notes, src, title, loc, time)
            registeringMaintenanceTimestamp = null
        }
    )
}
