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
import java.util.UUID
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: GESTOR DEL CICLO DE VIDA DE TURNOS
// ------------------------------------------------------------------------------
/**
 * Controla el inicio, seguimiento y cierre de jornadas laborales.
 */
class ManageShiftUseCase @Inject constructor(
    private val shiftRepository: ShiftRepository
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
     * Crea un turno completado atómico para registrar movimientos esporádicos fuera de jornada.
     */
    suspend fun createSporadicShift(
        odometer: Int = 100_000,
        notes: String = "Movimiento esporádico"
    ): Shift {
        val now = System.currentTimeMillis()
        val shift = Shift(
            id = UUID.randomUUID().toString(),
            startTime = now,
            endTime = now,
            startOdometer = odometer,
            endOdometer = odometer,
            dailyQuotaTarget = 0.0,
            status = ShiftStatus.COMPLETED,
            notes = notes
        )
        shiftRepository.startShift(shift)
        return shift
    }
}
