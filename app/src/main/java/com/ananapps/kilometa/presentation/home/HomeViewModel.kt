// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/HomeViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel principal de Cabina que orquesta el estado reactivo del turno activo,
// la bolsa contable, el feed en vivo de carreras y las operaciones de sincronización.
// ==============================================================================

package com.ananapps.kilometa.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.*
import com.ananapps.kilometa.domain.repository.AuthRepository
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.repository.SyncRepository
import com.ananapps.kilometa.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val getShiftFinancialStatusUseCase: GetShiftFinancialStatusUseCase,
    private val calculateBiFuelCostPerKmUseCase: CalculateBiFuelCostPerKmUseCase,
    private val registerTripUseCase: RegisterTripUseCase,
    private val deleteTripUseCase: DeleteTripUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val deleteFuelingUseCase: DeleteFuelingUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val updateFuelingUseCase: UpdateFuelingUseCase,
    private val registerFuelingUseCase: RegisterFuelingUseCase,
    private val registerExpenseUseCase: RegisterExpenseUseCase,
    private val manageShiftUseCase: ManageShiftUseCase,
    private val syncRepository: SyncRepository,
    private val authRepository: AuthRepository,
    private val syncDataUseCase: SyncDataUseCase,
    private val restoreDataUseCase: RestoreDataUseCase,
    private val authenticateUserUseCase: AuthenticateUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val checkCloudConnectionUseCase: CheckCloudConnectionUseCase,
    private val schedulePeriodicSyncUseCase: SchedulePeriodicSyncUseCase,
    private val getAccountingAuditDashboardUseCase: GetAccountingAuditDashboardUseCase,
    private val scheduleWeeklyCutoffNotificationsUseCase: ScheduleWeeklyCutoffNotificationsUseCase,
    private val notifyQuotaMilestoneUseCase: NotifyQuotaMilestoneUseCase,
    private val driverProfileRepository: DriverProfileRepository,
    private val getLastKnownOdometerUseCase: GetLastKnownOdometerUseCase,
    private val getDayContinuousTimelineUseCase: GetDayContinuousTimelineUseCase
) : ViewModel() {

    private val _syncPreferences = MutableStateFlow(SyncPreferences())
    val syncPreferences: StateFlow<SyncPreferences> = _syncPreferences.asStateFlow()

    val driverProfile: StateFlow<DriverProfile> = driverProfileRepository.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), DriverProfile())

    val vehicleProfile: StateFlow<VehicleProfile> = driverProfileRepository.getVehicleProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), VehicleProfile())

    val lastKnownOdometer: StateFlow<Int> = getLastKnownOdometerUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 0)

    val dayContinuousTimeline: StateFlow<DayContinuousSummary> = getDayContinuousTimelineUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), DayContinuousSummary())

    init {
        schedulePeriodicSyncUseCase(_syncPreferences.value)
        scheduleWeeklyCutoffNotificationsUseCase()
    }

    val syncStatus: StateFlow<SyncStatus> = syncRepository.observeSyncStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), SyncStatus(SyncState.IDLE))

    val userSession: StateFlow<UserSession?> = authRepository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    private val _restoreProgress = MutableStateFlow<RestoreProgress>(RestoreProgress.Idle)
    val restoreProgress: StateFlow<RestoreProgress> = _restoreProgress.asStateFlow()

    private val _isPingingCloud = MutableStateFlow(false)
    val isPingingCloud: StateFlow<Boolean> = _isPingingCloud.asStateFlow()

    private val _connectionDiagnostic = MutableStateFlow<ConnectionDiagnostic?>(null)
    val connectionDiagnostic: StateFlow<ConnectionDiagnostic?> = _connectionDiagnostic.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        shiftRepository.getActiveShiftFinancialSummary(),
        getAccountingAuditDashboardUseCase(),
        shiftRepository.getActiveShiftCashSettlement(),
        shiftRepository.getActiveShiftTrips()
    ) { activeSummary, auditSummary, cashSettlement, activeTrips ->
        val weeklyProgress = auditSummary.quotaProgress

        if (activeSummary == null) {
            HomeUiState.NoActiveShift(
                suggestedDailyQuota = weeklyProgress.suggestedDailyQuota,
                weeklyProgress = weeklyProgress,
                isOffline = userSession.value == null,
                connectionDiagnostic = _connectionDiagnostic.value,
                isPingingCloud = _isPingingCloud.value,
                syncPreferences = _syncPreferences.value,
                accountingAuditSummary = auditSummary
            )
        } else {
            val financialStatus = getShiftFinancialStatusUseCase(
                summary = activeSummary,
                targetQuota = weeklyProgress.suggestedDailyQuota
            )
            val biFuelMetrics = calculateBiFuelCostPerKmUseCase(activeSummary)

            HomeUiState.ActiveShift(
                summary = activeSummary,
                status = financialStatus,
                dailyQuotaTarget = weeklyProgress.suggestedDailyQuota,
                amountRemainingForFreeShift = financialStatus.remainingAmount,
                weeklyProgress = weeklyProgress,
                biFuelMetrics = biFuelMetrics,
                isOffline = userSession.value == null,
                connectionDiagnostic = _connectionDiagnostic.value,
                isPingingCloud = _isPingingCloud.value,
                syncPreferences = _syncPreferences.value,
                accountingAuditSummary = auditSummary,
                cashSettlement = cashSettlement,
                activeTrips = activeTrips
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), HomeUiState.Loading)

    fun registerTrip(
        platform: PlatformType, gross: Double, commission: Double, payment: PaymentMethod,
        dest: PaymentDestination = PaymentDestination.CASH_IN_HAND,
        timestamp: Long = System.currentTimeMillis(),
        startOdometer: Int? = null,
        endOdometer: Int? = null,
        isSporadic: Boolean = false
    ) {
        viewModelScope.launch {
            val shiftId = if (isSporadic || uiState.value !is HomeUiState.ActiveShift) {
                manageShiftUseCase.getOrCreateShiftForDay(
                    timestamp = timestamp,
                    odometer = startOdometer ?: endOdometer,
                    notes = "Jornada diaria"
                ).id
            } else {
                (uiState.value as HomeUiState.ActiveShift).summary.shiftId
            }
            registerTripUseCase(
                shiftId = shiftId,
                platformType = platform,
                grossAmount = gross,
                commissionAmount = commission,
                paymentMethod = payment,
                paymentDestination = dest,
                timestamp = timestamp,
                startOdometer = startOdometer,
                endOdometer = endOdometer
            )
        }
    }

    fun deleteTrip(tripId: String) = viewModelScope.launch { deleteTripUseCase(tripId) }
    fun deleteExpense(expenseId: String) = viewModelScope.launch { deleteExpenseUseCase(expenseId) }
    fun deleteFueling(fuelingId: String) = viewModelScope.launch { deleteFuelingUseCase(fuelingId) }
    fun updateTrip(trip: Trip) = viewModelScope.launch { updateTripUseCase(trip) }
    fun updateExpense(expense: Expense) = viewModelScope.launch { updateExpenseUseCase(expense) }
    fun updateFueling(fueling: Fueling) = viewModelScope.launch { updateFuelingUseCase(fueling) }

    fun registerFueling(type: FuelType, amount: Double, volume: Double, odo: Int, isSporadic: Boolean = false) = viewModelScope.launch {
        val shiftId = if (isSporadic || uiState.value !is HomeUiState.ActiveShift) {
            manageShiftUseCase.getOrCreateShiftForDay(System.currentTimeMillis(), odo, "Jornada diaria").id
        } else {
            (uiState.value as HomeUiState.ActiveShift).summary.shiftId
        }
        registerFuelingUseCase(shiftId, type, amount, volume, odo)
    }

    fun registerExpense(concept: ExpenseConcept, amount: Double, isSporadic: Boolean = false) = viewModelScope.launch {
        val shiftId = if (isSporadic || uiState.value !is HomeUiState.ActiveShift) {
            manageShiftUseCase.getOrCreateShiftForDay(System.currentTimeMillis(), null, "Jornada diaria").id
        } else {
            (uiState.value as HomeUiState.ActiveShift).summary.shiftId
        }
        registerExpenseUseCase(shiftId, concept, amount)
    }

    fun startShift(startOdometer: Int, dailyQuota: Double? = null) {
        viewModelScope.launch { manageShiftUseCase.startShift(startOdometer, dailyQuota ?: 83_333.0) }
    }

    fun closeShift(endOdometer: Int, notes: String? = null) {
        val state = uiState.value as? HomeUiState.ActiveShift ?: return
        viewModelScope.launch {
            manageShiftUseCase.closeShift(state.summary.shiftId, state.summary.startOdometer, endOdometer, notes = notes)
            notifyQuotaMilestoneUseCase.checkAndNotifyShiftMilestone(state.summary)
            notifyQuotaMilestoneUseCase.checkAndNotifyWeeklyMilestone(state.weeklyProgress)
        }
    }

    fun syncNow() { viewModelScope.launch { syncDataUseCase() } }

    fun onTestLiveConnection() {
        viewModelScope.launch {
            _isPingingCloud.value = true
            _connectionDiagnostic.value = checkCloudConnectionUseCase()
            _isPingingCloud.value = false
        }
    }

    fun startDisasterRecovery() {
        viewModelScope.launch { restoreDataUseCase().collect { _restoreProgress.value = it } }
    }

    fun resetRecoveryProgress() { _restoreProgress.value = RestoreProgress.Idle }

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    fun setAuthError(message: String) { _authError.value = message }
    fun clearAuthError() { _authError.value = null }

    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        _isAuthLoading.value = true; _authError.value = null
        val result = authenticateUserUseCase.signInWithGoogle(idToken)
        if (result.isSuccess) {
            result.getOrNull()?.let { driverProfileRepository.fetchAndHydrateRemoteProfile(it.uid) }
        } else {
            _authError.value = result.exceptionOrNull()?.localizedMessage ?: "Error Google"
        }
        _isAuthLoading.value = false
    }

    fun signInWithEmail(email: String, pass: String) = viewModelScope.launch {
        _isAuthLoading.value = true; _authError.value = null
        val signIn = authenticateUserUseCase.signInWithEmail(email.trim(), pass)
        if (signIn.isSuccess) {
            signIn.getOrNull()?.let { driverProfileRepository.fetchAndHydrateRemoteProfile(it.uid) }
        } else {
            val signUp = authenticateUserUseCase.signUpWithEmail(email.trim(), pass)
            if (signUp.isFailure) _authError.value = signUp.exceptionOrNull()?.localizedMessage ?: "Error auth"
        }
        _isAuthLoading.value = false
    }

    fun signInAnonymously() = viewModelScope.launch {
        _isAuthLoading.value = true; _authError.value = null
        val result = authenticateUserUseCase.signInAnonymously()
        if (result.isFailure) _authError.value = result.exceptionOrNull()?.localizedMessage ?: "Error sesión"
        _isAuthLoading.value = false
    }

    fun signOut() = viewModelScope.launch { signOutUseCase() }

    fun onToggleAutoSync(enabled: Boolean) {
        _syncPreferences.value = _syncPreferences.value.copy(isAutoSyncEnabled = enabled)
        schedulePeriodicSyncUseCase(_syncPreferences.value)
    }

    fun onSelectNetworkPreference(requireWifiOnly: Boolean) {
        _syncPreferences.value = _syncPreferences.value.copy(requireWifiOnly = requireWifiOnly)
        schedulePeriodicSyncUseCase(_syncPreferences.value)
    }
}
