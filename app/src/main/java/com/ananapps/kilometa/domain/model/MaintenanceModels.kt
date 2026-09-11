// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/MaintenanceModels.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelos de Dominio para el Módulo de Mantenimiento Preventivo estilo KiloMeta.
// Representa registros de servicio, intervalos recomendados por kilometraje y
// alertas predictivas para evitar averías mecánicas y optimizar costos de flota.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: TIPOS DE SERVICIO Y MANTENIMIENTO PREVENTIVO
// ------------------------------------------------------------------------------
enum class MaintenanceType(
    val displayName: String,
    val defaultIntervalKm: Int,
    val icon: String
) {
    OIL_CHANGE("Cambio de Aceite", 5_000, "🛢️"),
    BRAKES("Frenos y Pastillas", 20_000, "🛑"),
    TIRES("Llantas y Alineación", 40_000, "🛞"),
    FILTERS("Filtros (Aire/Gasolina)", 10_000, "💨"),
    TIMING_BELT("Correa de Repartición", 50_000, "⚙️"),
    SPARK_PLUGS("Bujías y Encendido", 30_000, "⚡"),
    SUSPENSION("Suspensión y Amortiguadores", 40_000, "🔩"),
    GENERAL_CHECKUP("Revisión Tecnomecánica", 10_000, "📋"),
    OTHER("Otro Mantenimiento", 10_000, "🔧")
}

// ------------------------------------------------------------------------------
// BLOQUE 2: NIVELES DE ALERTA SEMAFÓRICA PREDICTIVA
// ------------------------------------------------------------------------------
enum class MaintenanceAlertLevel(val label: String, val badgeIcon: String) {
    OK("Al Día", "🟢"),
    UPCOMING("Próximo", "🟡"),
    OVERDUE("Vencido / Urgente", "🔴")
}

// ------------------------------------------------------------------------------
// BLOQUE 3: REGISTRO HISTÓRICO DE MANTENIMIENTO
// ------------------------------------------------------------------------------
data class MaintenanceRecord(
    val id: String,
    val vehicleId: String,
    val type: MaintenanceType,
    val odometer: Int,
    val costCop: Double,
    val notes: String? = null,
    val nextDueOdometer: Int? = null,
    val nextDueDateMillis: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

// ------------------------------------------------------------------------------
// BLOQUE 4: ALERTA DE SERVICIO Y DIAGNÓSTICO PREDICTIVO
// ------------------------------------------------------------------------------
data class MaintenanceAlert(
    val type: MaintenanceType,
    val lastOdometer: Int,
    val nextDueOdometer: Int,
    val kmRemaining: Int,
    val alertLevel: MaintenanceAlertLevel,
    val lastServiceTimestamp: Long
)
