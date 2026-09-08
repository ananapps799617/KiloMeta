// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/VehicleCatalog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Catálogo estructurado en memoria para marcas y modelos populares en el parque
// automotor colombiano para taxis y vehículos de plataforma tecnológica.
// Regla de Oro: Tipado estricto, 100% inmutable, cero dependencias de Android.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: ESTRUCTURA DE MARCA VEHICULAR
// ------------------------------------------------------------------------------
data class VehicleBrand(
    val name: String,
    val popularModels: List<String>
)

// ------------------------------------------------------------------------------
// BLOQUE 2: CATÁLOGO CANÓNICO PARA COLOMBIA
// ------------------------------------------------------------------------------
object VehicleCatalog {
    const val OTHER_BRAND_KEY = "Otro / Personalizado"

    val brands: List<VehicleBrand> = listOf(
        VehicleBrand(
            name = "Chevrolet",
            popularModels = listOf("Spark", "Spark GT", "Joy", "Sail", "Onix", "Beat", "Tracker")
        ),
        VehicleBrand(
            name = "Renault",
            popularModels = listOf("Kwid", "Logan", "Sandero", "Stepway", "Duster")
        ),
        VehicleBrand(
            name = "Kia",
            popularModels = listOf("Picanto", "Rio", "Soluto", "Sonet")
        ),
        VehicleBrand(
            name = "Hyundai",
            popularModels = listOf("Grand i10", "Accent", "HB20", "Creta")
        ),
        VehicleBrand(
            name = "Nissan",
            popularModels = listOf("March", "Versa", "Sentra", "Kicks")
        ),
        VehicleBrand(
            name = "Toyota",
            popularModels = listOf("Corolla", "Yaris", "Hilux")
        ),
        VehicleBrand(
            name = "Suzuki",
            popularModels = listOf("Swift", "Alto", "Spresso", "Dzire")
        ),
        VehicleBrand(
            name = "BYD",
            popularModels = listOf("Yuan", "Dolphin", "S6", "Qin")
        ),
        VehicleBrand(
            name = "JAC",
            popularModels = listOf("S2", "S3", "E10X", "JS2")
        ),
        VehicleBrand(
            name = "Chery",
            popularModels = listOf("Tiggo", "QQ", "Arrizo")
        ),
        VehicleBrand(
            name = "Changan",
            popularModels = listOf("CS15", "CS35", "E-Star")
        ),
        VehicleBrand(
            name = "DFSK",
            popularModels = listOf("Glory 560", "Glory 580", "K01")
        ),
        VehicleBrand(
            name = OTHER_BRAND_KEY,
            popularModels = emptyList()
        )
    )

    fun getModelsForBrand(brandName: String): List<String> {
        return brands.firstOrNull { it.name.equals(brandName, ignoreCase = true) }?.popularModels ?: emptyList()
    }
}
