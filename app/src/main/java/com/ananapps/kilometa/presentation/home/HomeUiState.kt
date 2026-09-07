// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/HomeUiState.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Jerarquía sellada del estado de la Interfaz de Usuario para la pantalla Home de cabina.
// Representa reactivamente los estados del ciclo operativo: carga inicial (Loading),
// conductor disponible sin turno abierto (NoActiveShift) y jornada activa en ruta (ActiveShift)
// con telemetría de diagnóstico en vivo para la nube.
// ==============================================================================

package com.ananapps.kilometa.presentation.home

import com.ananapps.kilometa.domain.model.BiFuelCostMetrics
import com.ananapps.kilometa.domain.model.ConnectionDiagnostic
import com.ananapps.kilometa.domain.model.FinancialStatus
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.SyncPreferences
import com.ananapps.kilometa.domain.model.WeeklyQuotaProgress

// ------------------------------------------------------------------------------
// BLOQUE 1: CONTRATO SELLADO DE ESTADO DE UI
// ------------------------------------------------------------------------------
/**
 * Estados inmutables y estrictamente tipados para la pantalla principal de KiloMeta.
 */
sealed interface HomeUiState {

    // --------------------------------------------------------------------------
    // BLOQUE 2: ESTADO DE CARGA REACTIVA
    // --------------------------------------------------------------------------
    /**
     * Estado transitorio mientras se resuelven los flujos de Room y RoomDatabase.
     */
    data object Loading : HomeUiState

    // --------------------------------------------------------------------------
    // BLOQUE 3: ESTADO SIN TURNO ACTIVO (EN BASE / STANDBY)
    // --------------------------------------------------------------------------
    /**
     * Representa al conductor listo para iniciar jornada, mostrando el prorrateo semanal.
     */
    data class NoActiveShift(
        val suggestedDailyQuota: Double,
        val weeklyProgress: WeeklyQuotaProgress,
        val isOffline: Boolean = true,
        val connectionDiagnostic: ConnectionDiagnostic? = null,
        val isPingingCloud: Boolean = false,
        val syncPreferences: SyncPreferences = SyncPreferences()
    ) : HomeUiState

    // --------------------------------------------------------------------------
    // BLOQUE 4: ESTADO DE TURNO ACTIVO (EN CABINA / OPERANDO)
    // --------------------------------------------------------------------------
    /**
     * Cabina operativa completa con métricas financieras y telemetría de enlace.
     */
    data class ActiveShift(
        val summary: ShiftFinancialSummary,
        val status: FinancialStatus,
        val dailyQuotaTarget: Double,
        val amountRemainingForFreeShift: Double,
        val weeklyProgress: WeeklyQuotaProgress,
        val biFuelMetrics: BiFuelCostMetrics,
        val isOffline: Boolean = true,
        val connectionDiagnostic: ConnectionDiagnostic? = null,
        val isPingingCloud: Boolean = false,
        val syncPreferences: SyncPreferences = SyncPreferences()
    ) : HomeUiState

    // --------------------------------------------------------------------------
    // BLOQUE 5: COMPATIBILIDAD CON SMOKE TESTS ARQUITECTURALES
    // --------------------------------------------------------------------------
    /**
     * Conservado con fines de compatibilidad hacia atrás y verificación de cero mentiras técnicas.
     */
    data class StandbyPlaceholder(
        val status: FinancialStatus = FinancialStatus.Amber,
        val currentEarningsCop: Long = 0L,
        val dailyQuotaCop: Long = 58_333L,
        val fuelSpentCop: Long = 0L,
        val isOffline: Boolean = true,
        val notice: String = "⚠️ PENDIENTE DE CONEXIÓN REAL"
    ) : HomeUiState
}
