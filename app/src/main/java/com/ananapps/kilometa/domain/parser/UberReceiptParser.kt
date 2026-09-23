// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/parser/UberReceiptParser.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Analizador heurístico especializado para pantallazos de la aplicación Uber Driver.
// Extrae de forma determinista la tarifa, ganancias netas ajustadas con incrementos, pagos en efectivo,
// comisiones retenidas, distancia en kilómetros y duración en minutos (Orden #044).
// ==============================================================================

package com.ananapps.kilometa.domain.parser

import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.ScannedTripReceipt
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

object UberReceiptParser {

    fun canParse(rawText: String): Boolean {
        val lower = rawText.lowercase(Locale.ROOT)
        return (lower.contains("detalles del viaje") || lower.contains("drivers.uber.com")) ||
                (lower.contains("tarifa por adelantado") && lower.contains("cop")) ||
                (lower.contains("tus ganancias") && lower.contains("cop") && !lower.contains("didi"))
    }

    fun parse(rawText: String): ScannedTripReceipt? {
        if (!canParse(rawText)) return null

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        // 1. Detección de Efectivo vs Tarjeta
        val isCashTrip = rawText.contains("monto recibido en efectivo", ignoreCase = true) ||
                rawText.contains("recibido en efectivo", ignoreCase = true)

        val cashAmount = extractCashAmount(rawText)
        val netEarnings = extractEarnings(rawText, lines)

        val effectiveNet: Double
        val effectiveGross: Double
        val effectiveCommission: Double
        val paymentMethod: PaymentMethod
        val paymentDestination: PaymentDestination

        if (isCashTrip && cashAmount != null && cashAmount > 0.0) {
            paymentMethod = PaymentMethod.CASH
            paymentDestination = PaymentDestination.CASH_IN_HAND
            effectiveGross = cashAmount
            effectiveNet = if (netEarnings != null && netEarnings > 0.0) netEarnings else cashAmount
            effectiveCommission = (effectiveGross - effectiveNet).coerceAtLeast(0.0)
        } else {
            paymentMethod = PaymentMethod.APP_WALLET
            paymentDestination = PaymentDestination.APP_RETAINED
            effectiveNet = netEarnings ?: (cashAmount ?: 0.0)
            effectiveGross = effectiveNet
            effectiveCommission = 0.0
        }

        // 2. Extracción de Distancia en km
        val distanceKm = extractDistance(rawText)

        // 3. Extracción de Duración y Minutos
        val durationText = extractDuration(rawText)
        val durationMinutes = extractDurationMinutes(durationText)

        // 4. Extracción de Fecha y Hora
        val timestamp = extractTimestamp(rawText)

        return ScannedTripReceipt(
            platform = PlatformType.UBER,
            grossAmount = effectiveGross,
            netAmount = effectiveNet,
            commissionAmount = effectiveCommission,
            paymentMethod = paymentMethod,
            paymentDestination = paymentDestination,
            timestamp = timestamp,
            distanceKm = distanceKm,
            durationText = durationText,
            durationMinutes = durationMinutes,
            confidenceSummary = if (isCashTrip) "Uber · Efectivo en mano con descuento" else "Uber · Pago Digital con tarjeta",
            rawText = rawText
        )
    }

    private fun extractCashAmount(rawText: String): Double? {
        val pattern = Pattern.compile("monto recibido en efectivo[:\\s]*([0-9.,]+)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            return cleanNumber(matcher.group(1))
        }
        return null
    }

