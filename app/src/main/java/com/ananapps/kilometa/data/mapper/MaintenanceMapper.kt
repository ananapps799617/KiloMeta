// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/mapper/MaintenanceMapper.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Mapeador de datos bidireccional entre MaintenanceEntity (Room) y MaintenanceRecord (Dominio).
// Asegura tipado estricto y manejo de excepciones ante fallbacks de enumeraciones.
// ==============================================================================

package com.ananapps.kilometa.data.mapper

import com.ananapps.kilometa.data.local.entity.MaintenanceEntity
import com.ananapps.kilometa.domain.model.MaintenanceRecord
import com.ananapps.kilometa.domain.model.MaintenanceType

// ------------------------------------------------------------------------------
// BLOQUE 1: MAPEO BIDIRECCIONAL ENTIDAD <-> DOMINIO
// ------------------------------------------------------------------------------
fun MaintenanceEntity.toDomain(): MaintenanceRecord {
    val domainType = try {
        MaintenanceType.valueOf(this.maintenanceType)
    } catch (_: IllegalArgumentException) {
        MaintenanceType.OTHER
    }

    return MaintenanceRecord(
        id = this.id,
        vehicleId = this.vehicleId,
        type = domainType,
        odometer = this.odometer,
        costCop = this.costCop,
        notes = this.notes,
        nextDueOdometer = this.nextDueOdometer,
        nextDueDateMillis = this.nextDueDateMillis,
        timestamp = this.timestamp
    )
}

fun MaintenanceRecord.toEntity(): MaintenanceEntity {
    return MaintenanceEntity(
        id = this.id,
        vehicleId = this.vehicleId,
        maintenanceType = this.type.name,
        odometer = this.odometer,
        costCop = this.costCop,
        notes = this.notes,
        nextDueOdometer = this.nextDueOdometer,
        nextDueDateMillis = this.nextDueDateMillis,
        timestamp = this.timestamp
    )
}

fun List<MaintenanceEntity>.toDomain(): List<MaintenanceRecord> = map { it.toDomain() }
