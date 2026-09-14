// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/GetWalletTransfersUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para consultar el flujo de transferencias entre bolsillos (Orden #033).
// Regla inquebrantable: Modularidad estricta < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.WalletTransfer
import com.ananapps.kilometa.domain.repository.WalletTransferRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWalletTransfersUseCase @Inject constructor(
    private val walletTransferRepository: WalletTransferRepository
) {
    operator fun invoke(): Flow<List<WalletTransfer>> {
        return walletTransferRepository.getAllTransfers()
    }

    fun forRange(from: Long, to: Long): Flow<List<WalletTransfer>> {
        return walletTransferRepository.getTransfersByDateRange(from, to)
    }
}
