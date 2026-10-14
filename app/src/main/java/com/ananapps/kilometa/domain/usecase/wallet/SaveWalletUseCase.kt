package com.ananapps.kilometa.domain.usecase.wallet

import com.ananapps.kilometa.domain.model.Wallet
import com.ananapps.kilometa.domain.repository.WalletRepository
import javax.inject.Inject

/**
 * Caso de uso para crear o modificar un bolsillo con validaciones de negocio (Orden #054).
 */
class SaveWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(wallet: Wallet): Result<Unit> {
        if (wallet.name.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre del bolsillo no puede estar vacío."))
        }
        val normalizedId = wallet.id.ifBlank {
            wallet.name.lowercase().trim().replace("\\s+".toRegex(), "_")
        }
        val normalized = wallet.copy(
            id = normalizedId,
            name = wallet.name.trim()
        )
        return walletRepository.saveWallet(normalized)
    }
}
