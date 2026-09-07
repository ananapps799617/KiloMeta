// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/DriverProfile.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo canónico de Dominio para el perfil del conductor, configuración vehicular
// multi-propulsión (Gasolina, GNV, Bi-Combustible, Diésel, 100% Eléctrico),
// régimen de tenencia (propio vs renta) y metas de producción.
// Regla de Oro: Tipado estricto, 100% inmutable (val), cero dependencias de Android.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: SISTEMAS DE PROPULSIÓN VEHICULAR
// ------------------------------------------------------------------------------
/**
 * Arquitecturas de propulsión soportadas en KiloMeta para taxistas y conductores de app.
 */
enum class PropulsionType(val displayName: String, val defaultFuel: FuelType) {
    GASOLINE_ONLY("Solo Gasolina", FuelType.GASOLINE),
    GAS_ONLY("Solo Gas (GNV)", FuelType.GAS),
    BI_FUEL("Bi-Combustible (GNV + Gasolina)", FuelType.GAS),
    DIESEL("Diésel (ACPM)", FuelType.DIESEL),
    ELECTRIC_100("100% Eléctrico (EV)", FuelType.ELECTRICITY)
}

// ------------------------------------------------------------------------------
// BLOQUE 2: RÉGIMEN DE TENENCIA DEL VEHÍCULO
// ------------------------------------------------------------------------------
/**
 * Modalidad de posesión que impacta la estructura de costos (entrega de turno vs amortización).
 */
enum class VehicleTenure(val displayName: String) {
    OWNED("Vehículo Propio"),
    RENTED("Alquilado / En Renta")
}

// ------------------------------------------------------------------------------
// BLOQUE 3: PERIODICIDAD DE LA CUOTA OBJETIVO
// ------------------------------------------------------------------------------
/**
 * Ciclo temporal de fijación y evaluación de la meta financiera.
 */
enum class QuotaPeriodicity(val displayName: String) {
    DAILY("Diaria"),
    WEEKLY("Semanal"),
    MONTHLY("Mensual")
}

// ------------------------------------------------------------------------------
// BLOQUE 4: MODELO CANÓNICO DE PERFIL DE CONDUCTOR Y VEHÍCULO
// ------------------------------------------------------------------------------
/**
 * Datos del conductor, vehículo, sistema motriz y parámetros operacionales.
 *
 * @property driverName Nombre del conductor (ej. "Andrés López").
 * @property vehicleName Referencia o modelo del vehículo (ej. "Chevrolet Joy / Spark GT").
 * @property vehiclePlate Placa del vehículo registrada (ej. "WMB-789").
 * @property propulsionType Tipo de motorización y combustible principal.
 * @property vehicleTenure Régimen de propiedad o alquiler.
 * @property targetQuotaAmount Monto objetivo de cuota según la periodicidad elegida ($ COP).
 * @property quotaPeriodicity Ciclo de liquidación de cuota (diario, semanal, mensual).
 * @property workingDaysPerWeek Días laborales a la semana (1 a 7).
 * @property activePlatforms Plataformas de intermediación activadas por el conductor.
 * @property updatedAt Marca de tiempo en milisegundos de la última actualización del perfil.
 */
data class DriverProfile(
    val driverName: String = "",
    val vehicleName: String = "Chevrolet Joy / Spark GT",
    val vehiclePlate: String = "WMB-789",
    val propulsionType: PropulsionType = PropulsionType.BI_FUEL,
    val vehicleTenure: VehicleTenure = VehicleTenure.OWNED,
    val targetQuotaAmount: Double = 500_000.0,
    val quotaPeriodicity: QuotaPeriodicity = QuotaPeriodicity.WEEKLY,
    val workingDaysPerWeek: Int = 6,
    val activePlatforms: Set<PlatformType> = setOf(
        PlatformType.UBER,
        PlatformType.DIDI,
        PlatformType.INDRIVE
    ),
    val updatedAt: Long = System.currentTimeMillis()
)
