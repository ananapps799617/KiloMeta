// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/TripOfferEvaluation.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de evaluación financiera en milisegundos con semáforo de rentabilidad (Orden #047).
// Permite al conductor decidir en 2 segundos si una carrera genera ganancia o pérdida.
// ==============================================================================

package com.ananapps.kilometa.domain.model

enum class OfferSemaphoreTier(val label: String) {
    GREEN_PROFITABLE("RENTABLE"),
    YELLOW_BREAK_EVEN("EQUILIBRIO"),
    RED_UNFAVORABLE("NO RENTABLE")
}

data class TripOfferEvaluation(
    val offer: TripOffer,
    val totalDistanceKm: Double,
    val estimatedFuelCost: Double,
    val estimatedPlatformFee: Double,
    val estimatedNetEarnings: Double,
    val earningsPerKm: Double,
    val earningsPerHour: Double,
    val semaphoreTier: OfferSemaphoreTier,
    val alerts: List<String> = emptyList()
)
