// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/Expense.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio que representa un gasto operativo incidental durante el turno.
// Permite clasificar egresos como lavado, peajes, alimentos u otros imprevistos.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO INMUTABLE DE GASTO OPERATIVO
// ------------------------------------------------------------------------------
/**
 * Modela un egreso o gasto en ruta realizado por el conductor.
 *
 * @property id Identificador único UUID v4 del gasto.
 * @property shiftId Turno de trabajo al que se imputa el gasto.
 * @property concept Categoría o concepto del gasto (WASH, TOLL, FOOD, OTHER).
 * @property amount Valor monetario del gasto en COP.
 * @property timestamp Momento exacto de registro en milisegundos Unix.
 */
data class Expense(
    val id: String,
    val shiftId: String,
    val concept: ExpenseConcept,
    val amount: Double,
    val timestamp: Long,
    val paymentSource: WalletType = WalletType.CASH_PHYSICAL
)
