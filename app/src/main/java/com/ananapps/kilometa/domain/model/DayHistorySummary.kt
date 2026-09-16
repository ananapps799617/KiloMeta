// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/DayHistorySummary.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio Soberano para el Historial Diario en Cascada (Orden #041).
// Representa el día calendario consolidado con métricas operativas y lista de actividades.
// ==============================================================================

package com.ananapps.kilometa.domain.model

/**
 * Agregado financiero y operativo que modela un día completo en el Historial.
 * Erradica el concepto intermedio de "Jornada" para que cada contenedor sea un DÍA calendario.
 */
data class DayHistorySummary(
    val dateMillis: Long,
    val dateFormatted: String,
    val netProfit: Double,
    val grossProduced: Double,
    val appCommissions: Double,
    val fuelSpent: Double,
    val expensesSpent: Double,
    val startOdometer: Int,
    val endOdometer: Int,
    val distanceKm: Int,
    val startTimeFormatted: String,
    val endTimeFormatted: String,
    val elapsedHoursFormatted: String,
    val ratePerKm: Double,
    val events: List<DayActivityEvent>,
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
    val shiftId: String? = null
)
