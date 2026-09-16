// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/VehicleProfile.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo canónico de Dominio para el vehículo de trabajo como activo de producción.
// Encapsula especificaciones mecánicas, régimen de tenencia y metas de cuota.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO CANÓNICO DE PERFIL VEHICULAR
// ------------------------------------------------------------------------------
data class VehicleProfile(
    val id: String = "primary_vehicle",
    val vehicleType: VehicleType = VehicleType.PARTICULAR,
    val vehiclePlate: String = "WMB-789",
    val brand: String = "Chevrolet",
    val model: String = "Joy",
    val year: Int = 2022,
    val tankSystem: TankSystem = TankSystem.DUAL_TANK,
    val propulsionType: PropulsionType = PropulsionType.BI_FUEL,
    val tenureType: VehicleTenure = VehicleTenure.OWNED,
    val quotaPeriodicity: QuotaPeriodicity = QuotaPeriodicity.WEEKLY,
    val quotaAmount: Double = 500_000.0,
    val workDaysWeek: Int = 6,
    val quotaCutoffDay: Int = 2, // 2 = Martes (estándar gremial en Colombia)
    val quotaCutoffHour: Int = 23, // 23:59 hrs (fin de jornada del día de corte)
    val updatedAt: Long = System.currentTimeMillis()
)
