// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/HomeUiState.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Representa el estado inmutable de la Interfaz de Usuario (UI State) de la pantalla Home.
// Cumple con la regla 'Cero Mentiras Técnicas' al rotular explícitamente los datos
// provisionales de la fase de inicialización.
// ==============================================================================

package com.ananapps.kilometa.presentation.home

import com.ananapps.kilometa.domain.model.FinancialStatus

// ------------------------------------------------------------------------------
// BLOQUE 1: CONTRATO SELLADO DE ESTADO DE UI
// ------------------------------------------------------------------------------
sealed interface HomeUiState {

    // --------------------------------------------------------------------------
    // BLOQUE 2: ESTADO EN ESPERA (STANDBY PLACEHOLDER)
    // --------------------------------------------------------------------------
    /**
     * Estado inicial de arranque de la aplicación en la Orden #002.
     * Muestra la estructura de cabina sin simular datos persistentes no existentes.
     *
     * ⚠️ PENDIENTE DE CONEXIÓN REAL - Orden #003 (Conexión a Room/DAO).
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
