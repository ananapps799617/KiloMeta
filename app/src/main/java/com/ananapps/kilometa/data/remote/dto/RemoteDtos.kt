// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/remote/dto/RemoteDtos.kt
// DESCRIPCIÓN PEDAGÓGICA:
// DTOs planos y serializables para sincronización con Google Cloud Firestore.
// Incluyen marcas de tiempo 'updatedAt' para sincronización delta/incremental
// y banderas 'deleted' para soporte de borrado lógico sin pérdida de trazabilidad.
// ==============================================================================

package com.ananapps.kilometa.data.remote.dto

// ------------------------------------------------------------------------------
// BLOQUE 1: DTO REMOTO PARA JORNADAS / TURNOS (SHIFTS)
// ------------------------------------------------------------------------------
/**
 * Representación serializable de un turno para almacenamiento en la colección 'shifts' de Firestore.
 */
data class ShiftDto(
    val id: String = "",
    val startTime: Long = 0L,
    val endTime: Long? = null,
    val startOdometer: Int = 0,
    val endOdometer: Int? = null,
    val dailyQuotaTarget: Double = 0.0,
    val status: String = "ACTIVE",
    val notes: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
)

// ------------------------------------------------------------------------------
// BLOQUE 2: DTO REMOTO PARA CARRERAS / SERVICIOS (TRIPS)
// ------------------------------------------------------------------------------
/**
 * Representación serializable de una carrera para la subcolección 'trips' de Firestore.
 */
data class TripDto(
    val id: String = "",
    val shiftId: String = "",
    val platformName: String = "",
    val grossAmount: Double = 0.0,
    val commissionAmount: Double = 0.0,
    val netAmount: Double = 0.0,
    val paymentMethod: String = "",
    val paymentDestination: String = "CASH_IN_HAND",
    val timestamp: Long = 0L,
    val startOdometer: Int? = null,
    val endOdometer: Int? = null,
    val platformPromoDiscount: Double = 0.0,
    val distanceKm: Double? = null,
    val durationMinutes: Int? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
)

// ------------------------------------------------------------------------------
// BLOQUE 3: DTO REMOTO PARA ABASTECIMIENTOS DE COMBUSTIBLE (FUELINGS)
// ------------------------------------------------------------------------------
/**
 * Representación serializable de un tanqueo bi-combustible para la subcolección 'fuelings'.
 */
data class FuelingDto(
    val id: String = "",
    val shiftId: String = "",
    val fuelType: String = "",
    val amountPaid: Double = 0.0,
    val volume: Double = 0.0,
    val odometer: Int = 0,
    val timestamp: Long = 0L,
    val paymentSource: String = "CASH_PHYSICAL",
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
)

// ------------------------------------------------------------------------------
// BLOQUE 4: DTO REMOTO PARA GASTOS OPERATIVOS EN RUTA (EXPENSES)
// ------------------------------------------------------------------------------
/**
 * Representación serializable de un gasto operativo para la subcolección 'expenses'.
 */
data class ExpenseDto(
    val id: String = "",
    val shiftId: String = "",
    val concept: String = "",
    val amount: Double = 0.0,
    val timestamp: Long = 0L,
    val paymentSource: String = "CASH_PHYSICAL",
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
)

// ------------------------------------------------------------------------------
// BLOQUE 5: DTO REMOTO PARA TRANSFERENCIAS INTER-BILLETERAS (WALLET TRANSFERS)
// ------------------------------------------------------------------------------
/**
 * Representación serializable de una transferencia entre bolsillos (Orden #033).
 */
data class WalletTransferDto(
    val id: String = "",
    val timestamp: Long = 0L,
    val sourceWallet: String = "",
    val destinationWallet: String = "",
    val amount: Double = 0.0,
    val transferType: String = "",
    val notes: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
)

// ------------------------------------------------------------------------------
// BLOQUE 6: DTO REMOTO PARA MANTENIMIENTOS MECÁNICOS
// ------------------------------------------------------------------------------
data class MaintenanceDto(
    val id: String = "",
    val vehicleId: String = "",
    val maintenanceType: String = "",
    val odometer: Int = 0,
    val costCop: Double = 0.0,
    val notes: String? = null,
    val nextDueOdometer: Int? = null,
    val nextDueDateMillis: Long? = null,
    val timestamp: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
)
