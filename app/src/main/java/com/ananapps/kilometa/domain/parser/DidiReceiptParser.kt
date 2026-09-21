// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/parser/DidiReceiptParser.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Analizador heurístico especializado para pantallazos de la aplicación DiDi Conductor.
// Extrae de forma determinista la tarifa, ganancias netas, pagos en efectivo,
// pagos con tarjeta, subsidios/cupones de DiDi, distancia y fecha/hora (Orden #044).
// ==============================================================================

package com.ananapps.kilometa.domain.parser

import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.ScannedTripReceipt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

object DidiReceiptParser {

    fun canParse(rawText: String): Boolean {
        val lower = rawText.lowercase(Locale.ROOT)
        return lower.contains("tus solicitudes") ||
                lower.contains("recibido por didi") ||
                lower.contains("pagado por el arrendatario") ||
                lower.contains("didi confort") ||
                (lower.contains("tus ganancias") && (lower.contains("más rápido") || lower.contains("mas rapido")))
    }

    fun parse(rawText: String): ScannedTripReceipt? {
        if (!canParse(rawText)) return null

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        // 1. Ganancia Neta del Conductor
        val netEarnings = extractNetEarnings(rawText, lines) ?: 0.0

        // 2. Método de Pago (Efectivo vs Tarjeta)
        val isCashTrip = rawText.contains("en efectivo", ignoreCase = true) ||
                rawText.contains("efectivo recibido", ignoreCase = true)

        val cashReceived = extractCashReceived(rawText)
        val paidByRenter = extractPaidByRenter(rawText)

        val effectiveGross: Double
        val effectiveNet = netEarnings
        val effectiveCommission: Double
        val paymentMethod: PaymentMethod
        val paymentDestination: PaymentDestination
        val confidenceSummary: String

        if (isCashTrip) {
            paymentMethod = PaymentMethod.CASH
            paymentDestination = PaymentDestination.CASH_IN_HAND
            val cashInHand = cashReceived ?: paidByRenter ?: netEarnings
            effectiveGross = cashInHand

            if (cashInHand >= netEarnings) {
                effectiveCommission = (cashInHand - netEarnings).coerceAtLeast(0.0)
                confidenceSummary = "DiDi · Efectivo cobrado en mano (-$${effectiveCommission.toInt()} comisión)"
            } else {
                // Caso Cupón / Subsidio de DiDi (Imagen 5): Pasajero pagó menos y DiDi subsidia
                val subsidy = netEarnings - cashInHand
                effectiveCommission = 0.0
                confidenceSummary = "DiDi · Efectivo con cupón (+$${subsidy.toInt()} abonado por DiDi)"
            }
        } else {
            paymentMethod = PaymentMethod.APP_WALLET
            paymentDestination = PaymentDestination.APP_RETAINED
            val renterTotal = paidByRenter ?: netEarnings
            effectiveGross = renterTotal
            effectiveCommission = (renterTotal - netEarnings).coerceAtLeast(0.0)
            confidenceSummary = "DiDi · Pago Digital con tarjeta (Directo a Billetera)"
        }

        // 3. Extracción de Distancia en km
        val distanceKm = extractDistance(rawText)

        // 4. Extracción de Duración
        val durationText = extractDuration(rawText)

        // 5. Extracción de Fecha y Hora
        val timestamp = extractTimestamp(rawText)

        // 6. Pasajero
        val passengerName = extractPassenger(lines)

        return ScannedTripReceipt(
            platform = PlatformType.DIDI,
            grossAmount = effectiveGross,
            netAmount = effectiveNet,
            commissionAmount = effectiveCommission,
            paymentMethod = paymentMethod,
            paymentDestination = paymentDestination,
            timestamp = timestamp,
            distanceKm = distanceKm,
            durationText = durationText,
            passengerName = passengerName,
            confidenceSummary = confidenceSummary,
            rawText = rawText
        )
    }

