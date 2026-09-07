// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/remote/firebase/FirestoreDataSource.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Fuente de datos remota para Google Cloud Firestore. Implementa persistencia por lotes (Batches)
// bajo la ruta segura 'users/{userId}/[shifts|trips|fuelings|expenses]'.
// Resiliente a caídas y ausencia de servicios de Google Play (Cero crasheos).
// ==============================================================================

package com.ananapps.kilometa.data.remote.firebase

import com.ananapps.kilometa.data.remote.dto.ExpenseDto
import com.ananapps.kilometa.data.remote.dto.FuelingDto
import com.ananapps.kilometa.data.remote.dto.ShiftDto
import com.ananapps.kilometa.data.remote.dto.TripDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// ------------------------------------------------------------------------------
// BLOQUE 1: FUENTE DE DATOS REMOTA FIRESTORE
// ------------------------------------------------------------------------------
@Singleton
class FirestoreDataSource @Inject constructor() {

    private fun getFirestoreSafe(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: ESCRITURA EN LA NUBE (PUSH POR ENTIDAD)
    // --------------------------------------------------------------------------
    suspend fun saveShifts(userId: String, shifts: List<ShiftDto>): Result<Unit> = runCatching {
        if (shifts.isEmpty()) return@runCatching
        val db = getFirestoreSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseFirestore no inicializado")

        val batch = db.batch()
        for (shift in shifts) {
            val docRef = db.collection("users").document(userId).collection("shifts").document(shift.id)
            batch.set(docRef, shift, SetOptions.merge())
        }
        batch.commit().await()
    }

    suspend fun saveTrips(userId: String, trips: List<TripDto>): Result<Unit> = runCatching {
        if (trips.isEmpty()) return@runCatching
        val db = getFirestoreSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseFirestore no inicializado")

        val batch = db.batch()
        for (trip in trips) {
            val docRef = db.collection("users").document(userId).collection("trips").document(trip.id)
            batch.set(docRef, trip, SetOptions.merge())
        }
        batch.commit().await()
    }

    suspend fun saveFuelings(userId: String, fuelings: List<FuelingDto>): Result<Unit> = runCatching {
        if (fuelings.isEmpty()) return@runCatching
        val db = getFirestoreSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseFirestore no inicializado")

        val batch = db.batch()
        for (fueling in fuelings) {
            val docRef = db.collection("users").document(userId).collection("fuelings").document(fueling.id)
            batch.set(docRef, fueling, SetOptions.merge())
        }
        batch.commit().await()
    }

    suspend fun saveExpenses(userId: String, expenses: List<ExpenseDto>): Result<Unit> = runCatching {
        if (expenses.isEmpty()) return@runCatching
        val db = getFirestoreSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseFirestore no inicializado")

        val batch = db.batch()
        for (expense in expenses) {
            val docRef = db.collection("users").document(userId).collection("expenses").document(expense.id)
            batch.set(docRef, expense, SetOptions.merge())
        }
        batch.commit().await()
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: LECTURA Y DESCARGA (PULL Y DISASTER RECOVERY)
    // --------------------------------------------------------------------------
    suspend fun getShifts(userId: String, sinceTimestamp: Long = 0L): Result<List<ShiftDto>> = runCatching {
        val db = getFirestoreSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseFirestore no inicializado")

        val snapshot = db.collection("users").document(userId)
            .collection("shifts")
            .whereGreaterThanOrEqualTo("updatedAt", sinceTimestamp)
            .get()
            .await()

        snapshot.toObjects(ShiftDto::class.java)
    }

    suspend fun getTrips(userId: String, sinceTimestamp: Long = 0L): Result<List<TripDto>> = runCatching {
        val db = getFirestoreSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseFirestore no inicializado")

        val snapshot = db.collection("users").document(userId)
            .collection("trips")
            .whereGreaterThanOrEqualTo("updatedAt", sinceTimestamp)
            .get()
            .await()

        snapshot.toObjects(TripDto::class.java)
    }

    suspend fun getFuelings(userId: String, sinceTimestamp: Long = 0L): Result<List<FuelingDto>> = runCatching {
        val db = getFirestoreSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseFirestore no inicializado")

        val snapshot = db.collection("users").document(userId)
            .collection("fuelings")
            .whereGreaterThanOrEqualTo("updatedAt", sinceTimestamp)
            .get()
            .await()

        snapshot.toObjects(FuelingDto::class.java)
    }

    suspend fun getExpenses(userId: String, sinceTimestamp: Long = 0L): Result<List<ExpenseDto>> = runCatching {
        val db = getFirestoreSafe()
            ?: throw IllegalStateException("⚠️ PENDIENTE DE CONEXIÓN REAL: FirebaseFirestore no inicializado")

        val snapshot = db.collection("users").document(userId)
            .collection("expenses")
            .whereGreaterThanOrEqualTo("updatedAt", sinceTimestamp)
            .get()
            .await()

        snapshot.toObjects(ExpenseDto::class.java)
    }
}
