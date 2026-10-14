package com.ananapps.kilometa.domain.usecase.wallet

import com.ananapps.kilometa.domain.model.Wallet
import com.ananapps.kilometa.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso reactivo para observar todos los bolsillos para administración y ajustes (Orden #054).
 */
class GetAllWalletsUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    operator fun invoke(): Flow<List<Wallet>> = walletRepository.observeAllWallets()
}
