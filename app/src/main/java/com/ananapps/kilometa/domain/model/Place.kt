// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/Place.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo canónico de Dominio para puntos de interés frecuentes del conductor
// (Bombas de gas, gasolineras, talleres de confianza, lavaderos y restaurantes).
// ==============================================================================

package com.ananapps.kilometa.domain.model

enum class PlaceCategory(val displayName: String, val icon: String) {
    GAS_STATION_GNV("Bomba GNV", "⛽"),
    GAS_STATION_GASOLINE("Gasolinera", "⛽"),
    WORKSHOP_MECHANIC("Taller Mecánico", "🔧"),
    CAR_WASH("Lavadero", "🧼"),
    RESTAURANT("Restaurante", "🍽️"),
    HOME("Casa / Parqueadero", "🏠"),
    OTHER("Otro lugar", "📍")
}

data class Place(
    val id: String,
    val name: String,
    val category: PlaceCategory = PlaceCategory.GAS_STATION_GNV,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String? = null,
    val gnvPricePerM3: Double? = null,
    val gasolinePricePerGal: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)
