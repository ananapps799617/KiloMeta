// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/GetPlatformAnalyticsUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso de Dominio para la agregación analítica de servicios por plataforma tecnológica.
// Calcula ingreso bruto, comisiones retenidas, neto percibido, margen % y ticket promedio.
// Implementa guardas matemáticas estrictas contra división por cero.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.PlatformAnalytics
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.Trip
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE ANALÍTICAS POR PLATAFORMA
// ------------------------------------------------------------------------------
/**
 * Agrupa y totaliza el rendimiento contable por cada plataforma de transporte intermediaria.
 */
class GetPlatformAnalyticsUseCase @Inject constructor() {

    /**
     * Procesa una lista de carreras y retorna el desglose analítico para cada plataforma.
     *
     * @param trips Lista inmutable de servicios o carreras registradas en el período.
     * @return Lista de [PlatformAnalytics] ordenada de mayor a menor ingreso bruto facturado.
     */
    operator fun invoke(trips: List<Trip>): List<PlatformAnalytics> {
        val totalGrossGlobal = trips.sumOf { it.grossAmount }

        return PlatformType.entries.map { platform ->
            val platformTrips = trips.filter { it.platformType == platform }
            val count = platformTrips.size
            val gross = platformTrips.sumOf { it.grossAmount }
            val commission = platformTrips.sumOf { it.commissionAmount }
            val net = platformTrips.sumOf { it.netAmount }

            val avgTicket = if (count > 0) {
                gross / count
            } else {
                0.0
            }

            val netMargin = if (gross > 0.0) {
                (net / gross) * 100.0
            } else {
                0.0
            }

            val revenueShare = if (totalGrossGlobal > 0.0) {
                (gross / totalGrossGlobal) * 100.0
            } else {
                0.0
            }

            PlatformAnalytics(
                platformType = platform,
                grossIncome = gross,
                commissionPaid = commission,
                netIncome = net,
                tripsCount = count,
                averageTicket = avgTicket,
                netMarginPercentage = netMargin,
                revenueSharePercentage = revenueShare
            )
        }.sortedByDescending { it.grossIncome }
    }
}
