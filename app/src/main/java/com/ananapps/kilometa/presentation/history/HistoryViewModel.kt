// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/HistoryViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel para la pantalla de Historial y Analíticas (@HiltViewModel).
// Orquesta analíticas reactivas, exportación de reportes contables CSV (RFC 4180/BOM)
// y generación de resúmenes enriquecidos con emojis para WhatsApp/Telegram.
// ==============================================================================

package com.ananapps.kilometa.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.*
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import com.ananapps.kilometa.domain.repository.FuelingRepository
import com.ananapps.kilometa.domain.repository.MaintenanceRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.repository.TripRepository
import com.ananapps.kilometa.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getShiftHistoryUseCase: GetShiftHistoryUseCase,
    private val generateCsvReportUseCase: GenerateCsvReportUseCase,
    private val generateShareableSummaryUseCase: GenerateShareableSummaryUseCase,
    private val deleteShiftUseCase: DeleteShiftUseCase,
    private val consolidateDayShiftsUseCase: ConsolidateDayShiftsUseCase,
    private val updateShiftUseCase: UpdateShiftUseCase,
    private val shiftRepository: ShiftRepository,
    private val driverProfileRepository: DriverProfileRepository,
    private val tripRepository: TripRepository,
    private val fuelingRepository: FuelingRepository,
    private val expenseRepository: ExpenseRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val deleteTripUseCase: DeleteTripUseCase,
    private val deleteFuelingUseCase: DeleteFuelingUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val updateFuelingUseCase: UpdateFuelingUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val updateMaintenanceUseCase: UpdateMaintenanceUseCase,
    private val historyDataProcessor: HistoryDataProcessor,
    private val manageShiftUseCase: ManageShiftUseCase,
    private val registerTripUseCase: RegisterTripUseCase,
    private val registerFuelingUseCase: RegisterFuelingUseCase,
    private val registerExpenseUseCase: RegisterExpenseUseCase,
    private val registerMaintenanceUseCase: RegisterMaintenanceUseCase,
    private val getLastKnownOdometerUseCase: GetLastKnownOdometerUseCase
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(HistoryFilterPeriod.ALL)

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    val lastKnownOdometer: StateFlow<Int> = getLastKnownOdometerUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    val vehicleProfile: StateFlow<VehicleProfile> = driverProfileRepository.getVehicleProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), VehicleProfile())

    val uiState: StateFlow<HistoryUiState> = combine(
        _selectedFilter,
        shiftRepository.getAllTrips(),
        shiftRepository.getAllFuelings(),
        expenseRepository.getAllExpenses(),
        maintenanceRepository.getAllMaintenances(),
        driverProfileRepository.getVehicleProfile(),
        getShiftHistoryUseCase()
    ) { args: Array<Any?> ->
        val filter = args[0] as HistoryFilterPeriod
        val trips = args[1] as List<Trip>
        val fuelings = args[2] as List<Fueling>
        val expenses = args[3] as List<Expense>
        val maintenances = args[4] as List<MaintenanceRecord>
        val vehicle = args[5] as VehicleProfile
        val completedShifts = args[6] as List<ShiftFinancialSummary>

        historyDataProcessor.process(filter, completedShifts, trips, fuelings, expenses, maintenances, vehicle)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = HistoryUiState.Loading
    )

    fun onFilterSelected(filter: HistoryFilterPeriod) { _selectedFilter.value = filter }

    fun onExportCsvClicked(period: HistoryFilterPeriod) = viewModelScope.launch {
        _exportState.value = ExportUiState.Exporting
        try {
            val allShifts = getShiftHistoryUseCase().first()
            val vehicle = driverProfileRepository.getVehicleProfile().first()
            val cutoffMillis = historyDataProcessor.calculatePeriodCutoff(period, vehicle)
            val filteredShifts = if (cutoffMillis > 0L) allShifts.filter { it.startTime >= cutoffMillis } else allShifts
            _exportState.value = ExportUiState.CsvReady(generateCsvReportUseCase(period, filteredShifts))
        } catch (e: Exception) {
            _exportState.value = ExportUiState.Error(e.localizedMessage ?: "Error al compilar CSV")
        }
    }

    fun onShareSummaryClicked(period: HistoryFilterPeriod) = viewModelScope.launch {
        _exportState.value = ExportUiState.Exporting
        try {
            val allShifts = getShiftHistoryUseCase().first()
            val vehicle = driverProfileRepository.getVehicleProfile().first()
            val cutoffMillis = historyDataProcessor.calculatePeriodCutoff(period, vehicle)
            val filteredShifts = if (cutoffMillis > 0L) allShifts.filter { it.startTime >= cutoffMillis } else allShifts
            _exportState.value = ExportUiState.ShareReady(generateShareableSummaryUseCase(period, filteredShifts))
        } catch (e: Exception) {
            _exportState.value = ExportUiState.Error(e.localizedMessage ?: "Error al generar resumen")
        }
    }

    fun onShareShiftSummaryClicked(shift: ShiftFinancialSummary) = viewModelScope.launch {
        try {
            _exportState.value = ExportUiState.ShareReady(generateShareableSummaryUseCase(shift))
        } catch (e: Exception) {
            _exportState.value = ExportUiState.Error(e.localizedMessage ?: "Error al generar liquidación")
        }
    }

    fun resetExportState() { _exportState.value = ExportUiState.Idle }

    fun onDeleteShift(shiftId: String) = viewModelScope.launch { deleteShiftUseCase(shiftId) }

    fun onConsolidateShiftsForDay(shifts: List<ShiftFinancialSummary>) = viewModelScope.launch {
        val allShifts = shiftRepository.getAllShifts().first()
        val targetIds = shifts.map { it.shiftId }.toSet()
        val toConsolidate = allShifts.filter { it.id in targetIds }
        if (toConsolidate.size >= 2) consolidateDayShiftsUseCase(toConsolidate)
    }

    fun onUpdateShift(shiftId: String, startOdo: Int, endOdo: Int?, notes: String?) = viewModelScope.launch {
        val allShifts = shiftRepository.getAllShifts().first()
        val existing = allShifts.find { it.id == shiftId } ?: return@launch
        updateShiftUseCase(existing.copy(startOdometer = startOdo, endOdometer = endOdo, notes = notes))
    }

    fun onDeleteTrip(tripId: String) = viewModelScope.launch { deleteTripUseCase(tripId) }
    fun onDeleteFueling(fuelingId: String) = viewModelScope.launch { deleteFuelingUseCase(fuelingId) }
    fun onDeleteExpense(expenseId: String) = viewModelScope.launch { deleteExpenseUseCase(expenseId) }
    fun onDeleteMaintenance(id: String) = viewModelScope.launch { maintenanceRepository.deleteMaintenance(id) }

    fun onUpdateTrip(trip: Trip) = viewModelScope.launch { updateTripUseCase(trip) }
    fun onUpdateFueling(fueling: Fueling) = viewModelScope.launch { updateFuelingUseCase(fueling) }
    fun onUpdateExpense(expense: Expense) = viewModelScope.launch { updateExpenseUseCase(expense) }
    fun onUpdateMaintenance(maint: MaintenanceRecord) = viewModelScope.launch { updateMaintenanceUseCase(maint) }

    fun onUpdateDayMetrics(
        dateMillis: Long,
        startOdo: Int,
        endOdo: Int,
        startTimeMillis: Long,
        endTimeMillis: Long,
        breakMinutes: Int = 0
    ) = viewModelScope.launch {
        val shift = manageShiftUseCase.getOrCreateShiftForDay(timestamp = dateMillis, startOdometer = startOdo, endOdometer = endOdo)
        val existingNotes = shift.notes.orEmpty()
        val newNotes = if (existingNotes.contains("BREAKS:")) {
            existingNotes.replace(Regex("BREAKS:\\d+"), "BREAKS:$breakMinutes")
        } else if (existingNotes.isBlank()) {
            "BREAKS:$breakMinutes"
        } else {
            "$existingNotes | BREAKS:$breakMinutes"
        }
        updateShiftUseCase(
            shift.copy(
                startOdometer = startOdo,
                endOdometer = endOdo,
                startTime = startTimeMillis,
                endTime = endTimeMillis,
                notes = newNotes
            )
        )
    }

    fun onAddTrip(
        platform: PlatformType, gross: Double, commission: Double, payment: PaymentMethod,
        dest: PaymentDestination, timestamp: Long, startOdo: Int?, endOdo: Int?,
        distanceKm: Double? = null, durationMinutes: Int? = null, platformPromoDiscount: Double = 0.0
    ) = viewModelScope.launch {
        val shift = manageShiftUseCase.getOrCreateShiftForDay(timestamp = timestamp, startOdometer = startOdo ?: 0, endOdometer = endOdo ?: 0)
        registerTripUseCase(
            shiftId = shift.id, platformType = platform, grossAmount = gross, commissionAmount = commission,
            paymentMethod = payment, paymentDestination = dest, timestamp = timestamp,
            startOdometer = startOdo, endOdometer = endOdo,
            distanceKm = distanceKm, durationMinutes = durationMinutes,
            platformPromoDiscount = platformPromoDiscount
        )
    }

    fun onAddFueling(
        type: FuelType,
        amount: Double,
        volume: Double,
        odo: Int,
        source: WalletType,
        timestamp: Long
    ) = viewModelScope.launch {
        val shift = manageShiftUseCase.getOrCreateShiftForDay(timestamp = timestamp)
        registerFuelingUseCase(
            shiftId = shift.id,
            fuelType = type,
            amountPaid = amount,
            volume = volume,
            odometer = odo,
            timestamp = timestamp,
            paymentSource = source
        )
    }

    fun onAddExpense(
        concept: ExpenseConcept,
        amount: Double,
        source: WalletType,
        timestamp: Long
    ) = viewModelScope.launch {
        val shift = manageShiftUseCase.getOrCreateShiftForDay(timestamp = timestamp)
        registerExpenseUseCase(
            shiftId = shift.id,
            concept = concept,
            amount = amount,
            timestamp = timestamp,
            paymentSource = source
        )
    }

    fun onAddMaintenance(
        type: MaintenanceType,
        odo: Int,
        cost: Double,
        notes: String?,
        source: WalletType,
        title: String?,
        location: String?,
        timestamp: Long
    ) = viewModelScope.launch {
        val vehicle = driverProfileRepository.getVehicleProfile().first()
        registerMaintenanceUseCase(
            vehicleId = vehicle.vehiclePlate.ifBlank { "BYD_S6" },
            type = type,
            odometer = odo,
            costCop = cost,
            notes = notes,
            timestamp = timestamp,
            serviceTitle = title,
            location = location,
            paymentSource = source
        )
        if (cost > 0.0) {
            val shift = manageShiftUseCase.getOrCreateShiftForDay(timestamp = timestamp)
            registerExpenseUseCase(
                shiftId = shift.id,
                concept = ExpenseConcept.OTHER,
                amount = cost,
                timestamp = timestamp,
                paymentSource = source
            )
        }
    }
}
