// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/ManageShiftUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para la gestión del ciclo de vida de los turnos de trabajo.
// Encapsula las transiciones de arranque (con validación de odómetro >= 0) y cierre
// (garantizando que el odómetro final nunca sea menor al inicial), interactuando con ShiftRepository.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.Shift
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.ShiftStatus
import com.ananapps.kilometa.domain.repository.ShiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: GESTOR DEL CICLO DE VIDA DE TURNOS
// ------------------------------------------------------------------------------
/**
 * Controla el inicio, seguimiento y cierre de jornadas laborales.
 */
class ManageShiftUseCase @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val getLastKnownOdometerUseCase: GetLastKnownOdometerUseCase? = null
) {

    /**
     * Observa el turno activo actual.
     */
    fun getActiveShift(): Flow<Shift?> = shiftRepository.getActiveShift()

    /**
     * Observa el resumen financiero en tiempo real del turno en curso.
     */
    fun getActiveShiftFinancialSummary(): Flow<ShiftFinancialSummary?> =
        shiftRepository.getActiveShiftFinancialSummary()

    /**
     * Inicia una nueva jornada de conducción validando el kilometraje inicial.
     *
     * @param startOdometer Lectura del odómetro al inicio del turno en km (>= 0).
     * @param dailyQuotaTarget Meta financiera diaria fijada para el turno (COP).
     * @param startTime Momento Unix de apertura del turno en milisegundos.
     * @return [Shift] creado y persistido con estado [ShiftStatus.ACTIVE].
     * @throws IllegalArgumentException si el odómetro o la cuota son inconsistentes.
     */
    suspend fun startShift(
        startOdometer: Int,
        dailyQuotaTarget: Double = 83_333.0,
        startTime: Long = System.currentTimeMillis()
    ): Shift {
        // Domain Guards
        require(startOdometer >= 0) { "El odómetro inicial debe ser mayor o igual a cero" }
        require(dailyQuotaTarget >= 0.0) { "La meta de cuota diaria debe ser mayor o igual a cero" }

        val shift = Shift(
            id = UUID.randomUUID().toString(),
            startTime = startTime,
            endTime = null,
            startOdometer = startOdometer,
            endOdometer = null,
            dailyQuotaTarget = dailyQuotaTarget,
            status = ShiftStatus.ACTIVE,
            notes = null
        )

        shiftRepository.startShift(shift)
        return shift
    }

    /**
     * Cierra el turno activo validando la coherencia del odómetro final.
     *
     * @param shiftId Identificador único del turno a cerrar.
     * @param startOdometer Lectura inicial con la que arrancó el turno.
     * @param endOdometer Lectura final del odómetro al concluir la jornada (>= startOdometer).
     * @param endTime Momento Unix de finalización en milisegundos.
     * @param notes Observaciones finales del turno.
     * @throws IllegalArgumentException si el odómetro final es inferior al inicial.
     */
    suspend fun closeShift(
        shiftId: String,
        startOdometer: Int,
        endOdometer: Int,
        endTime: Long = System.currentTimeMillis(),
        notes: String? = null
    ) {
        // Domain Guards: Odometría consistente
        require(shiftId.isNotBlank()) { "El identificador del turno no puede estar vacío" }
        require(endOdometer >= startOdometer) {
            "El odómetro final ($endOdometer km) no puede ser inferior al inicial ($startOdometer km)"
        }

        shiftRepository.closeShift(
            shiftId = shiftId,
            endOdometer = endOdometer,
            endTime = endTime,
            notes = notes
        )
    }

    /**
     * Obtiene la jornada existente para la fecha de [timestamp], o crea una nueva si no existe.
     * Si ya existen múltiples turnos fragmentados para ese día (ej. turnos de 0 min),
     * los consolida en un Turno Maestro, actualizando los extremos temporales y de odómetro.
     */
    suspend fun getOrCreateShiftForDay(
        timestamp: Long = System.currentTimeMillis(),
        odometer: Int? = null,
        notes: String = "Jornada diaria"
    ): Shift {
        val zone = java.time.ZoneId.systemDefault()
        val localDate = java.time.Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()
        val dayStart = localDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = localDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val existingShifts = shiftRepository.getShiftsForDay(dayStart, dayEnd)

        // 1. Si hay una jornada activa en ese día, la retornamos de inmediato
        val activeShift = existingShifts.firstOrNull { it.status == ShiftStatus.ACTIVE }
        if (activeShift != null) {
            return activeShift
        }

        // 2. Si no hay turnos para esa fecha, creamos la jornada única para el día
        if (existingShifts.isEmpty()) {
            val odo = odometer ?: getLastKnownOdometerUseCase?.getSync() ?: (shiftRepository.getLastKnownOdometer().first() ?: 0)
            val shift = Shift(
                id = UUID.randomUUID().toString(),
                startTime = timestamp,
                endTime = timestamp,
                startOdometer = odo,
                endOdometer = odo,
                dailyQuotaTarget = 0.0,
                status = ShiftStatus.COMPLETED,
                notes = notes
            )
            shiftRepository.startShift(shift)
            return shift
        }

        // 3. Si ya existe al menos un turno para este día, unificamos / consolidamos
        val master = existingShifts.first()
        val secondaries = existingShifts.drop(1)

        val allStarts = listOfNotNull(master.startTime, timestamp) + secondaries.map { it.startTime }
        val minStart = allStarts.minOrNull() ?: master.startTime

        val allEnds = listOfNotNull(master.endTime ?: master.startTime, timestamp) + secondaries.map { it.endTime ?: it.startTime }
        val maxEnd = allEnds.maxOrNull() ?: master.startTime

        val allStartOdos = listOfNotNull(master.startOdometer, odometer) + secondaries.map { it.startOdometer }
        val minOdo = allStartOdos.minOrNull() ?: master.startOdometer

        val allEndOdos = listOfNotNull(master.endOdometer ?: master.startOdometer, odometer) + secondaries.map { it.endOdometer ?: it.startOdometer }
        val maxOdo = allEndOdos.maxOrNull() ?: (master.endOdometer ?: master.startOdometer)

        val consolidatedMaster = master.copy(
            startTime = minStart,
            endTime = maxEnd,
            startOdometer = minOdo,
            endOdometer = maxOdo
        )

        if (secondaries.isNotEmpty()) {
            shiftRepository.consolidateShifts(consolidatedMaster, secondaries.map { it.id })
        } else {
            shiftRepository.updateShift(consolidatedMaster)
        }

        return consolidatedMaster
    }

    /**
     * Delegado de compatibilidad para registrar movimientos esporádicos agrupados por día.
     */
    suspend fun createSporadicShift(
        odometer: Int? = null,
        notes: String = "Movimiento esporádico"
    ): Shift = getOrCreateShiftForDay(System.currentTimeMillis(), odometer, notes)
}
