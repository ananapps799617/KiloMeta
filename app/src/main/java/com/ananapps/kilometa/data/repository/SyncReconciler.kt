// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/SyncReconciler.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Motor de conciliación bidireccional y anti-resurrección (Mirror Reconciliation).
// Compara y sincroniza registros locales con el SSOT de Cloud Firestore, eliminando
// datos huérfanos o zombies entre dispositivos y resolviendo conflictos LWW.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.data.local.KiloMetaDatabase
import com.ananapps.kilometa.data.local.entity.ExpenseEntity
import com.ananapps.kilometa.data.local.entity.FuelingEntity
import com.ananapps.kilometa.data.local.entity.MaintenanceEntity
import com.ananapps.kilometa.data.local.entity.ShiftEntity
import com.ananapps.kilometa.data.local.entity.TripEntity
import com.ananapps.kilometa.data.local.entity.WalletTransferEntity
import com.ananapps.kilometa.data.remote.firebase.FirestoreDataSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncReconciler @Inject constructor(
    private val database: KiloMetaDatabase,
    private val firestoreDataSource: FirestoreDataSource
) {

    suspend fun reconcileShifts(
        userId: String,
        remoteShifts: List<ShiftEntity>,
        deletedIds: Set<String>
    ) {
        val shiftDao = database.shiftDao()
        val remoteMap = remoteShifts.associateBy { it.id }
        val localShifts = shiftDao.getAllShifts().first()

        for (local in localShifts) {
            if (local.id !in remoteMap && local.id !in deletedIds) {
                shiftDao.deleteShift(local.id)
            }
        }

        for (remote in remoteShifts) {
            if (remote.id in deletedIds) {
                firestoreDataSource.deleteShift(userId, remote.id)
                continue
            }
            val local = localShifts.find { it.id == remote.id }
            if (local == null) {
                shiftDao.insertShift(remote)
            } else if (remote.status == "COMPLETED" && local.status == "ACTIVE") {
                shiftDao.updateShift(remote)
            } else if (remote.endOdometer != null && local.endOdometer == null) {
                shiftDao.updateShift(remote)
            }
        }
    }

    suspend fun reconcileTrips(
        userId: String,
        remoteTrips: List<TripEntity>,
        deletedIds: Set<String>
    ) {
        val tripDao = database.tripDao()
        val remoteMap = remoteTrips.associateBy { it.id }
        val localTrips = tripDao.getAllTrips().first()

        for (local in localTrips) {
            if (local.id !in remoteMap && local.id !in deletedIds) {
                tripDao.deleteTrip(local.id)
            }
        }

        for (remote in remoteTrips) {
            if (remote.id in deletedIds) {
                firestoreDataSource.deleteTrip(userId, remote.id)
                continue
            }
            val local = localTrips.find { it.id == remote.id }
            if (local == null) {
                tripDao.insertTrip(remote)
            } else {
                val merged = remote.copy(
                    startOdometer = remote.startOdometer ?: local.startOdometer,
                    endOdometer = remote.endOdometer ?: local.endOdometer,
                    paymentDestination = if (remote.paymentDestination != "CASH_IN_HAND" || local.paymentDestination == "CASH_IN_HAND") remote.paymentDestination else local.paymentDestination
                )
                tripDao.updateTrip(merged)
            }
        }
    }

    suspend fun reconcileFuelings(
        userId: String,
        remoteFuelings: List<FuelingEntity>,
        deletedIds: Set<String>
    ) {
        val fuelingDao = database.fuelingDao()
        val remoteMap = remoteFuelings.associateBy { it.id }
        val localFuelings = fuelingDao.getAllFuelings().first()

        for (local in localFuelings) {
            if (local.id !in remoteMap && local.id !in deletedIds) {
                fuelingDao.deleteFueling(local.id)
            }
        }

        for (remote in remoteFuelings) {
            if (remote.id in deletedIds) {
                firestoreDataSource.deleteFueling(userId, remote.id)
                continue
            }
            val local = localFuelings.find { it.id == remote.id }
            if (local == null) {
                fuelingDao.insertFueling(remote)
            } else {
                val merged = if (remote.odometer == 0 && local.odometer != 0) {
                    remote.copy(odometer = local.odometer)
                } else remote
                fuelingDao.updateFueling(merged)
            }
        }
    }

    suspend fun reconcileExpenses(
        userId: String,
        remoteExpenses: List<ExpenseEntity>,
        deletedIds: Set<String>
    ) {
        val expenseDao = database.expenseDao()
        val remoteMap = remoteExpenses.associateBy { it.id }
        val localExpenses = expenseDao.getAllExpenses().first()

        for (local in localExpenses) {
            if (local.id !in remoteMap && local.id !in deletedIds) {
                expenseDao.deleteExpense(local.id)
            }
        }

        for (remote in remoteExpenses) {
            if (remote.id in deletedIds) {
                firestoreDataSource.deleteExpense(userId, remote.id)
                continue
            }
            val local = localExpenses.find { it.id == remote.id }
            if (local == null) {
                expenseDao.insertExpense(remote)
            } else {
                expenseDao.updateExpense(remote)
            }
        }
    }

    suspend fun reconcileMaintenances(
        userId: String,
        remoteMaintenances: List<MaintenanceEntity>,
        deletedIds: Set<String>
    ) {
        val maintenanceDao = database.maintenanceDao()
        val remoteMap = remoteMaintenances.associateBy { it.id }
        val localMaintenances = maintenanceDao.getAllMaintenances().first()

        for (local in localMaintenances) {
            if (local.id !in remoteMap && local.id !in deletedIds) {
                maintenanceDao.deleteMaintenance(local.id)
            }
        }

        for (remote in remoteMaintenances) {
            if (remote.id in deletedIds) {
                firestoreDataSource.deleteMaintenance(userId, remote.id)
                continue
            }
            val local = localMaintenances.find { it.id == remote.id }
            if (local == null) {
                maintenanceDao.insertMaintenance(remote)
            } else {
                maintenanceDao.updateMaintenance(remote)
            }
        }
    }

    suspend fun reconcileTransfers(
        userId: String,
        remoteTransfers: List<WalletTransferEntity>,
        deletedIds: Set<String>
    ) {
        val transferDao = database.walletTransferDao()
        val remoteMap = remoteTransfers.associateBy { it.id }
        val localTransfers = transferDao.getAllTransfers().first()

        for (local in localTransfers) {
            if (local.id !in remoteMap && local.id !in deletedIds) {
                transferDao.deleteTransferById(local.id)
            }
        }

        for (remote in remoteTransfers) {
            if (remote.id in deletedIds) {
                firestoreDataSource.deleteWalletTransfer(userId, remote.id)
                continue
            }
            val local = localTransfers.find { it.id == remote.id }
            if (local == null) {
                transferDao.insertTransfer(remote)
            } else {
                transferDao.updateTransfer(remote)
            }
        }
    }
}
