// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/GetShiftHistoryUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso de Dominio que obtiene el flujo reactivo de turnos de trabajo finalizados
// (COMPLETED) con sus resúmenes financieros integrales ordenados cronológicamente descendente.
// Regla arquitectural: Cero dependencias del framework Android en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.repository.ShiftRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE HISTORIAL DE TURNOS FINALIZADOS
// ------------------------------------------------------------------------------
/**
 * Recupera el histórico de jornadas laborales cerradas para visualización y auditoría contable.
 *
 * @param shiftRepository Contrato del repositorio de turnos.
 */
class GetShiftHistoryUseCase @Inject constructor(
    private val shiftRepository: ShiftRepository
) {

    /**
     * Emite la lista cronológica descendente de resúmenes de turnos completados.
     *
     * @return Flow reactivo que emite la colección inmutable de [ShiftFinancialSummary].
     */
    operator fun invoke(): Flow<List<ShiftFinancialSummary>> {
        return shiftRepository.getCompletedShiftsHistory()
    }
}
