// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/HomeViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel principal de cabina (@HiltViewModel).
// Orquesta reactivamente flujos locales, operaciones de captura express y el Cloud Sync Engine
// para telemetría en tiempo real y recuperación ante desastres (Disaster Recovery).
// ==============================================================================

package com.ananapps.kilometa.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.RestoreProgress
import com.ananapps.kilometa.domain.model.Shift
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.SyncState
import com.ananapps.kilometa.domain.model.SyncStatus
import com.ananapps.kilometa.domain.model.UserSession
import com.ananapps.kilometa.domain.repository.AuthRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.repository.SyncRepository
import com.ananapps.kilometa.domain.usecase.AuthenticateUserUseCase
import com.ananapps.kilometa.domain.usecase.CalculateBiFuelCostPerKmUseCase
import com.ananapps.kilometa.domain.usecase.CalculateDailyQuotaUseCase
import com.ananapps.kilometa.domain.usecase.GetShiftFinancialStatusUseCase
import com.ananapps.kilometa.domain.usecase.ManageShiftUseCase
import com.ananapps.kilometa.domain.usecase.RegisterExpenseUseCase
import com.ananapps.kilometa.domain.usecase.RegisterFuelingUseCase
import com.ananapps.kilometa.domain.usecase.RegisterTripUseCase
import com.ananapps.kilometa.domain.usecase.RestoreDataUseCase
import com.ananapps.kilometa.domain.usecase.SyncDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: VIEWMODEL DE CABINA CON INYECCIÓN HILT
// ------------------------------------------------------------------------------
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val calculateDailyQuotaUseCase: CalculateDailyQuotaUseCase,
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
    private val authenticateUserUseCase: AuthenticateUserUseCase
) : ViewModel() {

    val syncStatus: StateFlow<SyncStatus> = syncRepository.observeSyncStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), SyncStatus(SyncState.IDLE))

    val userSession: StateFlow<UserSession?> = authRepository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    private val _restoreProgress = MutableStateFlow<RestoreProgress>(RestoreProgress.Idle)
    val restoreProgress: StateFlow<RestoreProgress> = _restoreProgress.asStateFlow()

    // --------------------------------------------------------------------------
    // BLOQUE 2: COMBINACIÓN REACTIVA DEL ESTADO DE UI
    // --------------------------------------------------------------------------
    val uiState: StateFlow<HomeUiState> = combine(
        shiftRepository.getActiveShiftFinancialSummary(),
        shiftRepository.getAllShifts()
    ) { activeSummary: ShiftFinancialSummary?, allShifts: List<Shift> ->
        val cycleStartMillis = getWeeklyCycleStartMillis()
        val pastShiftsAccumulated = allShifts
            .filter { it.startTime >= cycleStartMillis && it.endTime != null }
            .sumOf { it.dailyQuotaTarget }

        if (activeSummary == null) {
            val weeklyProgress = calculateDailyQuotaUseCase(
                weeklyTarget = Constants.DEFAULT_WEEKLY_QUOTA_DOUBLE,
                accumulatedSoFar = pastShiftsAccumulated
            )
            HomeUiState.NoActiveShift(
                suggestedDailyQuota = weeklyProgress.suggestedDailyQuota,
                weeklyProgress = weeklyProgress,
                isOffline = userSession.value == null
            )
        } else {
            val totalAccumulated = pastShiftsAccumulated + maxOf(0.0, activeSummary.netProfit)
            val weeklyProgress = calculateDailyQuotaUseCase(
                weeklyTarget = Constants.DEFAULT_WEEKLY_QUOTA_DOUBLE,
                accumulatedSoFar = totalAccumulated
            )
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
                isOffline = userSession.value == null
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), HomeUiState.Loading)

    // --------------------------------------------------------------------------
    // BLOQUE 3: OPERACIONES DE CABINA Y CAPTURA EXPRESS
    // --------------------------------------------------------------------------
    fun registerTrip(platformType: PlatformType, gross: Double, commission: Double, payment: PaymentMethod) {
        val state = uiState.value as? HomeUiState.ActiveShift ?: return
        viewModelScope.launch {
            registerTripUseCase(state.summary.shiftId, platformType, gross, commission, payment)
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
        }
    }

    // --------------------------------------------------------------------------
    // BLOQUE 4: ACCIONES DE SINCRONIZACIÓN Y AUTENTICACIÓN
    // --------------------------------------------------------------------------
    fun syncNow() {
        viewModelScope.launch { syncDataUseCase() }
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

    private fun getWeeklyCycleStartMillis(): Long {
        val now = Instant.now().atZone(ZoneId.systemDefault())
        var current = now
        while (current.dayOfWeek != DayOfWeek.WEDNESDAY) { current = current.minusDays(1) }
        return current.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
