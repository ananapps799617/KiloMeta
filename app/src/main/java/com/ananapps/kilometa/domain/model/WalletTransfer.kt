// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/WalletTransfer.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de dominio para representar una transferencia de fondos entre bolsillos (Orden #033).
// Permite dispersiones de plataformas a Nequi, consignaciones, retiros y pagos de deuda.
// Regla inquebrantable: Modularidad estricta < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.domain.model

data class WalletTransfer(
    val id: String,
    val timestamp: Long,
    val sourceWallet: WalletType,
    val destinationWallet: WalletType,
    val amount: Double,
    val transferType: TransferType,
    val notes: String? = null
)
