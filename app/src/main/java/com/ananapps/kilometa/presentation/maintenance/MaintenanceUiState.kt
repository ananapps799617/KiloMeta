// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/maintenance/MaintenanceUiState.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Estado inmutable de la pantalla independiente de Mantenimiento Preventivo (estilo KiloMeta).
// ==============================================================================

package com.ananapps.kilometa.presentation.maintenance

import com.ananapps.kilometa.domain.model.MaintenanceAlert
import com.ananapps.kilometa.domain.model.MaintenanceRecord

data class MaintenanceUiState(
    val isLoading: Boolean = true,
    val records: List<MaintenanceRecord> = emptyList(),
    val alerts: List<MaintenanceAlert> = emptyList(),
    val totalInvestmentCop: Double = 0.0,
    val currentOdometer: Int = 0,
    val vehiclePlate: String = ""
)
