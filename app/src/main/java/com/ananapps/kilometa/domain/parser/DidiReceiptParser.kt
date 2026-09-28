// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/parser/DidiReceiptParser.kt
// DESCRIPCIÓN: Parser determinista de recibos DiDi Conductor (Orden #050 / #051).
// Algoritmo unificado: Gross (Pagado por arrendatario/efectivo), DiDi Total (con signo).
// Fórmula: net = gross + didiTotal (didiTotal < 0: comisión, didiTotal > 0: bono).
// Red de seguridad determinista contra fragmentación de Google ML Kit.
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

    private val TOTAL_PATTERN = Pattern.compile(
        "(?i)total[^0-9\\-+]{0,40}([-+]?)\\s*\\$?\\s*([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,6})"
    )

    private val AMOUNT_PATTERN = Pattern.compile(
        "([-+]?)\\s*\\$?\\s*([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,6})"
    )

    fun canParse(rawText: String): Boolean {
        val lower = rawText.lowercase(Locale.ROOT)
        return lower.contains("tus solicitudes") ||
               lower.contains("recibido por didi") ||
               lower.contains("pagado por el arrendatario") ||
               lower.contains("didi confort") ||
               lower.contains("didi express") ||
               lower.contains("tarifa por distancia") ||
               (lower.contains("tus ganancias") &&
                (lower.contains("duración") || lower.contains("distancia")))
    }

    fun parse(rawText: String): ScannedTripReceipt? {
        if (!canParse(rawText)) return null

        val text = rawText
            .replace('\u00A0', ' ').replace('\u2007', ' ')
            .replace('\u202F', ' ').replace('\u3000', ' ')
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val lower = text.lowercase(Locale.ROOT)

        val distanceKm = extractDistance(text)
        val durationPair = extractDuration(text)

        val isCard = lower.contains("pago con tarjeta") || lower.contains("con tarjeta")
        val isVirtual = !isCard && (lower.contains("bancolombia") ||
                        lower.contains("nequi") || lower.contains("daviplata"))

        val paymentMethod = when {
            isCard    -> PaymentMethod.APP_WALLET
            isVirtual -> PaymentMethod.NEQUI
            else      -> PaymentMethod.CASH
        }
        val paymentDestination = if (isCard) {
            PaymentDestination.APP_RETAINED
        } else {
            PaymentDestination.CASH_IN_HAND
        }

        // PASO 1: Extracción del bloque "Pagado por el arrendatario"
        var gross = extractBlockTotal(
            text = text,
            header = "pagado por el arrendatario",
            stopAt = listOf("recibido por didi", "detalles", "tus solicitudes"),
            signed = false
        ) ?: 0.0

        // PASO 2: Extracción del bloque "Recibido por DiDi"
        var didiTotal = extractBlockTotal(
            text = text,
            header = "recibido por didi",
            stopAt = listOf("detalles", "tus solicitudes", "pagado por el arrendatario"),
            signed = true
        ) ?: 0.0

        // PASO 3: Red de Seguridad Determinista (contra fragmentación de columnas en ML Kit)
        if (didiTotal == 0.0) {
            val allTotals = findAllTotals(text)
            val negativeTotal = allTotals.firstOrNull { it.first == "-" }
            if (negativeTotal != null) {
                didiTotal = -negativeTotal.second
            }
        }

        if (gross == 0.0) {
            gross = extractArrendatarioAmount(text) ?: 0.0
        }
        if (gross == 0.0) {
            gross = extractFallbackGross(text) ?: 0.0
        }
        if (gross == 0.0) {
            val allTotals = findAllTotals(text)
            val positiveTotals = allTotals.filter { it.first != "-" }.map { it.second }
            if (positiveTotals.isNotEmpty()) {
                gross = positiveTotals.maxOrNull() ?: 0.0
            }
        }

        // PASO 4: Fórmula determinista unificada
        val effectiveGross = gross
        val effectiveNet = (gross + didiTotal).coerceAtLeast(0.0)
        val effectiveCommission = if (didiTotal < 0.0) -didiTotal else 0.0
        val platformPromo = if (didiTotal > 0.0) didiTotal else 0.0

        val confidenceSummary = buildString {
            append("DiDi · ")
            if (isCard) append("Tarjeta · ")
            when {
                didiTotal < 0.0 -> append("Parte de la App: -\$${effectiveCommission.toInt()}")
                didiTotal > 0.0 -> append("Bono DiDi: +\$${platformPromo.toInt()}")
                else            -> append("Sin comisión / bono")
            }
        }

        return ScannedTripReceipt(
            platform = PlatformType.DIDI,
            grossAmount = effectiveGross,
            netAmount = effectiveNet,
            commissionAmount = effectiveCommission,
            paymentMethod = paymentMethod,
            paymentDestination = paymentDestination,
            timestamp = extractTimestamp(text),
            distanceKm = distanceKm,
            durationText = durationPair?.first,
            durationMinutes = durationPair?.second,
            passengerName = extractPassenger(lines),
            platformPromoDiscount = platformPromo,
            confidenceSummary = confidenceSummary,
            rawText = rawText
        )
    }

    private fun extractBlockTotal(
        text: String,
        header: String,
        stopAt: List<String>,
        signed: Boolean
    ): Double? {
        val section = extractSection(text, header, stopAt) ?: return null
        val matcher = AMOUNT_PATTERN.matcher(section)
        var lastSign = ""
        var lastAmount: Double? = null
        while (matcher.find()) {
            lastSign = matcher.group(1) ?: ""
            lastAmount = cleanNumber(matcher.group(2))
        }
        return lastAmount?.let {
            if (signed && lastSign == "-") -it else it
        }
    }

    private fun findAllTotals(text: String): List<Pair<String, Double>> {
        val list = mutableListOf<Pair<String, Double>>()
        val matcher = TOTAL_PATTERN.matcher(text)
        while (matcher.find()) {
            val sign = matcher.group(1) ?: ""
            val num = cleanNumber(matcher.group(2))
            if (num != null && num > 0) {
                list.add(Pair(sign, num))
            }
        }
        return list
    }

    private fun extractArrendatarioAmount(text: String): Double? {
        val lower = text.lowercase(Locale.ROOT)
        val idx = lower.indexOf("arrendatario")
        if (idx < 0) return null
        val sub = text.substring(idx, (idx + 180).coerceAtMost(text.length))
        val matcher = TOTAL_PATTERN.matcher(sub)
        var found: Double? = null
        while (matcher.find()) {
            val sign = matcher.group(1) ?: ""
            val num = cleanNumber(matcher.group(2))
            if (num != null && num > 0 && sign != "-") {
                found = num
            }
        }
        return found
    }

    private fun extractFallbackGross(text: String): Double? {
        val lower = text.lowercase(Locale.ROOT)
        val stopIdx = listOf(
            lower.lastIndexOf("tus ganancias"),
            lower.indexOf("recibido por didi")
        ).filter { it >= 0 }.minOrNull() ?: text.length
        val headerSection = text.substring(0, stopIdx)
        val m = Pattern.compile(
            "(?:efectivo recibido|total)[^\\d]{0,50}([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,6})",
            Pattern.CASE_INSENSITIVE
        ).matcher(headerSection)
        var lastAmount: Double? = null
        while (m.find()) {
            lastAmount = cleanNumber(m.group(1))
        }
        return lastAmount
    }

    private fun extractSection(
        text: String,
        startHeader: String,
        stopHeaders: List<String>
    ): String? {
        val lower = text.lowercase(Locale.ROOT)
        fun String.toFlexRegex() = lowercase(Locale.ROOT).replace(" ", "\\s+").toRegex()

        val startMatch = startHeader.toFlexRegex().find(lower) ?: return null
        val startIndex = startMatch.range.first
        var endIndex = text.length

        for (stop in stopHeaders) {
            val stopMatch = stop.toFlexRegex().find(lower, startIndex + startMatch.value.length)
            if (stopMatch != null && stopMatch.range.first < endIndex) {
                endIndex = stopMatch.range.first
            }
        }
        return text.substring(startIndex, endIndex)
    }

    private fun extractDistance(text: String): Double? {
        val m = Pattern.compile(
            "([0-9]+(?:[.,][0-9]+)?)\\s*km\\b", Pattern.CASE_INSENSITIVE
        ).matcher(text)
        return if (m.find()) m.group(1)?.replace(",", ".")?.toDoubleOrNull() else null
    }

    private fun extractDuration(text: String): Pair<String, Int>? {
        val pComp = Pattern.compile(
            "([0-9]+)\\s*m\\s*(?:([0-9]+)\\s*s)?", Pattern.CASE_INSENSITIVE
        ).matcher(text)
        if (pComp.find()) {
            val minutes = pComp.group(1)?.toIntOrNull() ?: 0
            val seconds = pComp.group(2)?.toIntOrNull() ?: 0
            val total = if (seconds >= 30) minutes + 1 else minutes
            val formatted = if (seconds > 0) "${minutes}m ${seconds}s" else "${minutes}m"
            return Pair(formatted, total.coerceAtLeast(1))
        }
        val pMin = Pattern.compile(
            "([0-9]+)\\s*(?:min|minutos)\\b", Pattern.CASE_INSENSITIVE
        ).matcher(text)
        if (pMin.find()) {
            val min = pMin.group(1)?.toIntOrNull() ?: 0
            return Pair("${min}m", min.coerceAtLeast(1))
        }
        return null
    }

    private fun extractTimestamp(text: String): Long {
        val p = Pattern.compile(
            "(\\d{1,2})/(\\d{1,2})/(\\d{4}),?\\s*(\\d{1,2}):(\\d{2})(?::(\\d{2}))?\\s*(am|pm|a\\.m\\.|p\\.m\\.)?",
            Pattern.CASE_INSENSITIVE
        ).matcher(text)
        if (p.find()) {
            val day = p.group(1)?.toIntOrNull() ?: 1
            val month = (p.group(2)?.toIntOrNull() ?: 1) - 1
            val year = p.group(3)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
            var hour = p.group(4)?.toIntOrNull() ?: 0
            val min = p.group(5)?.toIntOrNull() ?: 0
            val sec = p.group(6)?.toIntOrNull() ?: 0
            val ampm = p.group(7)?.lowercase(Locale.ROOT)
            if (ampm != null && (ampm.contains("pm") || ampm.contains("p.m.")) && hour < 12) hour += 12
            else if (ampm != null && (ampm.contains("am") || ampm.contains("a.m.")) && hour == 12) hour = 0
            return Calendar.getInstance().apply {
                set(Calendar.YEAR, year); set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day); set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, min); set(Calendar.SECOND, sec)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        return System.currentTimeMillis()
    }

    private fun extractPassenger(lines: List<String>): String? {
        val idx = lines.indexOfFirst { it.equals("detalles", ignoreCase = true) }
        if (idx >= 0) {
            for (i in (idx + 1) until (idx + 6).coerceAtMost(lines.size)) {
                val candidate = lines[i]
                if (!candidate.contains("/") && !candidate.contains(":") &&
                    !candidate.contains("$") &&
                    !candidate.lowercase(Locale.ROOT).contains("total") &&
                    candidate.none { it.isDigit() } && candidate.length in 2..30) {
                    return candidate
                }
            }
        }
        return null
    }

    private fun cleanNumber(str: String?): Double? =
        str?.filter { it.isDigit() }?.toDoubleOrNull()
}
