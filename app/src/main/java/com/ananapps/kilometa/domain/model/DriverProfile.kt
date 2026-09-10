// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/DriverProfile.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo canónico de Dominio para la identidad humana del conductor (PII protegida),
// régimen de propulsión y plataformas tecnológicas habilitadas.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: ENUMERACIONES ASOCIADAS AL PERFIL
// ------------------------------------------------------------------------------
enum class Gender(val displayName: String) {
    MALE("Masculino"),
    FEMALE("Femenino"),
    PREFER_NOT_TO_SAY("Prefiero no decir")
}

enum class PropulsionType(val displayName: String, val defaultFuel: FuelType) {
    GASOLINE_ONLY("Solo Gasolina", FuelType.GASOLINE),
    GAS_ONLY("Solo Gas (GNV)", FuelType.GAS),
    BI_FUEL("Bi-Combustible (GNV + Gasolina)", FuelType.GAS),
    DIESEL("Diésel (ACPM)", FuelType.DIESEL),
    ELECTRIC_100("100% Eléctrico (EV)", FuelType.ELECTRICITY)
}

enum class VehicleTenure(val displayName: String) {
    OWNED("Vehículo Propio"),
    RENTED("Alquilado / En Renta")
}

enum class QuotaPeriodicity(val displayName: String) {
    DAILY("Diaria"),
    WEEKLY("Semanal"),
    MONTHLY("Mensual")
}

// ------------------------------------------------------------------------------
// BLOQUE 2: MODELO CANÓNICO DE CONDUCTOR
// ------------------------------------------------------------------------------
data class DriverProfile(
    val id: String = "default_driver",
    val driverName: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: Gender = Gender.PREFER_NOT_TO_SAY,
    val activeVehicleId: String = "primary_vehicle",
    // Atributos de compatibilidad y conveniencia operativa directa en cabina
    val vehiclePlate: String = "WMB-789",
    val propulsionType: PropulsionType = PropulsionType.BI_FUEL,
    val activePlatforms: Set<PlatformType> = setOf(
        PlatformType.UBER,
        PlatformType.DIDI,
        PlatformType.INDRIVE
    ),
    val isProfileCompleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isReadyForCabina: Boolean
        get() = isProfileCompleted && driverName.isNotBlank() && vehiclePlate.isNotBlank()
}
