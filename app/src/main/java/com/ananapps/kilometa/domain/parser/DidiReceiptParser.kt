// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/parser/DidiReceiptParser.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Analizador heurístico especializado para pantallazos de la aplicación DiDi Conductor.
// Soporta los 3 modos de operación vehicular: Efectivo, Tarjeta y Subsidios/Recargas DiDi.
// Extrae deterministamente ganancias netas, comisiones, distancia (km) y duración (minutos) (Orden #050).
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
                lower.contains("didi express") ||
                lower.contains("tarifa por distancia") ||
                (lower.contains("tus ganancias") && (lower.contains("más rápido") || lower.contains("duración") || lower.contains("duracion") || lower.contains("distancia")))
    }

    fun parse(rawText: String): ScannedTripReceipt? {
        if (!canParse(rawText)) return null

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        // 1. Ganancia Neta del Conductor (Monto grande de cabecera o Total de ganancias)
        val netEarnings = extractNetEarnings(rawText, lines) ?: 0.0

        // 2. Extracción de Telemetría (Distancia en km y Duración en minutos)
        val distanceKm = extractDistance(rawText)
        val durationPair = extractDuration(rawText)
        val durationText = durationPair?.first
        val durationMinutes = durationPair?.second

        // 3. Detección de Modalidad Trimodal (Tarjeta vs Subsidio vs Efectivo)
        val lower = rawText.lowercase(Locale.ROOT)
        val isCardTrip = lower.contains("pago con tarjeta") || lower.contains("con tarjeta")
        val isSubsidyTrip = lower.contains("recarga de didi") || lower.contains("descuento del usuario")

        val effectiveGross: Double
        val effectiveNet = netEarnings
        val effectiveCommission: Double
        val platformPromoDiscount: Double
        val paymentMethod: PaymentMethod
        val paymentDestination: PaymentDestination
        val confidenceSummary: String

        if (isCardTrip) {
            // CASO 2: PAGO CON TARJETA / DIGITAL
            val paidByRenter = extractPaidByRenter(rawText) ?: netEarnings
            paymentMethod = PaymentMethod.APP_WALLET
            paymentDestination = PaymentDestination.APP_RETAINED
            effectiveGross = paidByRenter
            effectiveCommission = (paidByRenter - netEarnings).coerceAtLeast(0.0)
            platformPromoDiscount = 0.0
            confidenceSummary = "DiDi · Pago con Tarjeta ($0 cobrado en mano)"
        } else if (isSubsidyTrip) {
            // CASO 3: CON BONO / SUBSIDIO / RECARGA DIDI
            paymentMethod = if (lower.contains("bancolombia")) PaymentMethod.NEQUI else PaymentMethod.CASH
            paymentDestination = PaymentDestination.CASH_IN_HAND
            val collectedInHand = extractCashReceived(rawText) ?: extractPaidByRenter(rawText) ?: netEarnings
            val subsidy = (netEarnings - collectedInHand).coerceAtLeast(0.0)
            effectiveGross = netEarnings
            effectiveCommission = 0.0
            platformPromoDiscount = subsidy
            confidenceSummary = "DiDi · Subsidio/Recarga DiDi (+$${subsidy.toInt()} a favor en app)"
        } else {
            // CASO 1: EN EFECTIVO PURO
            paymentMethod = if (lower.contains("bancolombia")) PaymentMethod.NEQUI else PaymentMethod.CASH
            paymentDestination = PaymentDestination.CASH_IN_HAND
            val cashReceived = extractCashReceived(rawText) ?: extractPaidByRenter(rawText) ?: netEarnings

            if (cashReceived >= netEarnings) {
                effectiveGross = cashReceived
                effectiveCommission = (cashReceived - netEarnings).coerceAtLeast(0.0)
                platformPromoDiscount = 0.0
                confidenceSummary = "DiDi · Cobrado en mano (-$${effectiveCommission.toInt()} comisión app)"
            } else {
                val subsidy = (netEarnings - cashReceived).coerceAtLeast(0.0)
                effectiveGross = cashReceived
                effectiveCommission = 0.0
                platformPromoDiscount = subsidy
                confidenceSummary = "DiDi · Con abono/promo (+$${subsidy.toInt()} a favor en app)"
            }
        }

        // 4. Extracción de Fecha, Hora y Pasajero
        val timestamp = extractTimestamp(rawText)
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
        // Prioridad 1: Monto grande superior de cabecera en las primeras 12 líneas
        val headerPattern = Pattern.compile("^\\$?\\s*([0-9]{1,3}(?:[.,][0-9]{3})+)$")
        for (i in 0 until minOf(lines.size, 12)) {
            val line = lines[i]
            val matcher = headerPattern.matcher(line)
            if (matcher.matches()) {
                val amount = cleanNumber(matcher.group(1))
                if (amount != null && amount >= 1000.0) return amount
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

    private fun extractDistance(rawText: String): Double? {
        // Busca cualquier token como "6,1 km", "10,1 km", "7.3km"
        val pattern = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*km\\b", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            val rawDist = matcher.group(1)?.replace(",", ".")
            return rawDist?.toDoubleOrNull()
        }
        return null
    }

    private fun extractDuration(rawText: String): Pair<String, Int>? {
        // Busca formato "25m23s", "28m50s", "23m17s"
        val patternCompound = Pattern.compile("([0-9]+)\\s*m\\s*(?:([0-9]+)\\s*s)?", Pattern.CASE_INSENSITIVE)
        val matcherCompound = patternCompound.matcher(rawText)
        if (matcherCompound.find()) {
            val minutes = matcherCompound.group(1)?.toIntOrNull() ?: 0
            val seconds = matcherCompound.group(2)?.toIntOrNull() ?: 0
            val totalMinutes = if (seconds >= 30) minutes + 1 else minutes
            val text = if (seconds > 0) "${minutes}m ${seconds}s" else "${minutes}m"
            return Pair(text, totalMinutes.coerceAtLeast(1))
        }

        // Busca formato simple "25 min" o "25 minutos"
        val patternMin = Pattern.compile("([0-9]+)\\s*(?:min|minutos)\\b", Pattern.CASE_INSENSITIVE)
        val matcherMin = patternMin.matcher(rawText)
        if (matcherMin.find()) {
            val min = matcherMin.group(1)?.toIntOrNull() ?: 0
            return Pair("${min}m", min.coerceAtLeast(1))
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