    private fun extractNetEarnings(rawText: String, lines: List<String>): Double? {
        // En DiDi el titular superior dice: "Tus ganancias \n $6.020"
        for (i in lines.indices) {
            if (lines[i].equals("tus ganancias", ignoreCase = true) && i + 1 < lines.size) {
                val next = lines[i + 1]
                val amount = cleanNumber(next)
                if (amount != null && amount > 0.0) return amount
            }
        }

        val pattern = Pattern.compile("tus ganancias[:\\s]*\\$?([0-9.,]+)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            val amount = cleanNumber(matcher.group(1))
            if (amount != null && amount > 0.0) return amount
        }

        return null
    }

    private fun extractCashReceived(rawText: String): Double? {
        val pattern = Pattern.compile("efectivo recibido[:\\s]*\\$?([0-9.,]+)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            return cleanNumber(matcher.group(1))
        }
        return null
    }

    private fun extractPaidByRenter(rawText: String): Double? {
        // Busca "Total \n $6.700" o "Total \n $24.000" bajo "Pagado por el arrendatario"
        val pattern = Pattern.compile("pagado por el arrendatario[\\s\\S]*?total[:\\s]*\\$?([0-9.,]+)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            return cleanNumber(matcher.group(1))
        }

        val simplePattern = Pattern.compile("se pagó por la solic\\.[:\\s]*\\$?([0-9.,]+)", Pattern.CASE_INSENSITIVE)
        val simpleMatcher = simplePattern.matcher(rawText)
        if (simpleMatcher.find()) {
            return cleanNumber(simpleMatcher.group(1))
        }

        return null
    }

    private fun extractDistance(rawText: String): Double? {
        // Ej: "Distancia \n 1,4 km" o "10,1 km" o "12,3 km"
        val pattern = Pattern.compile("distancia[:\\s]*([0-9]+[.,][0-9]+)\\s*km", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            val rawDist = matcher.group(1)?.replace(",", ".")
            return rawDist?.toDoubleOrNull()
        }
        return null
    }

    private fun extractDuration(rawText: String): String? {
        // Ej: "5m24s", "28m50s", "30m55s"
        val pattern = Pattern.compile("duración[:\\s]*([0-9]+m\\s*[0-9]+s|[0-9]+\\s*min)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            return matcher.group(1)?.trim()
        }
        return null
    }

    private fun extractTimestamp(rawText: String): Long {
        // Ej: "21/09/2026, 12:32:56 pm" o "21/09/2026, 1:34:09 pm" o "21/09/2026, 2:41:10 pm"
        val pattern = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{4}),?\\s*(\\d{1,2}):(\\d{2})(?::(\\d{2}))?\\s*(am|pm|a\\.m\\.|p\\.m\\.)?", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            val day = matcher.group(1)?.toIntOrNull() ?: 1
            val month = (matcher.group(2)?.toIntOrNull() ?: 1) - 1
            val year = matcher.group(3)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
            var hour = matcher.group(4)?.toIntOrNull() ?: 0
            val minute = matcher.group(5)?.toIntOrNull() ?: 0
            val second = matcher.group(6)?.toIntOrNull() ?: 0
            val ampm = matcher.group(7)?.lowercase(Locale.ROOT)

            if (ampm != null && (ampm.contains("pm") || ampm.contains("p.m.")) && hour < 12) {
                hour += 12
            } else if (ampm != null && (ampm.contains("am") || ampm.contains("a.m.")) && hour == 12) {
                hour = 0
            }

            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, second)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }
        return System.currentTimeMillis()
    }

    private fun extractPassenger(lines: List<String>): String? {
        val detailsIndex = lines.indexOfFirst { it.equals("detalles", ignoreCase = true) }
        if (detailsIndex in 0 until lines.size - 1) {
            val candidate = lines[detailsIndex + 1]
            if (!candidate.contains("/") && !candidate.contains(":") && candidate.length < 30) {
                return candidate
            }
        }
        return null
    }

    private fun cleanNumber(str: String?): Double? {
        if (str.isNullOrBlank()) return null
        val clean = str.replace("$", "").replace("COP", "").replace("cop", "").trim()
        val digitsOnly = clean.filter { it.isDigit() }
        return digitsOnly.toDoubleOrNull()
    }
}
