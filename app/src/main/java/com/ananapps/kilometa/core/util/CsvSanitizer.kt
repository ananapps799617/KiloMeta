// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/core/util/CsvSanitizer.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Utilidad de seguridad y formato contable para la generación de reportes CSV.
// Implementa mitigación exhaustiva de inyección de fórmulas CSV (CWE-1236)
// y escape estricto según la especificación estándar RFC 4180.
// ==============================================================================

package com.ananapps.kilometa.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: OBJETO DE SANITIZACIÓN Y ESCAPE CONTABLE RFC 4180
// ------------------------------------------------------------------------------
/**
 * Utilitario maestro para la depuración de cadenas de texto y valores financieros
 * antes de su incorporación en archivos tabulares CSV y resúmenes compartibles.
 */
object CsvSanitizer {

    /**
     * Prefijos de riesgo catalogados bajo CWE-1236 (CSV/Formula Injection).
     * Si una celda comienza con alguno de estos caracteres, Excel o Calc podrían interpretarla
     * como fórmula ejecutable o comando DDE.
     */
    private val INJECTION_PREFIXES: CharArray = charArrayOf('=', '+', '-', '@', '\t', '\r')

    /**
     * Sanitiza un campo de texto individual para su inserción segura en un archivo CSV.
     *
     * 1. Defensa CWE-1236: Si el valor inicia con '=', '+', '-', '@', '\t' o '\r',
     *    se le antepone un apóstrofe (') para forzar su interpretación como texto puro.
     * 2. Conformidad RFC 4180: Si la cadena resultante contiene ';', '"', '\n' o '\r',
     *    se duplican las comillas dobles internas ("") y se envuelve el campo completo
     *    entre comillas dobles ("...").
     *
     * @param value Texto bruto a incluir en la celda CSV.
     * @return Texto sanitizado y adecuadamente delimitado para el motor de hojas de cálculo.
     */
    fun sanitizeCsvField(value: String): String {
        if (value.isEmpty()) {
            return ""
        }

        // Paso 1: Mitigación de inyección de fórmulas (CWE-1236)
        val formulaProtected = if (INJECTION_PREFIXES.any { value.startsWith(it) }) {
            "'$value"
        } else {
            value
        }

        // Paso 2: Escape RFC 4180 si contiene delimitador, comillas o saltos de línea
        val needsQuoting = formulaProtected.contains(';') ||
                formulaProtected.contains('"') ||
                formulaProtected.contains('\n') ||
                formulaProtected.contains('\r')

        return if (needsQuoting) {
            val escapedQuotes = formulaProtected.replace("\"", "\"\"")
            "\"$escapedQuotes\""
        } else {
            formulaProtected
        }
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: FORMATEO MONETARIO CONTABLE EN PESOS COLOMBIANOS (COP)
    // --------------------------------------------------------------------------
    /**
     * Convierte un valor de punto flotante en una representación monetaria COP amigable,
     * utilizando el punto '.' como separador de miles y sin fracciones decimales.
     *
     * Ejemplos:
     * - 120000.0 -> "$ 120.000"
     * - -15000.0 -> "-$ 15.000"
     * - 0.0 -> "$ 0"
     *
     * @param amount Monto financiero a formatear.
     * @return Cadena formateada para balances contables legibles en cabina.
     */
    fun formatCurrency(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale("es", "CO")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val formatter = DecimalFormat("#,##0", symbols)

        return if (amount < 0.0) {
            "-$ ${formatter.format(-amount)}"
        } else {
            "$ ${formatter.format(amount)}"
        }
    }
}
