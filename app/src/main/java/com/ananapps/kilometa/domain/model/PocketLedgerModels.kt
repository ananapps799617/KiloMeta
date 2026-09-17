// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/PocketLedgerModels.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelos de Dominio para el Libro Mayor y Extracto Contable de Bolsillos Soberanos (Orden #044).
// Permite auditar entradas, salidas y transacciones cronológicas por billetera.
// ==============================================================================

package com.ananapps.kilometa.domain.model

/**
 * Representa una transacción o movimiento atómico que impacta un bolsillo específico.
 */
data class PocketTransactionItem(
    val id: String,
    val timestamp: Long,
    val title: String,
    val subtitle: String,
    val amount: Double, // Positivo para entrada (+), negativo para deducción/salida (-)
    val icon: String
) {
    val isPositive: Boolean get() = amount >= 0.0
}

/**
 * Resumen consolidado del extracto contable de un bolsillo para un ciclo acotado.
 */
data class PocketLedgerSummary(
    val pocketType: PocketType,
    val cycleStartMillis: Long,
    val cycleEndMillis: Long,
    val cycleTitle: String,
    val cycleSubtitle: String,
    val totalInflow: Double,
    val totalOutflow: Double,
    val netPeriodBalance: Double,
    val transactions: List<PocketTransactionItem>,
    val canGoNext: Boolean
)
