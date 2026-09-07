// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/GetShiftFinancialStatusUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso que evalúa el estado del Semáforo Financiero en tiempo real a partir
// del resumen del turno activo y de la cuota diaria asignada o prorrateada.
// Determina con exactitud los estados Rojo (Déficit), Ámbar (Aceleración >=85%)
// y Verde (Turno Libre con excedente de utilidad).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.FinancialStatus
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: EVALUADOR DEL SEMÁFORO FINANCIERO DE CABINA
// ------------------------------------------------------------------------------
/**
 * Traduce las métricas financieras del turno a un estado cromático intuitivo
 * para el conductor con visibilidad instantánea a 60 cm del panel.
 */
class GetShiftFinancialStatusUseCase @Inject constructor() {

    /**
     * Evalúa el resumen financiero contra la cuota diaria y genera el estado del semáforo.
     *
     * @param summary Resumen financiero consolidado del turno actual.
     * @param targetQuota Cuota diaria objetivo (si es null o 0, utiliza la meta del turno).
     * @return [FinancialStatus] con los montos exactos de déficit o ganancia libre.
     */
    operator fun invoke(
        summary: ShiftFinancialSummary,
        targetQuota: Double? = null
    ): FinancialStatus {
        val effectiveTarget = when {
            targetQuota != null && targetQuota > 0.0 -> targetQuota
            summary.dailyQuotaTarget > 0.0 -> summary.dailyQuotaTarget
            else -> 0.0
        }

        val netProfit = summary.netProfit

        // Caso límite: si no hay cuota fijada (meta 0.0)
        if (effectiveTarget <= 0.0) {
            return if (netProfit >= 0.0) {
                FinancialStatus.Green(surplus = netProfit)
            } else {
                FinancialStatus.Red(remaining = -netProfit)
            }
        }

        val amberThreshold = effectiveTarget * Constants.AMBER_THRESHOLD_PERCENTAGE

        return when {
            // Estado Verde: Ganancia neta cubre o supera la cuota diaria fijada (Turno Libre)
            netProfit >= effectiveTarget -> {
                val surplus = netProfit - effectiveTarget
                FinancialStatus.Green(surplus = surplus)
            }

            // Estado Ámbar: Se ha cubierto al menos el 85% de la cuota diaria
            netProfit >= amberThreshold -> {
                val remaining = effectiveTarget - netProfit
                FinancialStatus.Amber(remaining = remaining)
            }

            // Estado Rojo: En déficit operativo o en fase inicial de cobertura
            else -> {
                val remaining = effectiveTarget - netProfit
                FinancialStatus.Red(remaining = remaining)
            }
        }
    }
}
