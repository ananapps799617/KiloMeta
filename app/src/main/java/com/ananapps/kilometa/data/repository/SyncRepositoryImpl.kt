// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/SyncRepositoryImpl.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación del motor de sincronización Cloud Sync Engine y Disaster Recovery.
// Orquesta el flujo de sincronización delta hacia Firestore y la hidratación
// en orden topológico respetando las claves foráneas de Room (Shifts -> Hijos).
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.core.dispatcher.CoroutineDispatchers
import com.ananapps.kilometa.data.local.dao.ExpenseDao
import com.ananapps.kilometa.data.local.dao.FuelingDao
import com.ananapps.kilometa.data.local.dao.ShiftDao
import com.ananapps.kilometa.data.local.dao.TripDao
import com.ananapps.kilometa.data.remote.firebase.FirestoreDataSource
import com.ananapps.kilometa.data.remote.mapper.toExpenseDtoList
import com.ananapps.kilometa.data.remote.mapper.toExpenseEntityList
import com.ananapps.kilometa.data.remote.mapper.toFuelingDtoList
import com.ananapps.kilometa.data.remote.mapper.toFuelingEntityList
import com.ananapps.kilometa.data.remote.mapper.toShiftDtoList
import com.ananapps.kilometa.data.remote.mapper.toShiftEntityList
import com.ananapps.kilometa.data.remote.mapper.toTripDtoList
import com.ananapps.kilometa.data.remote.mapper.toTripEntityList
import com.ananapps.kilometa.domain.model.ConnectionDiagnostic
import com.ananapps.kilometa.domain.model.RestoreProgress
import com.ananapps.kilometa.domain.model.SyncState
import com.ananapps.kilometa.domain.model.SyncStatus
import com.ananapps.kilometa.domain.repository.AuthRepository
import com.ananapps.kilometa.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: IMPLEMENTACIÓN DEL MOTOR DE SINCRONIZACIÓN
// ------------------------------------------------------------------------------
@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val shiftDao: ShiftDao,
    private val tripDao: TripDao,
    private val fuelingDao: FuelingDao,
    private val expenseDao: ExpenseDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val authRepository: AuthRepository,
    private val dispatchers: CoroutineDispatchers
) : SyncRepository {

    private val _syncStatus = MutableStateFlow(
        SyncStatus(state = SyncState.IDLE)
    )

    override fun observeSyncStatus(): Flow<SyncStatus> = _syncStatus.asStateFlow()

    // --------------------------------------------------------------------------
    // BLOQUE 2: PUSH DE MUTACIONES PENDIENTES
    // --------------------------------------------------------------------------
    override suspend fun pushPendingChanges(): Result<Unit> = withContext(dispatchers.io) {
        val user = authRepository.getCurrentUser()
        if (user == null) {
            _syncStatus.value = SyncStatus(state = SyncState.OFFLINE)
            return@withContext Result.failure(
                IllegalStateException("No hay sesión activa para respaldar en la nube")
            )
        }

        _syncStatus.value = _syncStatus.value.copy(state = SyncState.SYNCING)

        runCatching {
            val shifts = shiftDao.getAllShifts().first().toShiftDtoList()
            val trips = tripDao.getAllTrips().first().toTripDtoList()
            val fuelings = fuelingDao.getAllFuelings().first().toFuelingDtoList()
            val expenses = expenseDao.getAllExpenses().first().toExpenseDtoList()

            firestoreDataSource.saveShifts(user.uid, shifts).getOrThrow()
            firestoreDataSource.saveTrips(user.uid, trips).getOrThrow()
            firestoreDataSource.saveFuelings(user.uid, fuelings).getOrThrow()
            firestoreDataSource.saveExpenses(user.uid, expenses).getOrThrow()

            _syncStatus.value = SyncStatus(
                state = SyncState.SUCCESS,
                lastSyncTimestamp = System.currentTimeMillis(),
                pendingMutationsCount = 0
            )
        }.onFailure { error ->
            _syncStatus.value = SyncStatus(
                state = SyncState.ERROR,
                errorMessage = error.localizedMessage ?: "Error al subir cambios a la nube"
            )
        }
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: PULL DE ACTUALIZACIONES REMOTAS
    // --------------------------------------------------------------------------
    override suspend fun pullRemoteChanges(): Result<Unit> = withContext(dispatchers.io) {
        val user = authRepository.getCurrentUser()
            ?: return@withContext Result.failure(IllegalStateException("Sesión no disponible"))

        _syncStatus.value = _syncStatus.value.copy(state = SyncState.SYNCING)

        runCatching {
            val remoteShifts = firestoreDataSource.getShifts(user.uid).getOrThrow().toShiftEntityList()
            val remoteTrips = firestoreDataSource.getTrips(user.uid).getOrThrow().toTripEntityList()
            val remoteFuelings = firestoreDataSource.getFuelings(user.uid).getOrThrow().toFuelingEntityList()
            val remoteExpenses = firestoreDataSource.getExpenses(user.uid).getOrThrow().toExpenseEntityList()

            // Inserción en orden topológico (Primero padres, luego hijos con FK)
            for (shift in remoteShifts) { shiftDao.insertShift(shift) }
            for (trip in remoteTrips) { tripDao.insertTrip(trip) }
            for (fueling in remoteFuelings) { fuelingDao.insertFueling(fueling) }
            for (expense in remoteExpenses) { expenseDao.insertExpense(expense) }

            _syncStatus.value = SyncStatus(
                state = SyncState.SUCCESS,
                lastSyncTimestamp = System.currentTimeMillis()
            )
        }.onFailure { error ->
            _syncStatus.value = SyncStatus(
                state = SyncState.ERROR,
                errorMessage = error.localizedMessage ?: "Error al descargar cambios remotos"
            )
        }
    }

    // --------------------------------------------------------------------------
    // BLOQUE 4: RECUPERACIÓN TOTAL ANTE DESASTRES (DISASTER RECOVERY)
    // --------------------------------------------------------------------------
    override fun fullDisasterRecovery(): Flow<RestoreProgress> = flow {
        emit(RestoreProgress.Running("Iniciando conexión de seguridad...", 0, 0.05f))

        val user = authRepository.getCurrentUser()
        if (user == null) {
            emit(RestoreProgress.Failed("No hay sesión autenticada para restaurar."))
            return@flow
        }

        try {
            _syncStatus.value = _syncStatus.value.copy(state = SyncState.SYNCING)

            // 1. Reconstrucción de Jornadas
            emit(RestoreProgress.Running("Descargando Jornadas...", 0, 0.15f))
            val shifts = firestoreDataSource.getShifts(user.uid).getOrThrow().toShiftEntityList()
            for (shift in shifts) { shiftDao.insertShift(shift) }
            emit(RestoreProgress.Running("Jornadas reconstruidas", shifts.size, 0.35f))

            // 2. Reconstrucción de Carreras
            emit(RestoreProgress.Running("Descargando Carreras...", 0, 0.45f))
            val trips = firestoreDataSource.getTrips(user.uid).getOrThrow().toTripEntityList()
            for (trip in trips) { tripDao.insertTrip(trip) }
            emit(RestoreProgress.Running("Carreras reconstruidas", trips.size, 0.60f))

            // 3. Reconstrucción de Abastecimientos
            emit(RestoreProgress.Running("Descargando Tanqueos...", 0, 0.70f))
            val fuelings = firestoreDataSource.getFuelings(user.uid).getOrThrow().toFuelingEntityList()
            for (fueling in fuelings) { fuelingDao.insertFueling(fueling) }
            emit(RestoreProgress.Running("Tanqueos reconstruidos", fuelings.size, 0.80f))

            // 4. Reconstrucción de Gastos
            emit(RestoreProgress.Running("Descargando Gastos en Ruta...", 0, 0.85f))
            val expenses = firestoreDataSource.getExpenses(user.uid).getOrThrow().toExpenseEntityList()
            for (expense in expenses) { expenseDao.insertExpense(expense) }
            emit(RestoreProgress.Running("Gastos reconstruidos", expenses.size, 0.95f))

            // 5. Finalización exitosa
            _syncStatus.value = SyncStatus(
                state = SyncState.SUCCESS,
                lastSyncTimestamp = System.currentTimeMillis()
            )
            emit(RestoreProgress.Completed(shifts.size, trips.size, fuelings.size, expenses.size))
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus(
                state = SyncState.ERROR,
                errorMessage = e.localizedMessage ?: "Error en hidratación"
            )
            emit(RestoreProgress.Failed(e.localizedMessage ?: "Fallo inesperado al restaurar base de datos"))
        }
    }.flowOn(dispatchers.io)

    // --------------------------------------------------------------------------
    // BLOQUE 5: DIAGNÓSTICO DE ENLACE EN VIVO
    // --------------------------------------------------------------------------
    override suspend fun checkCloudConnection(): ConnectionDiagnostic = withContext(dispatchers.io) {
        val user = authRepository.getCurrentUser()
        if (user == null) {
            return@withContext ConnectionDiagnostic(
                isConnected = false,
                latencyMs = null,
                timestampMillis = System.currentTimeMillis(),
                message = "Sin sesión activa para verificar enlace",
                isPlaceholderConfig = false
            )
        }
        firestoreDataSource.pingDiagnostic(user.uid)
    }
}
