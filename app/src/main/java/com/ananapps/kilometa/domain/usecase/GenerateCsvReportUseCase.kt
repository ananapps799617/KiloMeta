// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/GenerateCsvReportUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso de Dominio que genera un reporte financiero multi-tabla en formato CSV.
// Estructurado en 4 secciones contables con delimitador ';', BOM UTF-8 (\uFEFF)
// y sanitización estricta anti-inyección CWE-1236 y escape RFC 4180.
// Regla arquitectural: Cero dependencias del framework Android en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.core.util.CsvSanitizer
import com.ananapps.kilometa.domain.model.CsvReport
import com.ananapps.kilometa.domain.model.HistoryFilterPeriod
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE EXPORTACIÓN TABULAR CONTABLE
// ------------------------------------------------------------------------------
/**
 * Compila todas las tablas del turno en un único documento CSV estructurado
 * compatible con Microsoft Excel, Google Sheets y software contable.
 */
class GenerateCsvReportUseCase @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val expenseRepository: ExpenseRepository
) {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.of("America/Bogota"))

    /**
     * Construye el reporte CSV consolidando los turnos dados y sus carreras,
     * recargas de combustible y gastos operativos correspondientes.
     *
     * @param periodFilter Filtro temporal aplicado (Semana, Mes o Todo).
     * @param shifts Lista de resúmenes de turnos a incluir en el reporte.
     * @return [CsvReport] con el contenido codificado en UTF-8 BOM y metadatos de exportación.
     */
    suspend operator fun invoke(
        periodFilter: HistoryFilterPeriod,
        shifts: List<ShiftFinancialSummary>
    ): CsvReport {
        val shiftIds: Set<String> = shifts.map { it.shiftId }.toSet()

        val allTrips = shiftRepository.getAllTrips().first()
            .filter { it.shiftId in shiftIds }
            .sortedBy { it.timestamp }

        val allFuelings = shiftRepository.getAllFuelings().first()
            .filter { it.shiftId in shiftIds }
            .sortedBy { it.timestamp }

        val allExpenses = expenseRepository.getAllExpenses().first()
            .filter { it.shiftId in shiftIds }
            .sortedBy { it.timestamp }

        val builder = StringBuilder()

        // Encabezado estándar UTF-8 BOM para apertura directa en Microsoft Excel
        builder.append('\uFEFF')

        // ----------------------------------------------------------------------
        // SECCION 1: RESUMEN DE JORNADAS
        // ----------------------------------------------------------------------
        builder.append("### SECCION 1: RESUMEN DE JORNADAS ###\r\n")
        builder.append(
            "ID_TURNO;ESTADO;FECHA_INICIO;FECHA_FIN;ODO_INICIAL;ODO_FINAL;KM_TOTALES;" +
                    "META_CUOTA;ING_BRUTO;COMISIONES;ING_NETO;GASTO_GNV;GASTO_GASOLINA;" +
                    "TOTAL_COMBUSTIBLE;GASTOS_OPERATIVOS;BENEFICIO_NETO;CUMPLIMIENTO_PCT;COSTO_POR_KM\r\n"
        )
        for (shift in shifts) {
            val formattedStart = formatTimestamp(shift.startTime)
            val formattedEnd = shift.endTime?.let { formatTimestamp(it) } ?: "-"
            val row = listOf(
                CsvSanitizer.sanitizeCsvField(shift.shiftId),
                CsvSanitizer.sanitizeCsvField(shift.status.name),
                CsvSanitizer.sanitizeCsvField(formattedStart),
                CsvSanitizer.sanitizeCsvField(formattedEnd),
                shift.startOdometer.toString(),
                shift.endOdometer?.toString() ?: "-",
                shift.totalKilometers.toString(),
                formatDecimal(shift.dailyQuotaTarget),
                formatDecimal(shift.totalGrossIncome),
                formatDecimal(shift.totalCommissions),
                formatDecimal(shift.totalNetIncome),
                formatDecimal(shift.totalGasExpense),
                formatDecimal(shift.totalGasolineExpense),
                formatDecimal(shift.totalFuelExpense),
                formatDecimal(shift.totalOperationalExpenses),
                formatDecimal(shift.netProfit),
                String.format(Locale.US, "%.1f", shift.quotaProgressPercentage),
                String.format(Locale.US, "%.2f", shift.costPerKilometer)
            ).joinToString(";")
            builder.append(row).append("\r\n")
        }
        builder.append("\r\n")

        // ----------------------------------------------------------------------
        // SECCION 2: DETALLE DE CARRERAS
        // ----------------------------------------------------------------------
        builder.append("### SECCION 2: DETALLE DE CARRERAS ###\r\n")
        builder.append(
            "ID_CARRERA;ID_TURNO;FECHA_HORA;PLATAFORMA;VALOR_BRUTO;COMISION;" +
                    "VALOR_NETO;PCT_COMISION;MEDIO_PAGO\r\n"
        )
        for (trip in allTrips) {
            val formattedDate = formatTimestamp(trip.timestamp)
            val row = listOf(
                CsvSanitizer.sanitizeCsvField(trip.id),
                CsvSanitizer.sanitizeCsvField(trip.shiftId),
                CsvSanitizer.sanitizeCsvField(formattedDate),
                CsvSanitizer.sanitizeCsvField(trip.platformType.displayName),
                formatDecimal(trip.grossAmount),
                formatDecimal(trip.commissionAmount),
                formatDecimal(trip.netAmount),
                String.format(Locale.US, "%.1f", trip.commissionRatePercentage),
                CsvSanitizer.sanitizeCsvField(trip.paymentMethod.displayName)
            ).joinToString(";")
            builder.append(row).append("\r\n")
        }
        builder.append("\r\n")

        // ----------------------------------------------------------------------
        // SECCION 3: ABASTECIMIENTO DE COMBUSTIBLE
        // ----------------------------------------------------------------------
        builder.append("### SECCION 3: ABASTECIMIENTO DE COMBUSTIBLE ###\r\n")
        builder.append(
            "ID_TANQUEO;ID_TURNO;FECHA_HORA;TIPO_COMBUSTIBLE;MONTO_COP;VOLUMEN;" +
                    "ODOMETRO;PRECIO_UNITARIO\r\n"
        )
        for (fueling in allFuelings) {
            val formattedDate = formatTimestamp(fueling.timestamp)
            val row = listOf(
                CsvSanitizer.sanitizeCsvField(fueling.id),
                CsvSanitizer.sanitizeCsvField(fueling.shiftId),
                CsvSanitizer.sanitizeCsvField(formattedDate),
                CsvSanitizer.sanitizeCsvField(fueling.fuelType.displayName),
                formatDecimal(fueling.amountPaid),
                String.format(Locale.US, "%.2f", fueling.volume),
                fueling.odometer.toString(),
                String.format(Locale.US, "%.2f", fueling.unitPrice)
            ).joinToString(";")
            builder.append(row).append("\r\n")
        }
        builder.append("\r\n")

        // ----------------------------------------------------------------------
        // SECCION 4: GASTOS OPERATIVOS EN RUTA
        // ----------------------------------------------------------------------
        builder.append("### SECCION 4: GASTOS OPERATIVOS EN RUTA ###\r\n")
        builder.append("ID_GASTO;ID_TURNO;FECHA_HORA;CONCEPTO;MONTO_COP\r\n")
        for (expense in allExpenses) {
            val formattedDate = formatTimestamp(expense.timestamp)
            val row = listOf(
                CsvSanitizer.sanitizeCsvField(expense.id),
                CsvSanitizer.sanitizeCsvField(expense.shiftId),
                CsvSanitizer.sanitizeCsvField(formattedDate),
                CsvSanitizer.sanitizeCsvField(expense.concept.displayName),
                formatDecimal(expense.amount)
            ).joinToString(";")
            builder.append(row).append("\r\n")
        }

        val totalDataRows = shifts.size + allTrips.size + allFuelings.size + allExpenses.size
        val generatedAtMillis = System.currentTimeMillis()
        val filterName = periodFilter.name.lowercase(Locale.ROOT)
        val fileName = "kilometa_reporte_${filterName}_$generatedAtMillis.csv"

        return CsvReport(
            fileName = fileName,
            content = builder.toString(),
            totalRows = totalDataRows,
            generatedAtMillis = generatedAtMillis
        )
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: FORMATEADORES PRIVADOS DE SOPORTE
    // --------------------------------------------------------------------------
    private fun formatTimestamp(epochMillis: Long): String {
        return dateFormatter.format(Instant.ofEpochMilli(epochMillis))
    }

    private fun formatDecimal(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }
}
