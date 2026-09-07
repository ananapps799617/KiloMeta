// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/FinancialStatus.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio que representa el estado del Semáforo Financiero del conductor.
// Estructurado mediante sealed class inmutable con tipado estricto para garantizar
// exhaustividad ('when' exhaustivo en Compose sin ramas else).
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: JERARQUÍA SELLADA DEL ESTADO FINANCIERO
// ------------------------------------------------------------------------------
/**
 * Estados posibles del semáforo financiero en cabina:
 * - [Red]: El conductor está en números rojos (no cubre combustible ni prorrateo de cuota).
 * - [Amber]: Ha cubierto el combustible y se encuentra en ruta a saldar la cuota.
 * - [Green]: Cuota y combustible cubiertos; cada kilómetro adicional es ganancia neta.
 */
sealed class FinancialStatus(
    val title: String,
    val subtitle: String
) {
    /**
     * Estado Rojo: Alerta de déficit operativo.
     */
    data object Red : FinancialStatus(
        title = "EN DÉFICIT",
        subtitle = "Ingresos insuficientes para costos operativos y cuota."
    )

    /**
     * Estado Ámbar: Punto de equilibrio parcial alcanzado (combustible cubierto).
     */
    data object Amber : FinancialStatus(
        title = "EN RUTA A META",
        subtitle = "Combustible cubierto. En progreso para cubrir la cuota semanal."
    )

    /**
     * Estado Verde: Meta lograda; generación de utilidad real.
     */
    data object Green : FinancialStatus(
        title = "META CUMPLIDA",
        subtitle = "Cuota y combustible cubiertos. Ganancia neta para el hogar."
    )
}
