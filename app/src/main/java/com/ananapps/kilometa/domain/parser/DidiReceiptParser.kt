// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/parser/DidiReceiptParser.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Analizador heurístico especializado para pantallazos de la aplicación DiDi Conductor.
// Extrae deterministamente ganancias netas, pagos cobrados, subsidios/promociones a favor
// del conductor, comisiones, distancia (km) y duración (minutos) (Orden #044).
// ==============================================================================

package com.ananapps.kilometa.domain.parser

import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.ScannedTripReceipt
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
                lower.contains("tarifa por distancia") ||
                (lower.contains("tus ganancias") && (lower.contains("más rápido") || lower.contains("duración") || lower.contains("duracion")))
    }

    fun parse(rawText: String): ScannedTripReceipt? {
        if (!canParse(rawText)) return null

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        // 1. Ganancia Neta del Conductor
        val netEarnings = extractNetEarnings(rawText, lines) ?: 0.0

        // 2. Cobros y Liquidación (Efectivo vs Billetera vs Subsidio Promo)
        val isCashTrip = rawText.contains("en efectivo", ignoreCase = true) ||
                rawText.contains("efectivo recibido", ignoreCase = true) ||
                rawText.contains("bancolombia", ignoreCase = true)

        val cashReceived = extractCashReceived(rawText)
        val paidByRenter = extractPaidByRenter(rawText)
        val collectedInHand = cashReceived ?: paidByRenter ?: netEarnings

        val effectiveGross: Double
        val effectiveNet = netEarnings
        val effectiveCommission: Double
        val platformPromoDiscount: Double
        val paymentMethod: PaymentMethod
        val paymentDestination: PaymentDestination
        val confidenceSummary: String

        if (isCashTrip) {
            paymentMethod = if (rawText.contains("bancolombia", ignoreCase = true)) PaymentMethod.NEQUI else PaymentMethod.CASH
            paymentDestination = PaymentDestination.CASH_IN_HAND

            if (collectedInHand >= netEarnings) {
                effectiveGross = collectedInHand
                effectiveCommission = (collectedInHand - netEarnings).coerceAtLeast(0.0)
                platformPromoDiscount = 0.0
                confidenceSummary = "DiDi · Cobrado en mano (-$${effectiveCommission.toInt()} comisión app)"
            } else {
                // Caso Promoción / Subsidio DiDi (Daniela): Pasajero pagó menos y DiDi abona la diferencia
                val subsidy = (netEarnings - collectedInHand).coerceAtLeast(0.0)
                effectiveGross = netEarnings
                effectiveCommission = 0.0
                platformPromoDiscount = subsidy
                confidenceSummary = "DiDi · Tarifa con subsidio/promo (+$${subsidy.toInt()} a favor en cartera DiDi)"
            }
        } else {
            paymentMethod = PaymentMethod.APP_WALLET
            paymentDestination = PaymentDestination.APP_RETAINED
            val renterTotal = paidByRenter ?: netEarnings
            effectiveGross = renterTotal
            effectiveCommission = (renterTotal - netEarnings).coerceAtLeast(0.0)
            platformPromoDiscount = 0.0
            confidenceSummary = "DiDi · Pago Digital con tarjeta (Directo a Billetera)"
        }

        // 3. Extracción de Distancia en km
        val distanceKm = extractDistance(rawText, lines)

        // 4. Extracción de Duración y Minutos
        val durationText = extractDuration(rawText, lines)
        val durationMinutes = extractDurationMinutes(durationText)

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
            durationMinutes = durationMinutes,
            passengerName = passengerName,
            platformPromoDiscount = platformPromoDiscount,
            confidenceSummary = confidenceSummary,
            rawText = rawText
        )
    }

    private fun extractNetEarnings(rawText: String, lines: List<String>): Double? {
        // Prioridad 1: Monto grande superior de cabecera bajo "Tus solicitudes" / "Tus ganancias"
        for (i in 0 until minOf(lines.size, 10)) {
            val line = lines[i]
            if (line.matches(Regex("^\\$?([0-9]{1,3}(?:[.,][0-9]{3})+)$"))) {
                val amount = cleanNumber(line)
                if (amount != null && amount > 1000.0) return amount
            }
        }

        // Prioridad 2: Sección "Tus ganancias" -> Total
        val pattern = Pattern.compile("tus ganancias[\\s\\S]*?total[:\\s]*\\$?([0-9.,]+)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            val amount = cleanNumber(matcher.group(1))
            if (amount != null && amount > 0.0) return amount
        }

        // Prioridad 3: Línea directa tras "Tus ganancias"
        for (i in lines.indices) {
            if (lines[i].equals("tus ganancias", ignoreCase = true) && i + 1 < lines.size) {
                val next = lines[i + 1]
                val amount = cleanNumber(next)
                if (amount != null && amount > 0.0) return amount
            }
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

    private fun extractDistance(rawText: String, lines: List<String>): Double? {
        val pattern = Pattern.compile("distancia[:\\s]*([0-9]+[.,][0-9]+)\\s*km", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            val rawDist = matcher.group(1)?.replace(",", ".")
            return rawDist?.toDoubleOrNull()
        }

        val distIndex = lines.indexOfFirst { it.equals("distancia", ignoreCase = true) }
        if (distIndex in 0 until lines.size - 1) {
            val candidate = lines[distIndex + 1].replace("km", "").replace(",", ".").trim()
            candidate.toDoubleOrNull()?.let { return it }
        }

        return null
    }

    private fun extractDuration(rawText: String, lines: List<String>): String? {
        val pattern = Pattern.compile("duración[:\\s]*([0-9]+m\\s*[0-9]+s|[0-9]+\\s*min)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            return matcher.group(1)?.trim()
        }

        val durIndex = lines.indexOfFirst { it.equals("duración", ignoreCase = true) || it.equals("duracion", ignoreCase = true) }
        if (durIndex in 0 until lines.size - 1) {
            return lines[durIndex + 1].trim()
        }

        return null
    }

    private fun extractDurationMinutes(durationText: String?): Int? {
        if (durationText.isNullOrBlank()) return null
        val pattern = Pattern.compile("([0-9]+)\\s*m", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(durationText)
        if (matcher.find()) {
            return matcher.group(1)?.toIntOrNull()
        }
        return null
    }

    private fun extractTimestamp(rawText: String): Long {
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

            return Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, second)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
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
