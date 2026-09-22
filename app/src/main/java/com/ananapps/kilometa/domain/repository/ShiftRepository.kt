// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/ShiftRepository.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contrato de Dominio para la gestión del ciclo de vida y análisis financiero de turnos.
// Expone flujos reactivos mediante Kotlin Flow y operaciones de mutación asíncronas suspend.
// ==============================================================================

package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.Shift
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.Trip
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------------
// BLOQUE 1: INTERFAZ DEL REPOSITORIO DE TURNOS
// ------------------------------------------------------------------------------
/**
 * Define el acceso a datos y operaciones de negocio sobre turnos de conducción.
 */
interface ShiftRepository {

    /**
     * Emite el turno activo actual, o null si no hay ninguna jornada en curso.
     */
    fun getActiveShift(): Flow<Shift?>

    /**
     * Emite el resumen financiero consolidado en tiempo real del turno activo.
     */
    fun getActiveShiftFinancialSummary(): Flow<ShiftFinancialSummary?>

    /**
     * Emite la liquidación de doble bolsillo (efectivo en mano vs saldo en app) del turno activo.
     */
    fun getActiveShiftCashSettlement(): Flow<com.ananapps.kilometa.domain.model.CashSettlementSummary?>

    /**
     * Emite el listado de carreras asociadas al turno activo ordenadas cronológicamente descendente.
     */
    fun getActiveShiftTrips(): Flow<List<Trip>>

    /**
     * Emite el histórico completo de turnos ordenados cronológicamente descendente.
     */
    fun getAllShifts(): Flow<List<Shift>>

    /**
     * Emite el histórico completo de turnos finalizados (COMPLETED) con sus resúmenes financieros.
     */
    fun getCompletedShiftsHistory(): Flow<List<ShiftFinancialSummary>>

    /**
     * Emite el histórico completo de todos los turnos (activos y finalizados) con sus detalles.
     */
    fun getAllShiftsHistoryWithDetails(): Flow<List<ShiftFinancialSummary>>

    /**
     * Emite todas las carreras históricas registradas en el sistema.
     */
    fun getAllTrips(): Flow<List<Trip>>

    /**
     * Emite todos los abastecimientos de combustible históricos registrados en el sistema.
     */
    fun getAllFuelings(): Flow<List<Fueling>>

    /**
     * Inicia una nueva jornada laboral.
     */
    suspend fun startShift(shift: Shift)

    /**
     * Finaliza un turno activo registrando el odómetro final y observaciones.
     */
    suspend fun closeShift(
        shiftId: String,
        endOdometer: Int,
        endTime: Long,
        notes: String? = null
    )

    /**
     * Cierra forzosamente cualquier turno activo remanente en SQLite para evitar inconsistencias.
     */
    suspend fun closeAllActiveShifts(endOdometer: Int, endTime: Long)

    /**
     * Actualiza la información de un turno existente.
     */
    suspend fun updateShift(shift: Shift)

    /**
     * Elimina un turno y todas sus entidades dependientes (en cascada).
     */
    suspend fun deleteShift(shiftId: String)

    /**
     * Emite el último kilometraje conocido registrado en el sistema.
     */
    fun getLastKnownOdometer(): Flow<Int?>

    /**
     * Obtiene todos los turnos que intersecten un rango de fecha/día calendario.
     */
    suspend fun getShiftsForDay(dayStart: Long, dayEnd: Long): List<Shift>

    /**
     * Consolida de forma atómica múltiples turnos secundarios en un turno maestro.
     */
    suspend fun consolidateShifts(masterShift: Shift, secondaryShiftIds: List<String>)
}
