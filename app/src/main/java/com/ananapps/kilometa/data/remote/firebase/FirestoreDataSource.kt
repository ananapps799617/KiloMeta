// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/remote/firebase/FirestoreDataSource.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Fuente de datos remota para Google Cloud Firestore. Implementa persistencia por lotes,
// soporte completo para Mantenimientos y diagnóstico de enlace round-trip con Source.SERVER.
// Resiliente a caídas y ausencia de servicios de Google Play (Cero crasheos).
// ==============================================================================

package com.ananapps.kilometa.data.remote.firebase

import com.ananapps.kilometa.data.remote.dto.ExpenseDto
import com.ananapps.kilometa.data.remote.dto.FuelingDto
import com.ananapps.kilometa.data.remote.dto.MaintenanceDto
import com.ananapps.kilometa.data.remote.dto.ShiftDto
import com.ananapps.kilometa.data.remote.dto.TripDto
import com.ananapps.kilometa.data.remote.dto.WalletTransferDto
import com.ananapps.kilometa.domain.model.ConnectionDiagnostic
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private fun getFirestoreSafe(): FirebaseFirestore? = firestore

    // --------------------------------------------------------------------------
    // BLOQUE 1: UTILITARIOS GENÉRICOS DE LOTE Y DESCARGA
    // --------------------------------------------------------------------------
    private suspend inline fun <T : Any> saveBatch(
        userId: String,
        collection: String,
        items: List<T>,
        crossinline idSelector: (T) -> String
    ): Result<Unit> = runCatching {
        if (items.isEmpty()) return@runCatching
        val db = getFirestoreSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseFirestore no inicializado")

        val batch = db.batch()
        for (item in items) {
            val docRef = db.collection("users").document(userId).collection(collection).document(idSelector(item))
            batch.set(docRef, item, SetOptions.merge())
        }
        batch.commit().await()
    }

    private suspend fun <T : Any> fetchCollection(
        userId: String,
        collection: String,
        clazz: Class<T>,
        sinceTimestamp: Long = 0L
    ): Result<List<T>> = runCatching {
        val db = getFirestoreSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseFirestore no inicializado")

        val snapshot = db.collection("users").document(userId)
            .collection(collection)
            .whereGreaterThanOrEqualTo("updatedAt", sinceTimestamp)
            .get()
            .await()

        snapshot.toObjects(clazz)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: ESCRITURA EN LA NUBE (PUSH POR ENTIDAD)
    // --------------------------------------------------------------------------
    suspend fun saveShifts(userId: String, shifts: List<ShiftDto>): Result<Unit> =
        saveBatch(userId, "shifts", shifts) { it.id }

    suspend fun saveTrips(userId: String, trips: List<TripDto>): Result<Unit> =
        saveBatch(userId, "trips", trips) { it.id }

    suspend fun saveFuelings(userId: String, fuelings: List<FuelingDto>): Result<Unit> =
        saveBatch(userId, "fuelings", fuelings) { it.id }

    suspend fun saveExpenses(userId: String, expenses: List<ExpenseDto>): Result<Unit> =
        saveBatch(userId, "expenses", expenses) { it.id }

    suspend fun saveWalletTransfers(userId: String, transfers: List<WalletTransferDto>): Result<Unit> =
        saveBatch(userId, "transfers", transfers) { it.id }

    suspend fun saveMaintenances(userId: String, maintenances: List<MaintenanceDto>): Result<Unit> =
        saveBatch(userId, "maintenances", maintenances) { it.id }

    // --------------------------------------------------------------------------
    // BLOQUE 3: ELIMINACIÓN REMOTA EN LA NUBE (ANTI-RESURRECCIÓN)
    // --------------------------------------------------------------------------
    private suspend fun deleteUserEntity(userId: String, collection: String, entityId: String): Result<Unit> = runCatching {
        val db = getFirestoreSafe() ?: return@runCatching
        db.collection("users").document(userId).collection(collection).document(entityId).delete().await()
    }

    suspend fun deleteTrip(userId: String, tripId: String): Result<Unit> = deleteUserEntity(userId, "trips", tripId)
    suspend fun deleteFueling(userId: String, fuelingId: String): Result<Unit> = deleteUserEntity(userId, "fuelings", fuelingId)
    suspend fun deleteExpense(userId: String, expenseId: String): Result<Unit> = deleteUserEntity(userId, "expenses", expenseId)
    suspend fun deleteShift(userId: String, shiftId: String): Result<Unit> = deleteUserEntity(userId, "shifts", shiftId)
    suspend fun deleteWalletTransfer(userId: String, transferId: String): Result<Unit> = deleteUserEntity(userId, "transfers", transferId)
    suspend fun deleteMaintenance(userId: String, maintenanceId: String): Result<Unit> = deleteUserEntity(userId, "maintenances", maintenanceId)

    suspend fun deleteAllUserData(userId: String): Result<Unit> = runCatching {
        val db = getFirestoreSafe() ?: return@runCatching
        val userDoc = db.collection("users").document(userId)
        val subcollections = listOf("trips", "shifts", "fuelings", "expenses", "transfers", "maintenances", "profile", "diagnostic")
        for (col in subcollections) {
            val snapshot = userDoc.collection(col).get().await()
            if (!snapshot.isEmpty) {
                val batch = db.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            }
        }
        userDoc.delete().await()
    }

    suspend fun saveDriverProfile(userId: String, profileMap: Map<String, Any>): Result<Unit> = runCatching {
        val db = getFirestoreSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseFirestore no inicializado")
        val docRef = db.collection("users").document(userId).collection("profile").document("current")
        docRef.set(profileMap, SetOptions.merge()).await()
    }

    suspend fun getDriverProfile(userId: String): Result<Map<String, Any>?> = runCatching {
        val db = getFirestoreSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseFirestore no inicializado")
        val docRef = db.collection("users").document(userId).collection("profile").document("current")
        val snapshot = docRef.get().await()
        if (snapshot.exists()) snapshot.data else null
    }

    // --------------------------------------------------------------------------
    // BLOQUE 4: LECTURA Y DESCARGA (PULL Y DISASTER RECOVERY)
    // --------------------------------------------------------------------------
    suspend fun getShifts(userId: String, sinceTimestamp: Long = 0L): Result<List<ShiftDto>> =
        fetchCollection(userId, "shifts", ShiftDto::class.java, sinceTimestamp)

    suspend fun getTrips(userId: String, sinceTimestamp: Long = 0L): Result<List<TripDto>> =
        fetchCollection(userId, "trips", TripDto::class.java, sinceTimestamp)

    suspend fun getFuelings(userId: String, sinceTimestamp: Long = 0L): Result<List<FuelingDto>> =
        fetchCollection(userId, "fuelings", FuelingDto::class.java, sinceTimestamp)

    suspend fun getExpenses(userId: String, sinceTimestamp: Long = 0L): Result<List<ExpenseDto>> =
        fetchCollection(userId, "expenses", ExpenseDto::class.java, sinceTimestamp)

    suspend fun getWalletTransfers(userId: String, sinceTimestamp: Long = 0L): Result<List<WalletTransferDto>> =
        fetchCollection(userId, "transfers", WalletTransferDto::class.java, sinceTimestamp)

    suspend fun getMaintenances(userId: String, sinceTimestamp: Long = 0L): Result<List<MaintenanceDto>> =
        fetchCollection(userId, "maintenances", MaintenanceDto::class.java, sinceTimestamp)

    // --------------------------------------------------------------------------
    // BLOQUE 5: DIAGNÓSTICO DE ENLACE EN VIVO (ROUND-TRIP TEST)
    // --------------------------------------------------------------------------
    suspend fun pingDiagnostic(userId: String): ConnectionDiagnostic {
        val startTime = System.currentTimeMillis()
        return try {
            val db = getFirestoreSafe()
                ?: return ConnectionDiagnostic(
                    isConnected = false,
                    latencyMs = null,
                    timestampMillis = System.currentTimeMillis(),
                    message = "⚠️ PENDIENTE DE CONEXIÓN REAL: Firestore no configurado",
                    isPlaceholderConfig = true
                )

            val pingRef = db.collection("users")
                .document(userId)
                .collection("diagnostic")
                .document("ping")

            val payload = mapOf(
                "timestamp" to startTime,
                "platform" to "Android",
                "status" to "OK"
            )

            pingRef.set(payload).await()
            pingRef.get(Source.SERVER).await()

            val latency = System.currentTimeMillis() - startTime
            ConnectionDiagnostic(
                isConnected = true,
                latencyMs = latency,
                timestampMillis = System.currentTimeMillis(),
                message = "Enlace en vivo confirmado con Cloud Firestore",
                isPlaceholderConfig = false
            )
        } catch (e: Exception) {
            ConnectionDiagnostic(
                isConnected = false,
                latencyMs = null,
                timestampMillis = System.currentTimeMillis(),
                message = e.localizedMessage ?: "Error de comunicación con Firestore",
                isPlaceholderConfig = true
            )
        }
    }
}