    private fun extractEarnings(rawText: String, lines: List<String>): Double? {
        // Prioridad 1: Buscar "Tus ganancias: 8.493 COP" o línea bajo "Tus ganancias"
        val earningsPattern = Pattern.compile("tus ganancias[:\\s]*([0-9.,]+)\\s*COP", Pattern.CASE_INSENSITIVE)
        val earningsMatcher = earningsPattern.matcher(rawText)
        if (earningsMatcher.find()) {
            val amount = cleanNumber(earningsMatcher.group(1))
            if (amount != null && amount > 0.0) return amount
        }

        for (i in lines.indices) {
            if (lines[i].equals("tus ganancias", ignoreCase = true) && i + 1 < lines.size) {
                val next = lines[i + 1]
                val amount = cleanNumber(next)
                if (amount != null && amount > 0.0) return amount
            }
        }

        // Prioridad 2: Cabecera superior con monto real (ej: "8.493 COP >")
        for (i in 0 until minOf(lines.size, 8)) {
            val line = lines[i]
            val copMatcher = Pattern.compile("([0-9]{1,3}(?:[.,][0-9]{3})+)\\s*COP", Pattern.CASE_INSENSITIVE).matcher(line)
            if (copMatcher.find()) {
                val amount = cleanNumber(copMatcher.group(1))
                if (amount != null && amount > 1000.0) return amount
            }
        }

        // Prioridad 3: Buscar "Tu pago: 8.493 COP"
        val paymentPattern = Pattern.compile("tu pago[:\\s]*([0-9.,]+)\\s*COP", Pattern.CASE_INSENSITIVE)
        val paymentMatcher = paymentPattern.matcher(rawText)
        if (paymentMatcher.find()) {
            val amount = cleanNumber(paymentMatcher.group(1))
            if (amount != null && amount > 0.0) return amount
        }

        // Prioridad 4 (Fallback base): Buscar "Tarifa por adelantado: 8.475 COP"
        val upfrontPattern = Pattern.compile("tarifa por adelantado[:\\s]*([0-9.,]+)", Pattern.CASE_INSENSITIVE)
        val upfrontMatcher = upfrontPattern.matcher(rawText)
        if (upfrontMatcher.find()) {
            val amount = cleanNumber(upfrontMatcher.group(1))
            if (amount != null && amount > 0.0) return amount
        }

        return null
    }

    private fun extractDistance(rawText: String): Double? {
        val pattern = Pattern.compile("distancia[:\\s]*([0-9]+[.,][0-9]+)\\s*km", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            val rawDist = matcher.group(1)?.replace(",", ".")
            return rawDist?.toDoubleOrNull()
        }
        return null
    }

    private fun extractDuration(rawText: String): String? {
        val pattern = Pattern.compile("duración[:\\s]*([0-9]+\\s*min(?:\\s*[0-9]+\\s*seg)?)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            return matcher.group(1)?.trim()
        }
        return null
    }

    private fun extractDurationMinutes(durationText: String?): Int? {
        if (durationText.isNullOrBlank()) return null
        val pattern = Pattern.compile("([0-9]+)\\s*min", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(durationText)
        if (matcher.find()) {
            return matcher.group(1)?.toIntOrNull()
        }
        return null
    }

    private fun extractTimestamp(rawText: String): Long {
        val pattern = Pattern.compile("(\\d{1,2})\\s+([a-zA-Z]{3,4})\\s+(\\d{4})\\s*·?\\s*(\\d{1,2}):(\\d{2})", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            val day = matcher.group(1)?.toIntOrNull() ?: 1
            val monthStr = matcher.group(2)?.lowercase(Locale.ROOT) ?: "ene"
            val year = matcher.group(3)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
            val hour = matcher.group(4)?.toIntOrNull() ?: 0
            val minute = matcher.group(5)?.toIntOrNull() ?: 0

            val monthIndex = parseMonth(monthStr)
            return Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, monthIndex)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        return System.currentTimeMillis()
    }

    private fun parseMonth(monthStr: String): Int = when {
        monthStr.startsWith("ene") -> Calendar.JANUARY
        monthStr.startsWith("feb") -> Calendar.FEBRUARY
        monthStr.startsWith("mar") -> Calendar.MARCH
        monthStr.startsWith("abr") -> Calendar.APRIL
        monthStr.startsWith("may") -> Calendar.MAY
        monthStr.startsWith("jun") -> Calendar.JUNE
        monthStr.startsWith("jul") -> Calendar.JULY
        monthStr.startsWith("ago") -> Calendar.AUGUST
        monthStr.startsWith("sep") -> Calendar.SEPTEMBER
        monthStr.startsWith("oct") -> Calendar.OCTOBER
        monthStr.startsWith("nov") -> Calendar.NOVEMBER
        monthStr.startsWith("dic") -> Calendar.DECEMBER
        else -> Calendar.JANUARY
    }

    private fun cleanNumber(str: String?): Double? {
        if (str.isNullOrBlank()) return null
        val clean = str.replace("$", "").replace("COP", "").replace("cop", "").trim()
        val digitsOnly = clean.filter { it.isDigit() }
        return digitsOnly.toDoubleOrNull()
    }
}
