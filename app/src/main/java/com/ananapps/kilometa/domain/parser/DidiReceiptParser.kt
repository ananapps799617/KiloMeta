// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/parser/DidiReceiptParser.kt
// DESCRIPCIÓN: Analizador heurístico ultra-resiliente para pantallazos de DiDi Conductor.
// Soporte trimodal (Tarjeta, Subsidio, Efectivo) con reconciliación matemática (Orden #050).
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

    private val AMOUNT_PATTERN = Pattern.compile("[-+]?\\$?\\s*([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,6})")

    fun canParse(rawText: String): Boolean {
        val lower = rawText.lowercase(Locale.ROOT)
        return lower.contains("tus solicitudes") || lower.contains("recibido por didi") ||
                lower.contains("pagado por el arrendatario") || lower.contains("didi confort") ||
                lower.contains("didi express") || lower.contains("tarifa por distancia") ||
                (lower.contains("tus ganancias") && (lower.contains("más rápido") || lower.contains("mas rapido") || lower.contains("duración") || lower.contains("distancia")))
    }

    fun parse(rawText: String): ScannedTripReceipt? {
        if (!canParse(rawText)) return null

        val text = rawText.replace('\u00A0', ' ').replace('\u2007', ' ').replace('\u202F', ' ').replace('\u3000', ' ')
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }

        val distanceKm = extractDistance(text)
        val durationPair = extractDuration(text)
        val netEarnings = extractNetEarnings(text, lines) ?: 0.0
        val paidByRenter = extractPaidByRenter(text)
        val directCommission = extractDirectCommission(text)
        val cashReceived = extractCashReceived(text, lines)

        val lower = text.lowercase(Locale.ROOT)
        val isCardTrip = lower.contains("pago con tarjeta") || lower.contains("con tarjeta") || lower.contains("tarjeta")
        val isSubsidyTrip = lower.contains("recarga de didi") || lower.contains("descuento del usuario")

        val effectiveGross: Double
        val effectiveNet: Double
        val effectiveCommission: Double
        val platformPromoDiscount: Double
        val paymentMethod: PaymentMethod
        val paymentDestination: PaymentDestination
        val confidenceSummary: String

        if (isCardTrip) {
            paymentMethod = PaymentMethod.APP_WALLET
            paymentDestination = PaymentDestination.APP_RETAINED
            val resolvedGross = when {
                paidByRenter != null && paidByRenter > 0.0 -> paidByRenter
                netEarnings > 0.0 && directCommission != null && directCommission > 0.0 -> netEarnings + directCommission
                else -> netEarnings
            }
            val resolvedComm = when {
                directCommission != null && directCommission > 0.0 -> directCommission
                resolvedGross > netEarnings -> resolvedGross - netEarnings
                else -> 0.0
            }
            effectiveGross = resolvedGross
            effectiveCommission = resolvedComm
            effectiveNet = if (netEarnings > 0.0) netEarnings else (resolvedGross - resolvedComm).coerceAtLeast(0.0)
            platformPromoDiscount = 0.0
            confidenceSummary = "DiDi · Pago con Tarjeta ($0 cobrado en mano)"
        } else if (isSubsidyTrip) {
            paymentMethod = if (lower.contains("bancolombia")) PaymentMethod.NEQUI else PaymentMethod.CASH
            paymentDestination = PaymentDestination.CASH_IN_HAND
            val directSub = extractDirectSubsidy(text) ?: 0.0
            val fallbackCash = findFallbackCash(text, netEarnings) ?: (if (netEarnings > directSub) netEarnings - directSub else netEarnings)
            val collectedInHand = cashReceived ?: paidByRenter ?: fallbackCash
            val subsidy = if (netEarnings > collectedInHand) netEarnings - collectedInHand else directSub
            effectiveGross = collectedInHand
            effectiveCommission = 0.0
            effectiveNet = if (netEarnings > 0.0) netEarnings else (collectedInHand + subsidy)
            platformPromoDiscount = subsidy
            confidenceSummary = "DiDi · Subsidio/Recarga DiDi (+$${platformPromoDiscount.toInt()} a favor en app)"
        } else {
            paymentMethod = if (lower.contains("bancolombia")) PaymentMethod.NEQUI else PaymentMethod.CASH
            paymentDestination = PaymentDestination.CASH_IN_HAND
            val collectedCash = cashReceived ?: paidByRenter ?: (if (directCommission != null && directCommission > 0) netEarnings + directCommission else netEarnings)
            val resolvedComm = directCommission ?: (collectedCash - netEarnings).coerceAtLeast(0.0)
            effectiveGross = collectedCash
            effectiveCommission = resolvedComm
            effectiveNet = if (netEarnings > 0.0) netEarnings else (collectedCash - resolvedComm).coerceAtLeast(0.0)
            platformPromoDiscount = 0.0
            confidenceSummary = "DiDi · Cobrado en mano (-$${resolvedComm.toInt()} comisión app)"
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
            platformPromoDiscount = platformPromoDiscount,
            confidenceSummary = confidenceSummary,
            rawText = rawText
        )
    }

    private fun extractNetEarnings(text: String, lines: List<String>): Double? {
        val headerIdx = lines.indexOfFirst { it.trim().equals("tus ganancias", ignoreCase = true) }
        if (headerIdx >= 0) {
            for (i in (headerIdx + 1)..minOf(headerIdx + 4, lines.size - 1)) {
                val candidate = lines[i]
                if (candidate.contains("ajustad", ignoreCase = true) ||
                    candidate.contains("estimad", ignoreCase = true) ||
                    candidate.contains("han ajustado", ignoreCase = true)
                ) break
                val matcher = AMOUNT_PATTERN.matcher(candidate)
                if (matcher.find()) {
                    val amount = cleanNumber(matcher.group(1))
                    if (amount != null && amount >= 1000.0) return amount
                }
            }
        }
        val earningsSection = extractSection(text, "tus ganancias", listOf("pagado por el arrendatario", "detalles", "recibido por didi"))
        if (earningsSection != null) {
            val totalPattern = Pattern.compile("total[^\\d]{0,400}([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,6})", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
            val matcher = totalPattern.matcher(earningsSection)
            if (matcher.find()) {
                val amount = cleanNumber(matcher.group(1))
                if (amount != null && amount >= 1000.0) return amount
            }
        }
        return null
    }

    private fun extractPaidByRenter(text: String): Double? {
        val renterSection = extractSection(text, "pagado por el arrendatario", listOf("recibido por didi", "detalles", "tus solicitudes"))
        if (renterSection != null) {
            val pSePago = Pattern.compile("se\\s*pag[oó][^\\d]{0,400}([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,6})", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
            val mSePago = pSePago.matcher(renterSection)
            if (mSePago.find()) return cleanNumber(mSePago.group(1))

            val pTotal = Pattern.compile("total[^\\d]{0,400}([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,6})", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
            val mTotal = pTotal.matcher(renterSection)
            if (mTotal.find()) return cleanNumber(mTotal.group(1))

            val pTarifa = Pattern.compile("tarifa\\s*(?:de\\s*la\\s*solic\\.?)?[^\\d]{0,400}([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,6})", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
            val mTarifa = pTarifa.matcher(renterSection)
            if (mTarifa.find()) return cleanNumber(mTarifa.group(1))
        }
        return null
    }

    private fun extractDirectCommission(text: String): Double? {
        val didiSection = extractSection(text, "recibido por didi", listOf("detalles", "tus solicitudes", "tus ganancias"))
        if (didiSection != null) {
            val pServ = Pattern.compile("tarifa\\s*servicio[^\\d]{0,400}[-]?([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,6})", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
            val mServ = pServ.matcher(didiSection)
            if (mServ.find()) return cleanNumber(mServ.group(1))

            val pTotal = Pattern.compile("total[^\\d]{0,400}[-]?([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,6})", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
            val mTotal = pTotal.matcher(didiSection)
            if (mTotal.find()) return cleanNumber(mTotal.group(1))
        }
        return null
    }

    private fun extractDirectSubsidy(text: String): Double? {
        val pattern = Pattern.compile("recarga\\s*de\\s*didi[^\\d]{0,400}([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,6})", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) cleanNumber(matcher.group(1)) else null
    }

    private fun extractCashReceived(text: String, lines: List<String>): Double? {
        val labelIdx = lines.indexOfFirst { it.contains("efectivo recibido", ignoreCase = true) }
        if (labelIdx >= 0) {
            for (j in labelIdx..minOf(labelIdx + 6, lines.size - 1)) {
                val matcher = AMOUNT_PATTERN.matcher(lines[j])
                while (matcher.find()) {
                    val amount = cleanNumber(matcher.group(1))
                    if (amount != null && amount >= 1000.0) return amount
                }
            }
        }
        val pattern = Pattern.compile("efectivo\\s*recibido[^\\d]{0,400}([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,6})", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) cleanNumber(matcher.group(1)) else null
    }

    private fun findFallbackCash(text: String, netEarnings: Double): Double? {
        var maxCand: Double? = null
        val m = AMOUNT_PATTERN.matcher(text)
        while (m.find()) {
            val amt = cleanNumber(m.group(1)) ?: continue
            if (amt in (netEarnings * 0.5)..netEarnings) maxCand = maxOf(maxCand ?: 0.0, amt)
        }
        return maxCand
    }

    private fun extractSection(text: String, startHeader: String, stopHeaders: List<String>): String? {
        val lower = text.lowercase(Locale.ROOT)
        fun String.toFlexRegex() = lowercase(Locale.ROOT).replace(" ", "\\s+").toRegex()
        
        val startMatch = startHeader.toFlexRegex().find(lower) ?: return null
        val startIndex = startMatch.range.first
        var endIndex = text.length
        for (stop in stopHeaders) {
            val stopMatch = stop.toFlexRegex().find(lower, startIndex + startMatch.value.length)
            if (stopMatch != null && stopMatch.range.first < endIndex) endIndex = stopMatch.range.first
        }
        return text.substring(startIndex, endIndex)
    }

    private fun extractDistance(text: String): Double? {
        val pattern = Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*km\\b", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(1)?.replace(",", ".")?.toDoubleOrNull() else null
    }

    private fun extractDuration(text: String): Pair<String, Int>? {
        val pComp = Pattern.compile("([0-9]+)\\s*m\\s*(?:([0-9]+)\\s*s)?", Pattern.CASE_INSENSITIVE)
        val mComp = pComp.matcher(text)
        if (mComp.find()) {
            val minutes = mComp.group(1)?.toIntOrNull() ?: 0
            val seconds = mComp.group(2)?.toIntOrNull() ?: 0
            val total = if (seconds >= 30) minutes + 1 else minutes
            val formattedDuration = if (seconds > 0) "${minutes}m ${seconds}s" else "${minutes}m"
            return Pair(formattedDuration, total.coerceAtLeast(1))
        }
        val pMin = Pattern.compile("([0-9]+)\\s*(?:min|minutos)\\b", Pattern.CASE_INSENSITIVE)
        val mMin = pMin.matcher(text)
        if (mMin.find()) {
            val min = mMin.group(1)?.toIntOrNull() ?: 0
            return Pair("${min}m", min.coerceAtLeast(1))
        }
        return null
    }

    private fun extractTimestamp(text: String): Long {
        val p = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{4}),?\\s*(\\d{1,2}):(\\d{2})(?::(\\d{2}))?\\s*(am|pm|a\\.m\\.|p\\.m\\.)?", Pattern.CASE_INSENSITIVE)
        val m = p.matcher(text)
        if (m.find()) {
            val day = m.group(1)?.toIntOrNull() ?: 1
            val month = (m.group(2)?.toIntOrNull() ?: 1) - 1
            val year = m.group(3)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
            var hour = m.group(4)?.toIntOrNull() ?: 0
            val min = m.group(5)?.toIntOrNull() ?: 0
            val sec = m.group(6)?.toIntOrNull() ?: 0
            val ampm = m.group(7)?.lowercase(Locale.ROOT)
            if (ampm != null && (ampm.contains("pm") || ampm.contains("p.m.")) && hour < 12) hour += 12
            else if (ampm != null && (ampm.contains("am") || ampm.contains("a.m.")) && hour == 12) hour = 0
            return Calendar.getInstance().apply {
                set(Calendar.YEAR, year); set(Calendar.MONTH, month); set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, min); set(Calendar.SECOND, sec); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        return System.currentTimeMillis()
    }

    private fun extractPassenger(lines: List<String>): String? {
        val detailsIndex = lines.indexOfFirst { it.equals("detalles", ignoreCase = true) }
        if (detailsIndex in 0 until lines.size - 1) {
            val candidate = lines[detailsIndex + 1]
            if (!candidate.contains("/") && !candidate.contains(":") && candidate.length < 30) return candidate
        }
        return null
    }

    private fun cleanNumber(str: String?): Double? = str?.filter { it.isDigit() }?.toDoubleOrNull()
}
