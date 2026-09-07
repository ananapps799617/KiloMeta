// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/PlatformAnalytics.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio que representa las métricas analíticas e indicadores contables
// segregados por plataforma de transporte (Uber, DiDi, InDrive, Particular).
// Permite al conductor auditar la rentabilidad neta real y comisiones de intermediación.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO INMUTABLE DE RENDIMIENTO POR PLATAFORMA
// ------------------------------------------------------------------------------
/**
 * Agregado analítico de carreras y comisiones segregado por plataforma.
 *
 * @property platformType Plataforma tecnológica o servicio particular.
 * @property grossIncome Total recaudado bruto facturado al cliente en COP.
 * @property commissionPaid Comisiones retenidas por la plataforma intermediaria en COP.
 * @property netIncome Ingreso neto real liquidado al conductor en COP (grossIncome - commissionPaid).
 * @property tripsCount Cantidad total de carreras completadas en la plataforma.
 * @property averageTicket Tarifa promedio cobrada por carrera completada en COP.
 * @property netMarginPercentage Porcentaje neto que conserva el conductor sobre el bruto.
 * @property revenueSharePercentage Participación porcentual de esta app sobre la facturación global.
 */
data class PlatformAnalytics(
    val platformType: PlatformType,
    val grossIncome: Double,
    val commissionPaid: Double,
    val netIncome: Double,
    val tripsCount: Int,
    val averageTicket: Double,
    val netMarginPercentage: Double,
    val revenueSharePercentage: Double
) {
    // --------------------------------------------------------------------------
    // BLOQUE 2: PROPIEDADES DERIVADAS DE CONTROL
    // --------------------------------------------------------------------------
    /**
     * Tasa de retención porcentual efectiva cobrada por la plataforma intermediaria.
     */
    val commissionRatePercentage: Double
        get() = if (grossIncome > 0.0) {
            (commissionPaid / grossIncome) * 100.0
        } else {
            0.0
        }
}
