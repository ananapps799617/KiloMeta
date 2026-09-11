// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/DayContinuousSummary.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio para el Timeline Continuo y resumen implícito de jornada
// al estilo KiloMeta. Unifica carreras, tanqueos y gastos de la jornada sin obligar
// a una pantalla bloqueante de inicio de turno.
// ==============================================================================

package com.ananapps.kilometa.domain.model

sealed class DayActivityEvent(
    open val id: String,
    open val timestamp: Long,
    open val title: String,
    open val subtitle: String,
    open val amount: Double,
    open val isIncome: Boolean,
    open val odometer: Int? = null
) {
    data class TripItem(val trip: Trip) : DayActivityEvent(
        id = trip.id,
        timestamp = trip.timestamp,
        title = "Carrera ${trip.platformType.name}",
        subtitle = "Cobro: ${trip.paymentMethod.name}",
        amount = trip.netAmount,
        isIncome = true
    )

    data class FuelingItem(val fueling: Fueling) : DayActivityEvent(
        id = fueling.id,
        timestamp = fueling.timestamp,
        title = "Tanqueo ${fueling.fuelType.name}",
        subtitle = "${fueling.volume} gal/m³ · Km ${fueling.odometer}",
        amount = fueling.amountPaid,
        isIncome = false,
        odometer = fueling.odometer
    )

    data class ExpenseItem(val expense: Expense) : DayActivityEvent(
        id = expense.id,
        timestamp = expense.timestamp,
        title = "${expense.concept.icon} ${expense.concept.displayName}",
        subtitle = "Gasto operativo en ruta",
        amount = expense.amount,
        isIncome = false
    )
}

data class DayContinuousSummary(
    val totalNetIncome: Double = 0.0,
    val totalGrossProduced: Double = 0.0,
    val totalFuelSpent: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val pocketNet: Double = 0.0,
    val totalTripsCount: Int = 0,
    val firstEventTimestamp: Long? = null,
    val lastEventTimestamp: Long? = null,
    val firstEventOdometer: Int? = null,
    val lastEventOdometer: Int? = null,
    val elapsedHoursFormatted: String = "0h 0m",
    val distanceTraversedKm: Int = 0,
    val hasActiveShift: Boolean = false,
    val activeShiftId: String? = null,
    val events: List<DayActivityEvent> = emptyList()
)
