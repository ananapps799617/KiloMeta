package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.data.local.dao.WalletDao
import com.ananapps.kilometa.data.mapper.toDomain
import com.ananapps.kilometa.data.mapper.toEntity
import com.ananapps.kilometa.data.mapper.toWalletDomain
import com.ananapps.kilometa.domain.model.Wallet
import com.ananapps.kilometa.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de WalletRepository con blindaje de ciberseguridad MND-04 / MND-DG-02 (Orden #054 / #055).
 * Protege de forma inmutable los bolsillos del sistema y previene saldos huérfanos.
 */
@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao
) : WalletRepository {

    override fun observeActiveWallets(): Flow<List<Wallet>> {
        return walletDao.observeActiveWallets().map { it.toWalletDomain() }
    }

    override fun observeAllWallets(): Flow<List<Wallet>> {
        return walletDao.observeAllWallets().map { it.toWalletDomain() }
    }

    override suspend fun getWalletById(id: String): Wallet? {
        return walletDao.getWalletById(id)?.toDomain()
    }

    override suspend fun saveWallet(wallet: Wallet): Result<Unit> = runCatching {
        val isProtected = wallet.id in listOf("cash_physical", "digital_wallet") || wallet.isSystemProtected
        val safeWallet = wallet.copy(isSystemProtected = isProtected)
        walletDao.insertWallet(safeWallet.toEntity())
    }

    override suspend fun toggleWalletStatus(id: String, isActive: Boolean): Result<Unit> = runCatching {
        val existing = walletDao.getWalletById(id)
            ?: throw NoSuchElementException("No existe el bolsillo con identificador '$id'")

        if (existing.isSystemProtected) {
            throw SecurityException("Violación de integridad (MND-04): El bolsillo '${existing.name}' es protegido e inmutable.")
        }

        if (!isActive && existing.balance != 0.0) {
            throw IllegalStateException("No es posible desactivar el bolsillo '${existing.name}' porque posee un saldo de $${existing.balance}. Transfiera primero los fondos.")
        }

        walletDao.updateWalletActiveStatus(id, isActive)
    }

    override suspend fun updateWalletBalance(id: String, delta: Double): Result<Unit> = runCatching {
        walletDao.updateWalletBalance(id, delta)
    }

    override suspend fun deleteCustomWallet(id: String): Result<Unit> = runCatching {
        val existing = walletDao.getWalletById(id)
            ?: throw NoSuchElementException("No existe el bolsillo con identificador '$id'")

        if (existing.isSystemProtected) {
            throw SecurityException("Violación de integridad (MND-04): El bolsillo de sistema no puede eliminarse.")
        }

        if (existing.balance != 0.0) {
            throw IllegalStateException("El bolsillo '${existing.name}' tiene saldo pendiente. Transfiera primero sus fondos.")
        }

        walletDao.deleteCustomWallet(id)
    }
}
