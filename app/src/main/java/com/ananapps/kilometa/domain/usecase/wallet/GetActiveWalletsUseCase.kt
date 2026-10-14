package com.ananapps.kilometa.domain.usecase.wallet

import com.ananapps.kilometa.domain.model.Wallet
import com.ananapps.kilometa.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso reactivo para observar bolsillos activos en la cabina vehicular (Orden #054).
 */
class GetActiveWalletsUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    operator fun invoke(): Flow<List<Wallet>> = walletRepository.observeActiveWallets()
}
