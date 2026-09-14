// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/WalletTransferRepository.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contrato de repositorio de dominio para transferencias inter-bolsillos (Orden #033).
// Permite observar transferencias reactivamente y mutar movimientos financieros.
// Regla inquebrantable: Modularidad estricta < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.WalletTransfer
import kotlinx.coroutines.flow.Flow

interface WalletTransferRepository {
    fun getAllTransfers(): Flow<List<WalletTransfer>>
    fun getTransfersByDateRange(from: Long, to: Long): Flow<List<WalletTransfer>>
    suspend fun insertTransfer(transfer: WalletTransfer)
    suspend fun deleteTransfer(id: String)
}
