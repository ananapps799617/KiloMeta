// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/VehicleCatalog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Catálogo estructurado de marcas y modelos populares en Colombia para carros,
// taxis y motocicletas. Define tipologías de vehículo y sistemas de tanques.
// ==============================================================================

package com.ananapps.kilometa.domain.model

enum class VehicleType(val displayName: String, val icon: String) {
    CAR("Carro / Taxi", "🚗"),
    MOTORCYCLE("Motocicleta", "🏍️")
}

enum class TankSystem(val displayName: String, val description: String) {
    SINGLE_TANK("Un Solo Tanque", "Gasolina pura o gas exclusivo"),
    DUAL_TANK("Dos Tanques (Bi-Combustible)", "GNV principal + Gasolina de reserva")
}

data class VehicleBrand(
    val name: String,
    val type: VehicleType,
    val popularModels: List<String>
)

object VehicleCatalog {
    const val OTHER_BRAND_KEY = "Otro / Personalizado"

    val brands: List<VehicleBrand> = listOf(
        // Carros y Taxis Populares en Colombia
        VehicleBrand("Chevrolet", VehicleType.CAR, listOf("Spark", "Spark GT", "Joy", "Sail", "Onix", "Beat", "Tracker")),
        VehicleBrand("Renault", VehicleType.CAR, listOf("Kwid", "Logan", "Sandero", "Stepway", "Duster")),
        VehicleBrand("Kia", VehicleType.CAR, listOf("Picanto", "Rio", "Soluto", "Sonet")),
        VehicleBrand("Hyundai", VehicleType.CAR, listOf("Grand i10", "Accent", "HB20", "Creta")),
        VehicleBrand("Nissan", VehicleType.CAR, listOf("March", "Versa", "Sentra", "Kicks")),
        VehicleBrand("Toyota", VehicleType.CAR, listOf("Corolla", "Yaris", "Hilux")),
        VehicleBrand("Suzuki", VehicleType.CAR, listOf("Swift", "Alto", "Spresso", "Dzire")),
        VehicleBrand("Volkswagen", VehicleType.CAR, listOf("Gol", "Voyage", "Polo", "Virtus")),
        VehicleBrand("BYD", VehicleType.CAR, listOf("Yuan", "Dolphin", "S6", "Qin")),
        VehicleBrand("JAC", VehicleType.CAR, listOf("S2", "S3", "E10X", "JS2")),
        VehicleBrand("Chery", VehicleType.CAR, listOf("Tiggo", "QQ", "Arrizo")),
        VehicleBrand("Changan", VehicleType.CAR, listOf("CS15", "CS35", "E-Star")),
        VehicleBrand("DFSK", VehicleType.CAR, listOf("Glory 560", "Glory 580", "K01")),

        // Motos Populares en Colombia
        VehicleBrand("Bajaj", VehicleType.MOTORCYCLE, listOf("Pulsar NS 200", "Pulsar 150", "Boxer CT 100", "Discover 125", "Dominar 400")),
        VehicleBrand("Yamaha", VehicleType.MOTORCYCLE, listOf("NMAX 155", "FZ 25", "FZ-S", "Crypton FI", "XTZ 125", "XTZ 150")),
        VehicleBrand("AKT", VehicleType.MOTORCYCLE, listOf("NKD 125", "CR4 125", "CR4 162", "Special 100", "TT DS 200")),
        VehicleBrand("Honda", VehicleType.MOTORCYCLE, listOf("CB 125F", "CB 160F", "Wave 110", "XR 150L", "Dio 110")),
        VehicleBrand("TVS", VehicleType.MOTORCYCLE, listOf("Raider 125 Fi", "Apache RTR 160 4V", "Apache RTR 200", "Stryker 125", "Ntorq 125")),
        VehicleBrand("Suzuki Moto", VehicleType.MOTORCYCLE, listOf("Gixxer 150", "GN 125", "AX4", "Burgman FI", "DR 150")),
        VehicleBrand("Hero", VehicleType.MOTORCYCLE, listOf("Eco 100", "Splendor", "Hunk 160R", "Xpulse 200")),

        // Opción Libre
        VehicleBrand(OTHER_BRAND_KEY, VehicleType.CAR, emptyList())
    )

    val CAR_BRANDS: List<String> = brands.filter { it.type == VehicleType.CAR && it.name != OTHER_BRAND_KEY }.map { it.name }
    val MOTO_BRANDS: List<String> = brands.filter { it.type == VehicleType.MOTORCYCLE }.map { it.name }

    fun getBrandsFor(type: VehicleType): List<String> {
        return brands.filter { it.type == type && it.name != OTHER_BRAND_KEY }.map { it.name }
    }

    fun getModelsForBrand(brandName: String): List<String> {
        return brands.firstOrNull { it.name.equals(brandName, ignoreCase = true) }?.popularModels ?: emptyList()
    }
}
