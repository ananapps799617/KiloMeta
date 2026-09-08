// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/HomeViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel principal de cabina (@HiltViewModel).
// Orquesta flujos locales, operaciones de captura express, Cloud Sync Engine,
// auditoría contable de ciclo semanal y alertas vehiculares de corte de cuota.
// ==============================================================================

package com.ananapps.kilometa.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.AccountingAuditSummary
import com.ananapps.kilometa.domain.model.ConnectionDiagnostic
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.RestoreProgress
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.SyncPreferences
import com.ananapps.kilometa.domain.model.SyncState
import com.ananapps.kilometa.domain.model.SyncStatus
import com.ananapps.kilometa.domain.model.UserSession
import com.ananapps.kilometa.domain.repository.AuthRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.repository.SyncRepository
import com.ananapps.kilometa.domain.usecase.AuthenticateUserUseCase
import com.ananapps.kilometa.domain.usecase.CalculateBiFuelCostPerKmUseCase
import com.ananapps.kilometa.domain.usecase.CheckCloudConnectionUseCase
import com.ananapps.kilometa.domain.usecase.GetAccountingAuditDashboardUseCase
import com.ananapps.kilometa.domain.usecase.GetShiftFinancialStatusUseCase
import com.ananapps.kilometa.domain.usecase.ManageShiftUseCase
import com.ananapps.kilometa.domain.usecase.NotifyQuotaMilestoneUseCase
import com.ananapps.kilometa.domain.usecase.RegisterExpenseUseCase
import com.ananapps.kilometa.domain.usecase.RegisterFuelingUseCase
import com.ananapps.kilometa.domain.usecase.RegisterTripUseCase
import com.ananapps.kilometa.domain.usecase.RestoreDataUseCase
import com.ananapps.kilometa.domain.usecase.SchedulePeriodicSyncUseCase
import com.ananapps.kilometa.domain.usecase.ScheduleWeeklyCutoffNotificationsUseCase
import com.ananapps.kilometa.domain.usecase.SyncDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: VIEWMODEL DE CABINA CON INYECCIÓN HILT
// ------------------------------------------------------------------------------
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val getShiftFinancialStatusUseCase: GetShiftFinancialStatusUseCase,
    private val calculateBiFuelCostPerKmUseCase: CalculateBiFuelCostPerKmUseCase,
    private val registerTripUseCase: RegisterTripUseCase,
    private val registerFuelingUseCase: RegisterFuelingUseCase,
    private val registerExpenseUseCase: RegisterExpenseUseCase,
    private val manageShiftUseCase: ManageShiftUseCase,
    private val syncRepository: SyncRepository,
    private val authRepository: AuthRepository,
    private val syncDataUseCase: SyncDataUseCase,
    private val restoreDataUseCase: RestoreDataUseCase,
    private val authenticateUserUseCase: AuthenticateUserUseCase,
    private val checkCloudConnectionUseCase: CheckCloudConnectionUseCase,
    private val schedulePeriodicSyncUseCase: SchedulePeriodicSyncUseCase,
    private val getAccountingAuditDashboardUseCase: GetAccountingAuditDashboardUseCase,
    private val scheduleWeeklyCutoffNotificationsUseCase: ScheduleWeeklyCutoffNotificationsUseCase,
    private val notifyQuotaMilestoneUseCase: NotifyQuotaMilestoneUseCase,
    private val driverProfileRepository: com.ananapps.kilometa.domain.repository.DriverProfileRepository
) : ViewModel() {

    private val _syncPreferences = MutableStateFlow(SyncPreferences())
    val syncPreferences: StateFlow<SyncPreferences> = _syncPreferences.asStateFlow()

    val driverProfile: StateFlow<com.ananapps.kilometa.domain.model.DriverProfile> = driverProfileRepository.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), com.ananapps.kilometa.domain.model.DriverProfile())

    init {
        // 1. Sincronización híbrida por defecto (Móviles + Wi-Fi)
        schedulePeriodicSyncUseCase(_syncPreferences.value)
        // 2. Programación de alarmas de corte vehicular (Martes 18:00 y 23:59)
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

    // --------------------------------------------------------------------------
    // BLOQUE 2: COMBINACIÓN REACTIVA DEL ESTADO DE UI
    // --------------------------------------------------------------------------
    val uiState: StateFlow<HomeUiState> = combine(
        shiftRepository.getActiveShiftFinancialSummary(),
        getAccountingAuditDashboardUseCase(),
        shiftRepository.getActiveShiftCashSettlement()
    ) { activeSummary, auditSummary, cashSettlement ->
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
                cashSettlement = cashSettlement
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), HomeUiState.Loading)

    // --------------------------------------------------------------------------
    // BLOQUE 3: OPERACIONES DE CABINA Y CAPTURA EXPRESS
    // --------------------------------------------------------------------------
    fun registerTrip(
        platform: PlatformType,
        gross: Double,
        commission: Double,
        payment: PaymentMethod,
        destination: com.ananapps.kilometa.domain.model.PaymentDestination = com.ananapps.kilometa.domain.model.PaymentDestination.CASH_IN_HAND
    ) {
        val state = uiState.value as? HomeUiState.ActiveShift ?: return
        viewModelScope.launch {
            registerTripUseCase(state.summary.shiftId, platform, gross, commission, payment, destination)
        }
    }

    fun registerFueling(type: FuelType, amount: Double, volume: Double, odo: Int) {
        val state = uiState.value as? HomeUiState.ActiveShift ?: return
        viewModelScope.launch {
            registerFuelingUseCase(state.summary.shiftId, type, amount, volume, odo)
        }
    }

    fun registerExpense(concept: ExpenseConcept, amount: Double) {
        val state = uiState.value as? HomeUiState.ActiveShift ?: return
        viewModelScope.launch {
            registerExpenseUseCase(state.summary.shiftId, concept, amount)
        }
    }

    fun startShift(startOdometer: Int, dailyQuota: Double? = null) {
        viewModelScope.launch {
            manageShiftUseCase.startShift(startOdometer, dailyQuota ?: 83_333.0)
        }
    }

    fun closeShift(endOdometer: Int, notes: String? = null) {
        val state = uiState.value as? HomeUiState.ActiveShift ?: return
        viewModelScope.launch {
            manageShiftUseCase.closeShift(
                shiftId = state.summary.shiftId,
                startOdometer = state.summary.startOdometer,
                endOdometer = endOdometer,
                notes = notes
            )
            // Evaluación de hitos de cuota con política DataGuard (cero dinero líquido en pantalla)
            notifyQuotaMilestoneUseCase.checkAndNotifyShiftMilestone(state.summary)
            notifyQuotaMilestoneUseCase.checkAndNotifyWeeklyMilestone(state.weeklyProgress)
        }
    }

    // --------------------------------------------------------------------------
    // BLOQUE 4: ACCIONES DE SINCRONIZACIÓN Y DIAGNÓSTICO EN VIVO
    // --------------------------------------------------------------------------
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
        if (result.isFailure) _authError.value = result.exceptionOrNull()?.localizedMessage ?: "Error Google"
        _isAuthLoading.value = false
    }

    fun signInWithEmail(email: String, pass: String) = viewModelScope.launch {
        _isAuthLoading.value = true; _authError.value = null
        val signIn = authenticateUserUseCase.signInWithEmail(email.trim(), pass)
        if (signIn.isFailure) {
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

    fun signOut() = viewModelScope.launch { authenticateUserUseCase.signOut() }

    fun onToggleAutoSync(enabled: Boolean) {
        _syncPreferences.value = _syncPreferences.value.copy(isAutoSyncEnabled = enabled)
        schedulePeriodicSyncUseCase(_syncPreferences.value)
    }

    fun onSelectNetworkPreference(requireWifiOnly: Boolean) {
        _syncPreferences.value = _syncPreferences.value.copy(requireWifiOnly = requireWifiOnly)
        schedulePeriodicSyncUseCase(_syncPreferences.value)
    }
}
