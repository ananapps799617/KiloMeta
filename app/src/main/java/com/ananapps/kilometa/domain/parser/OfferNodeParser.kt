// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/parser/OfferNodeParser.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Analizador determinista de textos y nodos de accesibilidad para Uber, DiDi e inDrive (Orden #047).
// Extrae de forma ultra ligera (<= 25 ms) tarifa, distancias y duración estimada.
// ==============================================================================

package com.ananapps.kilometa.domain.parser

import android.view.accessibility.AccessibilityNodeInfo
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.TripOffer
import javax.inject.Inject

class OfferNodeParser @Inject constructor() {

    private val fareRegex = Regex("(?:\\$|COP|cop)?\\s*([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,6})")
    private val distanceRegex = Regex("([0-9]+(?:[.,][0-9]+)?)\\s*(km|m|kilometros|metros)\\b", RegexOption.IGNORE_CASE)
    private val durationRegex = Regex("(?:([0-9]+)\\s*h(?:ora)?s?)?\\s*([0-9]+)\\s*min\\b", RegexOption.IGNORE_CASE)

    fun extractAllTexts(rootNode: AccessibilityNodeInfo?): List<String> {
        if (rootNode == null) return emptyList()
        val result = mutableListOf<String>()
        collectTextsRecursively(rootNode, result)
        return result
    }

    private fun collectTextsRecursively(node: AccessibilityNodeInfo, list: MutableList<String>) {
        node.text?.toString()?.trim()?.takeIf { it.isNotBlank() }?.let { list.add(it) }
        node.contentDescription?.toString()?.trim()?.takeIf { it.isNotBlank() }?.let { list.add(it) }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectTextsRecursively(child, list)
        }
    }

    fun parse(packageName: String, texts: List<String>): TripOffer? {
        if (texts.isEmpty()) return null
        if (!isRealTripOffer(texts)) return null

        val platform = resolvePlatform(packageName, texts) ?: return null
        val fare = extractFare(texts) ?: return null
        val (pickupDist, tripDist) = extractDistances(texts) ?: return null
        val minutes = extractDurationMinutes(texts)

        return TripOffer(
            platform = platform,
            offeredFare = fare,
            pickupDistanceKm = pickupDist,
            tripDistanceKm = tripDist,
            estimatedMinutes = minutes
        )
    }

    fun isRealTripOffer(texts: List<String>): Boolean {
        for (text in texts) {
            val lower = text.lowercase()
            if (lower.startsWith("aceptar") || lower.contains("aceptar") ||
                lower.contains("oferta de pasajero") || lower.contains("contraoferta") ||
                lower.contains("recogida") || lower.contains("viaje de") ||
                lower.contains("viaje:") || lower.contains("en tu precio") ||
                lower.contains("pon tu precio") || lower.contains("arrendamiento")
            ) {
                return true
            }
        }
        return false
    }

    private fun resolvePlatform(pkg: String, texts: List<String>): PlatformType? {
        val lowerTexts = texts.joinToString(" ").lowercase()
        return when {
            lowerTexts.contains("en tu precio") || lowerTexts.contains("pon tu precio") ||
            lowerTexts.contains("didi") || lowerTexts.contains("arrendamiento") -> PlatformType.DIDI

            lowerTexts.contains("uberya") || lowerTexts.contains("uberx") ||
            lowerTexts.contains("uber") || lowerTexts.contains("reserve") -> PlatformType.UBER

            lowerTexts.contains("indrive") || lowerTexts.contains("in-drive") -> PlatformType.INDRIVE

            pkg.contains("ubercab", ignoreCase = true) -> PlatformType.UBER
            pkg.contains("didi", ignoreCase = true) -> PlatformType.DIDI
            pkg.contains("indriver", ignoreCase = true) -> PlatformType.INDRIVE
            else -> null
        }
    }

    private fun extractFare(texts: List<String>): Double? {
        val acceptCandidates = mutableListOf<Double>()
        val generalCandidates = mutableListOf<Double>()

        for (text in texts) {
            val lower = text.lowercase()
            if (lower.contains("/km") || lower.contains("/ km") || lower.contains("/h") ||
                lower.contains("gasolina") || lower.contains("refer") ||
                lower.contains("gana") || lower.contains("genera") || lower.contains("invita") ||
                lower.contains("batería") || lower.contains("bateria")
            ) continue

            val match = fareRegex.find(text)
            if (match != null) {
                val raw = match.groupValues[1].replace(".", "").replace(",", "")
                val value = raw.toDoubleOrNull()
                if (value != null && value in 3_000.0..150_000.0) {
                    if (lower.contains("aceptar")) {
                        acceptCandidates.add(value)
                    } else {
                        generalCandidates.add(value)
                    }
                }
            }
        }

        if (acceptCandidates.isNotEmpty()) {
            return acceptCandidates.first()
        }
        return generalCandidates.firstOrNull() ?: generalCandidates.maxOrNull()
    }

    private fun extractDistances(texts: List<String>): Pair<Double, Double>? {
        val distances = mutableListOf<Double>()
        for (text in texts) {
            val lower = text.lowercase()
            if (lower.contains("/km") || lower.contains("/ km")) continue
            val matches = distanceRegex.findAll(text)
            for (m in matches) {
                val num = m.groupValues[1].replace(",", ".").toDoubleOrNull() ?: continue
                val unit = m.groupValues[2].lowercase()
                val km = if (unit.startsWith("m")) num / 1000.0 else num
                if (km in 0.05..150.0) {
                    distances.add(km)
                }
            }
        }

        return when {
            distances.size >= 2 -> Pair(distances[0], distances[1])
            distances.size == 1 -> Pair(0.5, distances[0])
            else -> null
        }
    }

    private fun extractDurationMinutes(texts: List<String>): Int {
        val durations = mutableListOf<Int>()
        for (text in texts) {
            val matches = durationRegex.findAll(text)
            for (m in matches) {
                val hours = m.groupValues[1].toIntOrNull() ?: 0
                val mins = m.groupValues[2].toIntOrNull() ?: 0
                val total = (hours * 60) + mins
                if (total in 1..300) durations.add(total)
            }
        }
        return when {
            durations.size >= 2 -> durations[0] + durations[1]
            durations.size == 1 -> durations[0]
            else -> 12 // Fallback razonable de duración promedio urbana
        }
    }
}
