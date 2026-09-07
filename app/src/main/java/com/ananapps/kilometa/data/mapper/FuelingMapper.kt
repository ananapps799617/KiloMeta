// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/mapper/FuelingMapper.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Mapeadores de extensión bidireccionales entre FuelingEntity y Fueling (Dominio).
// Procesa adecuadamente el tipo de combustible bi-modal (GNV / Gasolina).
// ==============================================================================

package com.ananapps.kilometa.data.mapper

import com.ananapps.kilometa.data.local.entity.FuelingEntity
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.Fueling

// ------------------------------------------------------------------------------
// BLOQUE 1: MAPEO ENTIDAD <-> DOMINIO DE COMBUSTIBLE
// ------------------------------------------------------------------------------
fun FuelingEntity.toDomain(): Fueling {
    val fuel = try {
        FuelType.valueOf(this.fuelType)
    } catch (_: IllegalArgumentException) {
        FuelType.GAS
    }

    return Fueling(
        id = this.id,
        shiftId = this.shiftId,
        fuelType = fuel,
        amountPaid = this.amountPaid,
        volume = this.volume,
        odometer = this.odometer,
        timestamp = this.timestamp
    )
}

fun Fueling.toEntity(): FuelingEntity {
    return FuelingEntity(
        id = this.id,
        shiftId = this.shiftId,
        fuelType = this.fuelType.name,
        amountPaid = this.amountPaid,
        volume = this.volume,
        odometer = this.odometer,
        timestamp = this.timestamp
    )
}

fun List<FuelingEntity>.toDomain(): List<Fueling> = map { it.toDomain() }
