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
        val platform = resolvePlatform(packageName) ?: return null
        if (texts.isEmpty()) return null

        val fare = extractFare(texts) ?: return null
        val (pickupDist, tripDist) = extractDistances(texts)
        val minutes = extractDurationMinutes(texts)

        return TripOffer(
            platform = platform,
            offeredFare = fare,
            pickupDistanceKm = pickupDist,
            tripDistanceKm = tripDist,
            estimatedMinutes = minutes
        )
    }

    private fun resolvePlatform(pkg: String): PlatformType? = when {
        pkg.contains("ubercab", ignoreCase = true) -> PlatformType.UBER
        pkg.contains("didi", ignoreCase = true) -> PlatformType.DIDI
        pkg.contains("indriver", ignoreCase = true) -> PlatformType.INDRIVE
        else -> null
    }

    private fun extractFare(texts: List<String>): Double? {
        for (text in texts) {
            val match = fareRegex.find(text)
            if (match != null) {
                val raw = match.groupValues[1].replace(".", "").replace(",", "")
                val value = raw.toDoubleOrNull()
                if (value != null && value >= 3000.0 && value <= 500_000.0) {
                    return value
                }
            }
        }
        return null
    }

    private fun extractDistances(texts: List<String>): Pair<Double, Double> {
        val distances = mutableListOf<Double>()
        for (text in texts) {
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
            else -> Pair(0.5, 3.0)
        }
    }

    private fun extractDurationMinutes(texts: List<String>): Int {
        for (text in texts) {
            val match = durationRegex.find(text)
            if (match != null) {
                val hours = match.groupValues[1].toIntOrNull() ?: 0
                val mins = match.groupValues[2].toIntOrNull() ?: 0
                val total = (hours * 60) + mins
                if (total in 1..300) return total
            }
        }
        return 12 // Fallback razonable de duración promedio urbana
    }
}
