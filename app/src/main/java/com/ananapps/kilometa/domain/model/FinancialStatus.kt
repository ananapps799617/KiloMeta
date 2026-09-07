// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/FinancialStatus.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio que representa el estado del Semáforo Financiero del conductor.
// Estructurado mediante sealed class inmutable con tipado estricto para garantizar
// exhaustividad ('when' exhaustivo en Compose sin ramas else).
// Provee montos exactos faltantes para alcanzar el Turno Libre o excedentes de ganancia.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: JERARQUÍA SELLADA DEL ESTADO FINANCIERO
// ------------------------------------------------------------------------------
/**
 * Estados posibles del semáforo financiero en cabina:
 * - [Red]: El conductor está en números rojos o cubriendo costos (ingreso neto < 85% de la cuota).
 * - [Amber]: Cerca de la meta (ingreso neto >= 85% y < 100% de la cuota diaria prorrateada).
 * - [Green]: Cuota y combustible cubiertos (Turno Libre); cada carrera adicional es utilidad pura.
 *
 * @property title Titular sintético de alta visibilidad para cabina.
 * @property subtitle Explicación pedagógica de la condición financiera actual.
 * @property remainingAmount Monto en COP pendiente para alcanzar la cuota fijada (0.0 si cumplida).
 * @property surplusAmount Utilidad neta de bolsillo acumulada por encima de la cuota (COP).
 */
sealed class FinancialStatus(
    val title: String,
    val subtitle: String,
    val remainingAmount: Double = 0.0,
    val surplusAmount: Double = 0.0
) {
    /**
     * Estado Rojo: Déficit operativo o etapa inicial de cobertura de costos y cuota.
     */
    open class Red(remaining: Double = 0.0) : FinancialStatus(
        title = "EN DÉFICIT",
        subtitle = "Ingresos insuficientes para costos operativos y cuota.",
        remainingAmount = remaining,
        surplusAmount = 0.0
    ) {
        companion object : Red(0.0)

        override fun equals(other: Any?): Boolean =
            other is Red && other.remainingAmount == this.remainingAmount

        override fun hashCode(): Int = remainingAmount.hashCode()

        override fun toString(): String = "FinancialStatus.Red(remaining=$remainingAmount)"
    }

    /**
     * Estado Ámbar: Cerca de la meta (>= 85% de la cuota diaria). Zona de aceleración.
     */
    open class Amber(remaining: Double = 0.0) : FinancialStatus(
        title = "EN RUTA A META",
        subtitle = "Combustible cubierto. En progreso para cubrir la cuota semanal.",
        remainingAmount = remaining,
        surplusAmount = 0.0
    ) {
        companion object : Amber(0.0)

        override fun equals(other: Any?): Boolean =
            other is Amber && other.remainingAmount == this.remainingAmount

        override fun hashCode(): Int = remainingAmount.hashCode()

        override fun toString(): String = "FinancialStatus.Amber(remaining=$remainingAmount)"
    }

    /**
     * Estado Verde: Meta lograda; Turno Libre y generación de utilidad real de bolsillo.
     */
    open class Green(surplus: Double = 0.0) : FinancialStatus(
        title = "META CUMPLIDA",
        subtitle = "Cuota y combustible cubiertos. Ganancia neta para el hogar.",
        remainingAmount = 0.0,
        surplusAmount = surplus
    ) {
        companion object : Green(0.0)

        override fun equals(other: Any?): Boolean =
            other is Green && other.surplusAmount == this.surplusAmount

        override fun hashCode(): Int = surplusAmount.hashCode()

        override fun toString(): String = "FinancialStatus.Green(surplus=$surplusAmount)"
    }
}
