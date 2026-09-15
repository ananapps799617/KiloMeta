// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/VehicleCatalog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Catálogo estructurado de marcas y modelos populares en Colombia para particulares,
// taxis y motocicletas. Define tipologías tripartitas y sistemas de tanques.
// ==============================================================================

package com.ananapps.kilometa.domain.model

enum class VehicleType(val displayName: String, val icon: String) {
    PARTICULAR("Carro Particular", "🚗"),
    TAXI("Taxi", "🚕"),
    MOTORCYCLE("Motocicleta", "🏍️")
}

enum class TankSystem(val displayName: String, val description: String) {
    SINGLE_TANK("Un Solo Tanque", "Gasolina pura o gas exclusivo"),
    DUAL_TANK("Dos Tanques (Bi-Combustible)", "GNV principal + Gasolina de reserva")
}

data class VehicleBrand(
    val name: String,
    val types: Set<VehicleType>,
    val popularModels: List<String>
)

object VehicleCatalog {
    const val OTHER_BRAND_KEY = "Otro / Personalizado"

    private val CAR_TYPES = setOf(VehicleType.PARTICULAR, VehicleType.TAXI)
    private val MOTO_TYPES = setOf(VehicleType.MOTORCYCLE)

    val brands: List<VehicleBrand> = listOf(
        // Marcas Clave en Colombia (Carros Particulares y Taxis)
        VehicleBrand("BYD", CAR_TYPES, listOf("S6", "Song Plus", "Yuan Plus", "Yuan Pro", "Dolphin", "Tang", "Qin Plus", "Seagull")),
        VehicleBrand("Chevrolet", CAR_TYPES, listOf("Spark", "Spark GT", "Joy", "Sail", "Onix", "Beat", "Tracker", "Aveo", "Optra")),
        VehicleBrand("Renault", CAR_TYPES, listOf("Kwid", "Logan", "Sandero", "Stepway", "Duster", "Symbol", "Captur", "Clio")),
        VehicleBrand("Kia", CAR_TYPES, listOf("Picanto", "Rio", "Soluto", "Sonet", "Cerato", "Sportage", "Grand EKR")),
        VehicleBrand("Hyundai", CAR_TYPES, listOf("Grand i10", "Accent", "HB20", "Creta", "Tucson", "Atos", "City Taxi")),
        VehicleBrand("Toyota", CAR_TYPES, listOf("Corolla", "Yaris", "Hilux", "Prado", "RAV4", "Etios", "Fortuner")),
        VehicleBrand("Suzuki", CAR_TYPES, listOf("Swift", "Alto", "Spresso", "Dzire", "Baleno", "Vitara", "Celerio")),
        VehicleBrand("Nissan", CAR_TYPES, listOf("March", "Versa", "Sentra", "Kicks", "Tiida", "Frontier")),
        VehicleBrand("Mazda", CAR_TYPES, listOf("Mazda 2", "Mazda 3", "CX-30", "CX-5", "Mazda 6")),
        VehicleBrand("Volkswagen", CAR_TYPES, listOf("Gol", "Voyage", "Polo", "Virtus", "T-Cross", "Jetta")),
        VehicleBrand("JAC", CAR_TYPES, listOf("S2", "S3", "E10X", "JS2", "JS4", "Taxi Star")),
        VehicleBrand("Chery", CAR_TYPES, listOf("Tiggo", "QQ", "Arrizo", "Grand City Taxi")),
        VehicleBrand("Changan", CAR_TYPES, listOf("CS15", "CS35", "E-Star", "Benni")),
        VehicleBrand("DFSK", CAR_TYPES, listOf("Glory 560", "Glory 580", "K01", "C35")),
        VehicleBrand("Ford", CAR_TYPES, listOf("Fiesta", "EcoSport", "Focus", "Escape", "Ranger")),

        // Motocicletas Populares en Colombia
        VehicleBrand("AKT", MOTO_TYPES, listOf("NKD 125", "CR4 125", "CR4 162", "Special 100", "TT DS 200", "Dynamic Pro")),
        VehicleBrand("Bajaj", MOTO_TYPES, listOf("Pulsar NS 200", "Pulsar 150", "Boxer CT 100", "Discover 125", "Dominar 400", "Platina 100")),
        VehicleBrand("TVS", MOTO_TYPES, listOf("Raider 125", "Apache RTR 160", "Apache RTR 200", "Stryker 125", "Ntorq 125", "Sport 100")),
        VehicleBrand("Yamaha", MOTO_TYPES, listOf("NMAX 155", "FZ 25", "FZ-S", "Crypton FI", "XTZ 125", "XTZ 150", "YBR 125")),
        VehicleBrand("Honda", MOTO_TYPES, listOf("CB 125F", "CB 160F", "Wave 110", "XR 150L", "Dio 110", "Navi", "XBlade")),
        VehicleBrand("Suzuki Moto", MOTO_TYPES, listOf("Gixxer 150", "GN 125", "AX4", "Burgman FI", "DR 150", "Viva R")),
        VehicleBrand("Hero", MOTO_TYPES, listOf("Eco 100", "Splendor", "Hunk 160R", "Xpulse 200", "Ignitor 125")),

        // Contingencia
        VehicleBrand(OTHER_BRAND_KEY, CAR_TYPES + MOTO_TYPES, emptyList())
    )

    fun getBrandsFor(type: VehicleType): List<String> {
        return brands
            .filter { type in it.types && it.name != OTHER_BRAND_KEY }
            .map { it.name }
            .sorted()
    }

    fun searchBrands(query: String, type: VehicleType): List<String> {
        val base = getBrandsFor(type)
        if (query.isBlank()) return base
        val trimmed = query.trim()
        return base.filter { it.contains(trimmed, ignoreCase = true) }
    }

    fun getModelsForBrand(brandName: String): List<String> {
        return brands.firstOrNull { it.name.equals(brandName, ignoreCase = true) }?.popularModels ?: emptyList()
    }
}
