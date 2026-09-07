// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/GenerateShareableSummaryUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso de Dominio que genera resúmenes contables enriquecidos con emojis
// optimizados para difusión instantánea por WhatsApp y Telegram.
// Incluye desglose bi-combustible (GNV vs Gasolina), ahorro auditado y cumplimiento.
// Regla arquitectural: Cero dependencias del framework Android en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.core.util.CsvSanitizer
import com.ananapps.kilometa.domain.model.HistoryFilterPeriod
import com.ananapps.kilometa.domain.model.ShareableSummary
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE GENERACIÓN DE RESÚMENES COMPARTIBLES
// ------------------------------------------------------------------------------
/**
 * Transforma métricas financieras en textos estructurados de alta legibilidad
 * para rendición de cuentas ante propietarios del taxi o archivo personal del conductor.
 */
class GenerateShareableSummaryUseCase @Inject constructor(
    private val getBiFuelSavingsUseCase: GetBiFuelSavingsUseCase
) {

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("HH:mm")
        .withZone(ZoneId.of("America/Bogota"))

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("dd/MM/yyyy")
        .withZone(ZoneId.of("America/Bogota"))

    // --------------------------------------------------------------------------
    // BLOQUE 2: LIQUIDACIÓN DE TURNO INDIVIDUAL
    // --------------------------------------------------------------------------
    /**
     * Genera la liquidación financiera de una jornada de trabajo individual.
     *
     * @param summary Resumen contable consolidado del turno.
     * @return [ShareableSummary] con encabezado de asunto y texto con emojis de cabina.
     */
    operator fun invoke(summary: ShiftFinancialSummary): ShareableSummary {
        val startDateStr = dateFormatter.format(Instant.ofEpochMilli(summary.startTime))
        val startTimeStr = timeFormatter.format(Instant.ofEpochMilli(summary.startTime))
        val endTimeStr = summary.endTime?.let { timeFormatter.format(Instant.ofEpochMilli(it)) } ?: "En curso"

        val savings = getBiFuelSavingsUseCase(
            totalKmDriven = summary.totalKilometers,
            totalGasExpense = summary.totalGasExpense,
            totalGasolineExpense = summary.totalGasolineExpense
        )

        val odoEndText = summary.endOdometer?.toString() ?: "-"
        val quotaAchievedEmoji = if (summary.isQuotaAchieved) "✅" else "⚠️"

        val message = buildString {
            append("🚕 *KILOMETA - LIQUIDACIÓN DE JORNADA*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("📅 *Fecha:* $startDateStr\n")
            append("⏱️ *Horario:* $startTimeStr - $endTimeStr\n")
            append("🛣️ *Odómetro:* ${summary.startOdometer} ➔ $odoEndText (${summary.totalKilometers} km)\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("💵 *Ingresos Brutos:* ${CsvSanitizer.formatCurrency(summary.totalGrossIncome)} (${summary.tripsCount} carreras)\n")
            append("📉 *Comisiones App:* -${CsvSanitizer.formatCurrency(summary.totalCommissions)}\n")
            append("💰 *Neto en Mano:* ${CsvSanitizer.formatCurrency(summary.totalNetIncome)}\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("⛽ *Combustible Bi-Modal:*\n")
            append("   • GNV (Gas): ${CsvSanitizer.formatCurrency(summary.totalGasExpense)}\n")
            append("   • Gasolina: ${CsvSanitizer.formatCurrency(summary.totalGasolineExpense)}\n")
            append("   • Total Combustible: ${CsvSanitizer.formatCurrency(summary.totalFuelExpense)}\n")
            if (savings.netSavingsAmount > 0.0) {
                append("🌱 *Ahorro con GNV:* ${CsvSanitizer.formatCurrency(savings.netSavingsAmount)} (${String.format(Locale.US, "%.1f", savings.savingsPercentage)}%)\n")
            }
            if (summary.totalOperationalExpenses > 0.0) {
                append("🛠️ *Gastos en Ruta:* -${CsvSanitizer.formatCurrency(summary.totalOperationalExpenses)}\n")
            }
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("✨ *BENEFICIO NETO:* ${CsvSanitizer.formatCurrency(summary.netProfit)}\n")
            append("$quotaAchievedEmoji *Meta Cuota:* ${CsvSanitizer.formatCurrency(summary.dailyQuotaTarget)} (${String.format(Locale.US, "%.1f", summary.quotaProgressPercentage)}%)\n")
            append("📊 *Costo/Km:* ${CsvSanitizer.formatCurrency(summary.costPerKilometer)}/km\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("_Generado con KiloMeta 100% Offline_ 🇨🇴")
        }

        return ShareableSummary(
            subject = "Liquidación KiloMeta - $startDateStr",
            messageText = message
        )
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: REPORTE CONSOLIDADO POR PERÍODO
    // --------------------------------------------------------------------------
    /**
     * Sobrecarga que genera un balance consolidado para un grupo de turnos o ventana temporal.
     *
     * @param periodFilter Ventana de corte seleccionada (Semana, Mes o Todo).
     * @param shifts Lista de resúmenes de turnos incluidos en el período.
     * @return [ShareableSummary] consolidado.
     */
    operator fun invoke(
        periodFilter: HistoryFilterPeriod,
        shifts: List<ShiftFinancialSummary>
    ): ShareableSummary {
        val totalKm = shifts.sumOf { it.totalKilometers }
        val totalGross = shifts.sumOf { it.totalGrossIncome }
        val totalCommissions = shifts.sumOf { it.totalCommissions }
        val totalNet = shifts.sumOf { it.totalNetIncome }
        val totalGas = shifts.sumOf { it.totalGasExpense }
        val totalGasoline = shifts.sumOf { it.totalGasolineExpense }
        val totalFuel = shifts.sumOf { it.totalFuelExpense }
        val totalOps = shifts.sumOf { it.totalOperationalExpenses }
        val totalProfit = shifts.sumOf { it.netProfit }
        val totalTrips = shifts.sumOf { it.tripsCount }

        val savings = getBiFuelSavingsUseCase(
            totalKmDriven = totalKm,
            totalGasExpense = totalGas,
            totalGasolineExpense = totalGasoline
        )

        val avgQuotaProgress = if (shifts.isNotEmpty()) {
            shifts.map { it.quotaProgressPercentage }.average()
        } else {
            0.0
        }

        val message = buildString {
            append("🚕 *KILOMETA - REPORTE CONSOLIDADO*\n")
            append("🗓️ *Período:* ${periodFilter.displayName.uppercase(Locale.ROOT)}\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("📋 *Jornadas Registradas:* ${shifts.size}\n")
            append("🛣️ *Kilómetros Totales:* $totalKm km\n")
            append("🏁 *Carreras Atendidas:* $totalTrips\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("💵 *Ingresos Brutos:* ${CsvSanitizer.formatCurrency(totalGross)}\n")
            append("📉 *Comisiones App:* -${CsvSanitizer.formatCurrency(totalCommissions)}\n")
            append("💰 *Neto Recaudado:* ${CsvSanitizer.formatCurrency(totalNet)}\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("⛽ *Combustible:* ${CsvSanitizer.formatCurrency(totalFuel)}\n")
            append("   • GNV: ${CsvSanitizer.formatCurrency(totalGas)}\n")
            append("   • Gasolina: ${CsvSanitizer.formatCurrency(totalGasoline)}\n")
            if (savings.netSavingsAmount > 0.0) {
                append("🌱 *Ahorro Total GNV:* ${CsvSanitizer.formatCurrency(savings.netSavingsAmount)} (${String.format(Locale.US, "%.1f", savings.savingsPercentage)}%)\n")
            }
            if (totalOps > 0.0) {
                append("🛠️ *Gastos Operativos:* -${CsvSanitizer.formatCurrency(totalOps)}\n")
            }
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("🏆 *BENEFICIO NETO TOTAL:* ${CsvSanitizer.formatCurrency(totalProfit)}\n")
            append("🎯 *Cumplimiento Promedio:* ${String.format(Locale.US, "%.1f", avgQuotaProgress)}%\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("_KiloMeta - Auditoría Financiera Bi-Combustible_ 🇨🇴")
        }

        return ShareableSummary(
            subject = "Reporte KiloMeta - ${periodFilter.displayName}",
            messageText = message
        )
    }
}
