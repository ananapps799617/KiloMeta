// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/mapper/ExpenseMapper.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Mapeadores de extensión bidireccionales entre ExpenseEntity y Expense (Dominio).
// Transforma conceptos de gasto operativo con tipado seguro.
// ==============================================================================

package com.ananapps.kilometa.data.mapper

import com.ananapps.kilometa.data.local.entity.ExpenseEntity
import com.ananapps.kilometa.domain.model.Expense
import com.ananapps.kilometa.domain.model.ExpenseConcept

// ------------------------------------------------------------------------------
// BLOQUE 1: MAPEO ENTIDAD <-> DOMINIO DE GASTOS
// ------------------------------------------------------------------------------
fun ExpenseEntity.toDomain(): Expense {
    val conceptEnum = try {
        ExpenseConcept.valueOf(this.concept)
    } catch (_: IllegalArgumentException) {
        ExpenseConcept.OTHER
    }

    return Expense(
        id = this.id,
        shiftId = this.shiftId,
        concept = conceptEnum,
        amount = this.amount,
        timestamp = this.timestamp
    )
}

fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = this.id,
        shiftId = this.shiftId,
        concept = this.concept.name,
        amount = this.amount,
        timestamp = this.timestamp
    )
}

fun List<ExpenseEntity>.toDomain(): List<Expense> = map { it.toDomain() }
