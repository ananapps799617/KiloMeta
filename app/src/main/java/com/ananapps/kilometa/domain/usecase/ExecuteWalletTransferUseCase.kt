// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/ExecuteWalletTransferUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para registrar y validar transferencias entre bolsillos (Orden #033).
// Valida que el monto sea positivo y que el origen sea distinto al destino.
// Regla inquebrantable: Modularidad estricta < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.TransferType
import com.ananapps.kilometa.domain.model.WalletTransfer
import com.ananapps.kilometa.domain.model.WalletType
import com.ananapps.kilometa.domain.repository.WalletTransferRepository
import java.util.UUID
import javax.inject.Inject

class ExecuteWalletTransferUseCase @Inject constructor(
    private val walletTransferRepository: WalletTransferRepository
) {
    suspend operator fun invoke(
        sourceWallet: WalletType,
        destinationWallet: WalletType,
        amount: Double,
        transferType: TransferType,
        notes: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ): Result<WalletTransfer> {
        if (amount <= 0.0) {
            return Result.failure(IllegalArgumentException("El monto a transferir debe ser mayor a $0"))
        }
        if (sourceWallet == destinationWallet) {
            return Result.failure(IllegalArgumentException("El bolsillo de origen y destino no pueden ser el mismo"))
        }

        val transfer = WalletTransfer(
            id = UUID.randomUUID().toString(),
            timestamp = timestamp,
            sourceWallet = sourceWallet,
            destinationWallet = destinationWallet,
            amount = amount,
            transferType = transferType,
            notes = notes
        )

        walletTransferRepository.insertTransfer(transfer)
        return Result.success(transfer)
    }
}
