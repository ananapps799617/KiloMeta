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
    private val notifyQuotaMilestoneUseCase: NotifyQuotaMilestoneUseCase
) : ViewModel() {

    private val _syncPreferences = MutableStateFlow(SyncPreferences())
    val syncPreferences: StateFlow<SyncPreferences> = _syncPreferences.asStateFlow()

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
        getAccountingAuditDashboardUseCase()
    ) { activeSummary: ShiftFinancialSummary?, auditSummary: AccountingAuditSummary ->
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
                accountingAuditSummary = auditSummary
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), HomeUiState.Loading)

    // --------------------------------------------------------------------------
    // BLOQUE 3: OPERACIONES DE CABINA Y CAPTURA EXPRESS
    // --------------------------------------------------------------------------
    fun registerTrip(platform: PlatformType, gross: Double, commission: Double, payment: PaymentMethod) {
        val state = uiState.value as? HomeUiState.ActiveShift ?: return
        viewModelScope.launch {
            registerTripUseCase(state.summary.shiftId, platform, gross, commission, payment)
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
            manageShiftUseCase.closeShift(state.summary.shiftId, state.summary.startOdometer, endOdometer, notes)
            // Evaluación de hitos de cuota con política DataGuard (cero dinero líquido en pantalla)
            notifyQuotaMilestoneUseCase.checkAndNotifyShiftMilestone(state.summary)
            notifyQuotaMilestoneUseCase.checkAndNotifyWeeklyMilestone(state.weeklyProgress)
        }
    }

    // --------------------------------------------------------------------------
    // BLOQUE 4: ACCIONES DE SINCRONIZACIÓN Y DIAGNÓSTICO EN VIVO
    // --------------------------------------------------------------------------
    fun syncNow() {
        viewModelScope.launch { syncDataUseCase() }
    }

    fun onTestLiveConnection() {
        viewModelScope.launch {
            _isPingingCloud.value = true
            _connectionDiagnostic.value = checkCloudConnectionUseCase()
            _isPingingCloud.value = false
        }
    }

    fun startDisasterRecovery() {
        viewModelScope.launch {
            restoreDataUseCase().collect { _restoreProgress.value = it }
        }
    }

    fun resetRecoveryProgress() {
        _restoreProgress.value = RestoreProgress.Idle
    }

    fun signInWithGoogle() {
        viewModelScope.launch { authenticateUserUseCase.signInWithGoogle("mock-google-id-token") }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch { authenticateUserUseCase.signInWithEmail(email, pass) }
    }

    fun signInAnonymously() {
        viewModelScope.launch { authenticateUserUseCase.signInAnonymously() }
    }

    fun signOut() {
        viewModelScope.launch { authenticateUserUseCase.signOut() }
    }

    fun onToggleAutoSync(enabled: Boolean) {
        val updated = _syncPreferences.value.copy(isAutoSyncEnabled = enabled)
        _syncPreferences.value = updated
        schedulePeriodicSyncUseCase(updated)
    }

    fun onSelectNetworkPreference(requireWifiOnly: Boolean) {
        val updated = _syncPreferences.value.copy(requireWifiOnly = requireWifiOnly)
        _syncPreferences.value = updated
        schedulePeriodicSyncUseCase(updated)
    }
}
