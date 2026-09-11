// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/ConsolidateDayShiftsUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para fusionar múltiples jornadas fragmentadas de un mismo día calendario
// en un único Turno Maestro. Reasigna actividades y purga los turnos secundarios
// con lápidas anti-resurrección en Room y Firestore.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.Shift
import com.ananapps.kilometa.domain.model.ShiftStatus
import com.ananapps.kilometa.domain.repository.ShiftRepository
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE CONSOLIDACIÓN DE TURNOS
// ------------------------------------------------------------------------------
/**
 * Fusiona turnos fragmentados de una misma fecha en un único turno maestro.
 */
class ConsolidateDayShiftsUseCase @Inject constructor(
    private val shiftRepository: ShiftRepository
) {

    /**
     * Ejecuta la consolidación de una lista de turnos pertenecientes a una misma fecha.
     *
     * @param dayShifts Lista de turnos a unificar (mínimo 2).
     * @return El [Shift] maestro resultante consolidado.
     */
    suspend operator fun invoke(dayShifts: List<Shift>): Shift {
        require(dayShifts.size >= 2) { "Se requieren al menos 2 turnos para consolidar" }

        // El turno maestro es el activo (si hay alguno) o el primero cronológicamente
        val master = dayShifts.firstOrNull { it.status == ShiftStatus.ACTIVE }
            ?: dayShifts.minByOrNull { it.startTime }
            ?: dayShifts.first()

        val secondaries = dayShifts.filter { it.id != master.id }

        val allStarts = dayShifts.map { it.startTime }
        val minStart = allStarts.minOrNull() ?: master.startTime

        val allEnds = dayShifts.mapNotNull { it.endTime }
        val maxEnd = if (allEnds.isNotEmpty()) allEnds.maxOrNull() else master.endTime

        val allStartOdos = dayShifts.map { it.startOdometer }
        val minOdo = allStartOdos.minOrNull() ?: master.startOdometer

        val allEndOdos = dayShifts.mapNotNull { it.endOdometer }
        val maxOdo = if (allEndOdos.isNotEmpty()) allEndOdos.maxOrNull() else master.endOdometer

        val consolidatedMaster = master.copy(
            startTime = minStart,
            endTime = maxEnd,
            startOdometer = minOdo,
            endOdometer = maxOdo
        )

        shiftRepository.consolidateShifts(consolidatedMaster, secondaries.map { it.id })
        return consolidatedMaster
    }
}
