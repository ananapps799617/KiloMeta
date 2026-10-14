package com.ananapps.kilometa.domain.usecase.wallet

import com.ananapps.kilometa.domain.repository.WalletRepository
import javax.inject.Inject

/**
 * Caso de uso para conmutar el estado lógico de un bolsillo con defensa MND-04 (Orden #054).
 */
class ToggleWalletStatusUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(id: String, isActive: Boolean): Result<Unit> {
        return walletRepository.toggleWalletStatus(id, isActive)
    }
}
