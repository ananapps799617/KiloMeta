// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/WalletTransferRepositoryImpl.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación del repositorio de transferencias inter-billeteras (Orden #033).
// Integra persistencia local Room SQLite, protocolo anti-resurrección con lápidas
// y sincronización asíncrona con Cloud Firestore.
// Regla inquebrantable: Modularidad estricta < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.core.dispatcher.CoroutineDispatchers
import com.ananapps.kilometa.data.local.dao.PendingDeletionDao
import com.ananapps.kilometa.data.local.dao.WalletTransferDao
import com.ananapps.kilometa.data.local.entity.PendingDeletionEntity
import com.ananapps.kilometa.data.mapper.toDomain
import com.ananapps.kilometa.data.mapper.toEntity
import com.ananapps.kilometa.data.remote.firebase.FirestoreDataSource
import com.ananapps.kilometa.data.remote.mapper.toDto
import com.ananapps.kilometa.domain.model.WalletTransfer
import com.ananapps.kilometa.domain.repository.AuthRepository
import com.ananapps.kilometa.domain.repository.WalletTransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletTransferRepositoryImpl @Inject constructor(
    private val walletTransferDao: WalletTransferDao,
    private val pendingDeletionDao: PendingDeletionDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val authRepository: AuthRepository,
    private val dispatchers: CoroutineDispatchers
) : WalletTransferRepository {

    override fun getAllTransfers(): Flow<List<WalletTransfer>> {
        return walletTransferDao.getAllTransfers()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override fun getTransfersByDateRange(from: Long, to: Long): Flow<List<WalletTransfer>> {
        return walletTransferDao.getTransfersByDateRange(from, to)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun insertTransfer(transfer: WalletTransfer) {
        withContext(dispatchers.io) {
            walletTransferDao.insertTransfer(transfer.toEntity())
            pendingDeletionDao.deleteByEntityId(transfer.id)
            val user = authRepository.getCurrentUser()
            if (user != null) {
                firestoreDataSource.saveWalletTransfers(
                    user.uid,
                    listOf(transfer.toEntity().toDto())
                )
            }
        }
    }

    override suspend fun deleteTransfer(id: String) {
        withContext(dispatchers.io) {
            walletTransferDao.deleteTransferById(id)
            pendingDeletionDao.insert(PendingDeletionEntity(id, "WALLET_TRANSFER"))
            val user = authRepository.getCurrentUser()
            if (user != null) {
                firestoreDataSource.deleteWalletTransfer(user.uid, id)
            }
        }
    }
}
