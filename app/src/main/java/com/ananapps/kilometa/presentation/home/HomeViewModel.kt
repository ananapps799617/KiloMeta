// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/HomeViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel principal de cabina (@HiltViewModel).
// Orquesta reactivamente los flujos del repositorio local y casos de uso de Dominio
// para producir un StateFlow<HomeUiState> inmutable con retención de 5000ms.
// Expone acciones deterministas de captura en cabina (carreras, tanqueos, gastos, turnos).
// ==============================================================================

package com.ananapps.kilometa.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.ExpenseConcept
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.Shift
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.WeeklyQuotaProgress
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.usecase.CalculateBiFuelCostPerKmUseCase
import com.ananapps.kilometa.domain.usecase.CalculateDailyQuotaUseCase
import com.ananapps.kilometa.domain.usecase.GetShiftFinancialStatusUseCase
import com.ananapps.kilometa.domain.usecase.ManageShiftUseCase
import com.ananapps.kilometa.domain.usecase.RegisterExpenseUseCase
import com.ananapps.kilometa.domain.usecase.RegisterFuelingUseCase
import com.ananapps.kilometa.domain.usecase.RegisterTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val manageShiftUseCase: ManageShiftUseCase
) : ViewModel() {

    // --------------------------------------------------------------------------
    // BLOQUE 2: COMBINACIÓN REACTIVA DEL ESTADO DE UI (STATEFLOW 5000MS)
    // --------------------------------------------------------------------------
    val uiState: StateFlow<HomeUiState> = combine(
        shiftRepository.getActiveShiftFinancialSummary(),
        shiftRepository.getAllShifts()
    ) { activeSummary: ShiftFinancialSummary?, allShifts: List<Shift> ->
        val cycleStartMillis = getWeeklyCycleStartMillis()
        val completedShiftsInCycle = allShifts.filter {
            it.startTime >= cycleStartMillis && it.endTime != null
        }
        // Estimación del acumulado de turnos finalizados en el ciclo semanal
        val pastShiftsAccumulated = completedShiftsInCycle.sumOf { shift ->
            // Si la cuota ya fue alcanzada o estimada, se proyecta su contribución
            shift.dailyQuotaTarget
        }

        if (activeSummary == null) {
            val weeklyProgress = calculateDailyQuotaUseCase(
                weeklyTarget = Constants.DEFAULT_WEEKLY_QUOTA_DOUBLE,
                accumulatedSoFar = pastShiftsAccumulated
            )
            HomeUiState.NoActiveShift(
                suggestedDailyQuota = weeklyProgress.suggestedDailyQuota,
                weeklyProgress = weeklyProgress,
                isOffline = true
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
                isOffline = true
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeUiState.Loading
    )

    // --------------------------------------------------------------------------
    // BLOQUE 3: ACCIONES OPERATIVAS EN CABINA (MUTACIONES ASÍNCRONAS)
    // --------------------------------------------------------------------------
    /**
     * Registra una carrera realizada en el turno activo.
     */
    fun registerTrip(
        platformType: PlatformType,
        grossAmount: Double,
        commissionAmount: Double,
        paymentMethod: PaymentMethod
    ) {
        val currentState = uiState.value
        if (currentState !is HomeUiState.ActiveShift) return

        viewModelScope.launch {
            registerTripUseCase(
                shiftId = currentState.summary.shiftId,
                platformType = platformType,
                grossAmount = grossAmount,
                commissionAmount = commissionAmount,
                paymentMethod = paymentMethod
            )
        }
    }

    /**
     * Registra un abastecimiento bi-modal de combustible.
     */
    fun registerFueling(
        fuelType: FuelType,
        amountPaid: Double,
        volume: Double,
        odometer: Int
    ) {
        val currentState = uiState.value
        if (currentState !is HomeUiState.ActiveShift) return

        viewModelScope.launch {
            registerFuelingUseCase(
                shiftId = currentState.summary.shiftId,
                fuelType = fuelType,
                amountPaid = amountPaid,
                volume = volume,
                odometer = odometer
            )
        }
    }

    /**
     * Registra un gasto incidental en ruta.
     */
    fun registerExpense(
        concept: ExpenseConcept,
        amount: Double
    ) {
        val currentState = uiState.value
        if (currentState !is HomeUiState.ActiveShift) return

        viewModelScope.launch {
            registerExpenseUseCase(
                shiftId = currentState.summary.shiftId,
                concept = concept,
                amount = amount
            )
        }
    }

    /**
     * Inicia una nueva jornada laboral en cabina.
     */
    fun startShift(startOdometer: Int, dailyQuota: Double? = null) {
        viewModelScope.launch {
            manageShiftUseCase.startShift(
                startOdometer = startOdometer,
                dailyQuotaTarget = dailyQuota ?: 83_333.0
            )
        }
    }

    /**
     * Cierra el turno activo validando el odómetro final.
     */
    fun closeShift(endOdometer: Int, notes: String? = null) {
        val currentState = uiState.value
        if (currentState !is HomeUiState.ActiveShift) return

        viewModelScope.launch {
            manageShiftUseCase.closeShift(
                shiftId = currentState.summary.shiftId,
                startOdometer = currentState.summary.startOdometer,
                endOdometer = endOdometer,
                notes = notes
            )
        }
    }

    // --------------------------------------------------------------------------
    // BLOQUE 4: CÁLCULO DEL INICIO DEL CICLO SEMANAL (MIÉRCOLES 00:00:00)
    // --------------------------------------------------------------------------
    private fun getWeeklyCycleStartMillis(): Long {
        val now = Instant.now().atZone(ZoneId.systemDefault())
        var current = now
        while (current.dayOfWeek != DayOfWeek.WEDNESDAY) {
            current = current.minusDays(1)
        }
        return current.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
