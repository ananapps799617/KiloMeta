// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/local/relation/ShiftWithDetails.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Objeto compuesto (POJO) para Room que modela la relación 1:N entre un Turno
// y sus carreras, recargas y gastos. Permite lecturas atómicas con @Transaction.
// ==============================================================================

package com.ananapps.kilometa.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.ananapps.kilometa.data.local.entity.ExpenseEntity
import com.ananapps.kilometa.data.local.entity.FuelingEntity
import com.ananapps.kilometa.data.local.entity.ShiftEntity
import com.ananapps.kilometa.data.local.entity.TripEntity

// ------------------------------------------------------------------------------
// BLOQUE 1: AGREGADO RELACIONAL COMPLETO DEL TURNO
// ------------------------------------------------------------------------------
/**
 * Agrega un registro de turno con todas sus colecciones hijas asociadas.
 * Garantiza integridad de lectura en transacciones atómicas.
 */
data class ShiftWithDetails(
    @Embedded
    val shift: ShiftEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "shift_id"
    )
    val trips: List<TripEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "shift_id"
    )
    val fuelings: List<FuelingEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "shift_id"
    )
    val expenses: List<ExpenseEntity>
)
