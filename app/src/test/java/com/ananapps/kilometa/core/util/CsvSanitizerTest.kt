// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/core/util/CsvSanitizerTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para la utilidad de sanitización y escape CsvSanitizer.
// Valida exhaustivamente la protección contra inyección de fórmulas (CWE-1236),
// conformidad con el estándar RFC 4180 y formateo contable COP.
// ==============================================================================

package com.ananapps.kilometa.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvSanitizerTest {

    // --------------------------------------------------------------------------
    // BLOQUE 1: PRUEBAS DE MITIGACIÓN DE INYECCIÓN DE FÓRMULAS (CWE-1236)
    // --------------------------------------------------------------------------
    @Test
    fun testSanitizeFormulaInjectionPrefixes() {
        // Prueba de prefijo '='
        val equalsFormula = "=SUM(A1:A10)"
        assertEquals("'=SUM(A1:A10)", CsvSanitizer.sanitizeCsvField(equalsFormula))

        // Prueba de prefijo '+'
        val plusFormula = "+573001234567"
        assertEquals("'+573001234567", CsvSanitizer.sanitizeCsvField(plusFormula))

        // Prueba de prefijo '-'
        val minusValue = "-15000"
        assertEquals("'-15000", CsvSanitizer.sanitizeCsvField(minusValue))

        // Prueba de prefijo '@'
        val atSymbol = "@mention"
        assertEquals("'@mention", CsvSanitizer.sanitizeCsvField(atSymbol))

        // Prueba de tabulación inicial '\t'
        val tabbed = "\tDatosOcultos"
        assertEquals("'\tDatosOcultos", CsvSanitizer.sanitizeCsvField(tabbed))
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: PRUEBAS DE ESCAPE RFC 4180 (DELIMITADORES Y COMILLAS)
    // --------------------------------------------------------------------------
    @Test
    fun testSanitizeRfc4180SpecialCharacters() {
        // Texto con delimitador punto y coma ';'
        val textWithDelimiter = "Carrera;Particular;Aeropuerto"
        assertEquals("\"Carrera;Particular;Aeropuerto\"", CsvSanitizer.sanitizeCsvField(textWithDelimiter))

        // Texto con comillas dobles internas '"'
        val textWithQuotes = "Cliente dijo: \"Excelente servicio\""
        assertEquals("\"Cliente dijo: \"\"Excelente servicio\"\"\"", CsvSanitizer.sanitizeCsvField(textWithQuotes))

        // Texto multilínea con salto de línea '\n'
        val multilineText = "Observación línea 1\nObservación línea 2"
        assertEquals("\"Observación línea 1\nObservación línea 2\"", CsvSanitizer.sanitizeCsvField(multilineText))

        // Combinación de fórmula CWE-1236 y delimitador RFC 4180
        val formulaWithDelimiter = "=cmd|' /C calc'!A0;Inyección"
        assertEquals("\"'=cmd|' /C calc'!A0;Inyección\"", CsvSanitizer.sanitizeCsvField(formulaWithDelimiter))
    }

    @Test
    fun testSanitizeNormalTextAndEmpty() {
        // Texto seguro convencional sin caracteres especiales ni prefijos riesgosos
        val normalText = "Carrera Normal Bogotá Chapinero"
        assertEquals("Carrera Normal Bogotá Chapinero", CsvSanitizer.sanitizeCsvField(normalText))

        // Cadena vacía
        assertEquals("", CsvSanitizer.sanitizeCsvField(""))
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: PRUEBAS DE FORMATEO MONETARIO CONTABLE EN COP
    // --------------------------------------------------------------------------
    @Test
    fun testFormatCurrencyColombianPesos() {
        // Valores enteros positivos
        assertEquals("$ 120.000", CsvSanitizer.formatCurrency(120000.0))
        assertEquals("$ 83.333", CsvSanitizer.formatCurrency(83333.0))
        assertEquals("$ 1.500.000", CsvSanitizer.formatCurrency(1500000.0))

        // Cero
        assertEquals("$ 0", CsvSanitizer.formatCurrency(0.0))

        // Valores negativos (déficit contable)
        assertEquals("-$ 15.000", CsvSanitizer.formatCurrency(-15000.0))
        assertEquals("-$ 500", CsvSanitizer.formatCurrency(-500.0))
    }
}
