package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.data.local.dao.PendingDeletionDao
import com.ananapps.kilometa.data.local.dao.WalletDao
import com.ananapps.kilometa.data.local.entity.PendingDeletionEntity
import com.ananapps.kilometa.data.mapper.toDomain
import com.ananapps.kilometa.data.mapper.toEntity
import com.ananapps.kilometa.data.mapper.toWalletDomain
import com.ananapps.kilometa.domain.model.Wallet
import com.ananapps.kilometa.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Implementación de WalletRepository con blindaje de ciberseguridad MND-04 / MND-DG-02 (Orden #054 / #055 / #056).
 * Protege de forma inmutable los bolsillos del sistema, preserva saldos reales vivos y aniquila el Zombie Bug.
 */
@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao,
    private val pendingDeletionDao: PendingDeletionDao
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
        val existing = walletDao.getWalletById(wallet.id)
        val currentBalance = existing?.balance ?: wallet.balance
        val isProtected = wallet.id in listOf("cash_physical", "digital_wallet") ||
            wallet.isSystemProtected ||
            (existing?.isSystemProtected == true)

        val safeWallet = wallet.copy(
            isSystemProtected = isProtected,
            balance = currentBalance
        )

        pendingDeletionDao.deleteByEntityId(wallet.id)
        walletDao.insertWallet(safeWallet.toEntity())
    }

    override suspend fun toggleWalletStatus(id: String, isActive: Boolean): Result<Unit> = runCatching {
        val existing = walletDao.getWalletById(id)
            ?: throw NoSuchElementException("No existe el bolsillo con identificador '$id'")

        if (existing.isSystemProtected) {
            throw SecurityException("Violación de integridad (MND-04): El bolsillo '${existing.name}' es protegido e inmutable.")
        }

        if (!isActive && abs(existing.balance) >= 0.01) {
            throw IllegalStateException("No es posible desactivar el bolsillo '${existing.name}' porque posee un saldo de $${existing.balance}. Transfiera primero los fondos.")
        }

        walletDao.updateWalletActiveStatus(id, isActive)
    }

    override suspend fun updateWalletBalance(id: String, delta: Double): Result<Unit> = runCatching {
        walletDao.updateWalletBalance(id, delta)
    }

    override suspend fun deleteCustomWallet(id: String): Result<Unit> = runCatching {
        val existing = walletDao.getWalletById(id)
            ?: throw NoSuchElementException("No existe el bolsillo con ID '$id'")

        if (existing.isSystemProtected) {
            throw SecurityException("Violación de integridad: Los bolsillos de sistema son inmutables.")
        }

        if (abs(existing.balance) >= 0.01) {
            throw IllegalStateException("El bolsillo '${existing.name}' tiene saldo activo.")
        }

        walletDao.deleteCustomWallet(id)
        // Registro de lápida atómica Protocolo Anti-Zombie (Orden #027)
        pendingDeletionDao.insert(
            PendingDeletionEntity(
                entityId = id,
                entityType = "WALLET",
                deletedAt = System.currentTimeMillis()
            )
        )
    }
}
