// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/parser/DidiReceiptParser.kt
// DESCRIPCIÓN: Parser determinista de recibos DiDi Conductor (Orden #050 / #051).
// Algoritmo unificado: Gross (Pagado por arrendatario/efectivo), DiDi Total (con signo).
// Fórmula: net = gross + didiTotal (didiTotal < 0: comisión, didiTotal > 0: bono).
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
               (lower.contains("tus ganancias") &&
                (lower.contains("duración") || lower.contains("distancia")))
    }

    // -----------------------------------------------------------------------
    // PARSE PRINCIPAL — Algoritmo determinista unificado
    // -----------------------------------------------------------------------
    fun parse(rawText: String): ScannedTripReceipt? {
        if (!canParse(rawText)) return null

        // Normalizar espacios especiales de Unicode que ML Kit puede introducir
        val text = rawText
            .replace('\u00A0', ' ').replace('\u2007', ' ')
            .replace('\u202F', ' ').replace('\u3000', ' ')
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val lower = text.lowercase(Locale.ROOT)

        // PASO 1 — Duración y Distancia
        val distanceKm   = extractDistance(text)
        val durationPair = extractDuration(text)

        // PASO 2 — Detectar método de pago
        val isCard    = lower.contains("pago con tarjeta") || lower.contains("con tarjeta")
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

        // PASO 3 — "Pagado por el arrendatario" → ÚLTIMO Total → GROSS
        val gross = extractSectionLastTotal(
            text,
            header  = "pagado por el arrendatario",
            stopAt  = listOf("recibido por didi", "detalles", "tus solicitudes"),
            signed  = false
        ) ?: extractFallbackGross(text) ?: 0.0

        // PASO 4 — "Recibido por DiDi" → ÚLTIMO Total (CON SIGNO) → DIDI_TOTAL
        val didiTotal = extractSectionLastTotal(
            text,
            header  = "recibido por didi",
            stopAt  = listOf("detalles", "tus solicitudes", "pagado por el arrendatario"),
            signed  = true
        ) ?: 0.0

        // PASO 5 — Fórmula unificada según signo de didiTotal
        //   net = gross + didiTotal
        //   didiTotal < 0 → comisión (resta)
        //   didiTotal > 0 → bono DiDi (suma)
        val effectiveGross      = gross
        val effectiveNet        = (gross + didiTotal).coerceAtLeast(0.0)
        val effectiveCommission = if (didiTotal < 0.0) -didiTotal else 0.0
        val platformPromo       = if (didiTotal > 0.0)  didiTotal else 0.0

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
            platform             = PlatformType.DIDI,
            grossAmount          = effectiveGross,
            netAmount            = effectiveNet,
            commissionAmount     = effectiveCommission,
            paymentMethod        = paymentMethod,
            paymentDestination   = paymentDestination,
            timestamp            = extractTimestamp(text),
            distanceKm           = distanceKm,
            durationText         = durationPair?.first,
            durationMinutes      = durationPair?.second,
            passengerName        = extractPassenger(lines),
            platformPromoDiscount = platformPromo,
            confidenceSummary    = confidenceSummary,
            rawText              = rawText
        )
    }

    // -----------------------------------------------------------------------
    // EXTRACTOR CLAVE: busca el monto consolidado al final de la sección.
    // En DiDi, el último valor monetario de la sección es siempre el Total.
    // -----------------------------------------------------------------------
    private fun extractSectionLastTotal(
        text: String,
        header: String,
        stopAt: List<String>,
        signed: Boolean
    ): Double? {
        val section = extractSection(text, header, stopAt) ?: return null

        val pattern = Pattern.compile(
            "([-+]?)\\s*\\$?\\s*([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,6})"
        )
        val matcher = pattern.matcher(section)
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

    // Fallback para recibos de efectivo donde no dice "Pagado por el arrendatario"
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


    // -----------------------------------------------------------------------
    // EXTRACTOR DE SECCIÓN: tolerante a saltos de línea en encabezados
    // (ML Kit puede partir "Pagado por\nel arrendatario" con \n)
    // -----------------------------------------------------------------------
    private fun extractSection(
        text: String,
        startHeader: String,
        stopHeaders: List<String>
    ): String? {
        val lower = text.lowercase(Locale.ROOT)
        // Construir regex flexible: cada espacio → \s+ (incluye \n de ML Kit)
        fun String.toFlexRegex() = lowercase(Locale.ROOT).replace(" ", "\\s+").toRegex()

        val startMatch = startHeader.toFlexRegex().find(lower) ?: return null
        val startIndex = startMatch.range.first
        var endIndex   = text.length

        for (stop in stopHeaders) {
            val stopMatch = stop.toFlexRegex().find(lower, startIndex + startMatch.value.length)
            if (stopMatch != null && stopMatch.range.first < endIndex) {
                endIndex = stopMatch.range.first
            }
        }
        return text.substring(startIndex, endIndex)
    }

    // -----------------------------------------------------------------------
    // DISTANCIA — captura "X,X km" o "X.X km"
    // -----------------------------------------------------------------------
    private fun extractDistance(text: String): Double? {
        val m = Pattern.compile(
            "([0-9]+(?:[.,][0-9]+)?)\\s*km\\b", Pattern.CASE_INSENSITIVE
        ).matcher(text)
        return if (m.find()) m.group(1)?.replace(",", ".")?.toDoubleOrNull() else null
    }

    // -----------------------------------------------------------------------
    // DURACIÓN — captura "Xm Xs" o "X min"
    // -----------------------------------------------------------------------
    private fun extractDuration(text: String): Pair<String, Int>? {
        val pComp = Pattern.compile(
            "([0-9]+)\\s*m\\s*(?:([0-9]+)\\s*s)?", Pattern.CASE_INSENSITIVE
        ).matcher(text)
        if (pComp.find()) {
            val minutes         = pComp.group(1)?.toIntOrNull() ?: 0
            val seconds         = pComp.group(2)?.toIntOrNull() ?: 0
            val total           = if (seconds >= 30) minutes + 1 else minutes
            val formattedDuration = if (seconds > 0) "${minutes}m ${seconds}s" else "${minutes}m"
            return Pair(formattedDuration, total.coerceAtLeast(1))
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

    // -----------------------------------------------------------------------
    // TIMESTAMP — extrae fecha y hora del recibo
    // -----------------------------------------------------------------------
    private fun extractTimestamp(text: String): Long {
        val p = Pattern.compile(
            "(\\d{1,2})/(\\d{1,2})/(\\d{4}),?\\s*(\\d{1,2}):(\\d{2})(?::(\\d{2}))?\\s*(am|pm|a\\.m\\.|p\\.m\\.)?",
            Pattern.CASE_INSENSITIVE
        ).matcher(text)
        if (p.find()) {
            val day   = p.group(1)?.toIntOrNull() ?: 1
            val month = (p.group(2)?.toIntOrNull() ?: 1) - 1
            val year  = p.group(3)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
            var hour  = p.group(4)?.toIntOrNull() ?: 0
            val min   = p.group(5)?.toIntOrNull() ?: 0
            val sec   = p.group(6)?.toIntOrNull() ?: 0
            val ampm  = p.group(7)?.lowercase(Locale.ROOT)
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

    // -----------------------------------------------------------------------
    // PASAJERO — nombre en la sección "Detalles"
    // -----------------------------------------------------------------------
    private fun extractPassenger(lines: List<String>): String? {
        val idx = lines.indexOfFirst { it.equals("detalles", ignoreCase = true) }
        if (idx in 0 until lines.size - 1) {
            val candidate = lines[idx + 1]
            if (!candidate.contains("/") && !candidate.contains(":") && candidate.length < 30) {
                return candidate
            }
        }
        return null
    }

    // -----------------------------------------------------------------------
    // UTILIDAD: limpia un string a Double (solo dígitos)
    // -----------------------------------------------------------------------
    private fun cleanNumber(str: String?): Double? =
        str?.filter { it.isDigit() }?.toDoubleOrNull()
}
