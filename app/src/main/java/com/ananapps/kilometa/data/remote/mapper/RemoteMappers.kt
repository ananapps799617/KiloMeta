// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/remote/mapper/RemoteMappers.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Mapeadores de extensión bidireccionales entre entidades de persistencia local (Room)
// y DTOs de transporte en la nube (Firestore). Asegura tipado estricto e inmutabilidad.
// ==============================================================================

package com.ananapps.kilometa.data.remote.mapper

import com.ananapps.kilometa.data.local.entity.ExpenseEntity
import com.ananapps.kilometa.data.local.entity.FuelingEntity
import com.ananapps.kilometa.data.local.entity.ShiftEntity
import com.ananapps.kilometa.data.local.entity.TripEntity
import com.ananapps.kilometa.data.remote.dto.ExpenseDto
import com.ananapps.kilometa.data.remote.dto.FuelingDto
import com.ananapps.kilometa.data.remote.dto.ShiftDto
import com.ananapps.kilometa.data.remote.dto.TripDto

// ------------------------------------------------------------------------------
// BLOQUE 1: MAPEO BIDIRECCIONAL DE JORNADAS (SHIFTS)
// ------------------------------------------------------------------------------
fun ShiftEntity.toDto(
    updatedAt: Long = System.currentTimeMillis(),
    deleted: Boolean = false
): ShiftDto = ShiftDto(
    id = this.id,
    startTime = this.startTime,
    endTime = this.endTime,
    startOdometer = this.startOdometer,
    endOdometer = this.endOdometer,
    dailyQuotaTarget = this.dailyQuotaTarget,
    status = this.status,
    notes = this.notes,
    updatedAt = updatedAt,
    deleted = deleted
)

fun ShiftDto.toEntity(): ShiftEntity = ShiftEntity(
    id = this.id,
    startTime = this.startTime,
    endTime = this.endTime,
    startOdometer = this.startOdometer,
    endOdometer = this.endOdometer,
    dailyQuotaTarget = this.dailyQuotaTarget,
    status = this.status,
    notes = this.notes
)

fun List<ShiftEntity>.toShiftDtoList(): List<ShiftDto> = map { it.toDto() }
fun List<ShiftDto>.toShiftEntityList(): List<ShiftEntity> = map { it.toEntity() }

// ------------------------------------------------------------------------------
// BLOQUE 2: MAPEO BIDIRECCIONAL DE CARRERAS (TRIPS)
// ------------------------------------------------------------------------------
fun TripEntity.toDto(
    updatedAt: Long = System.currentTimeMillis(),
    deleted: Boolean = false
): TripDto = TripDto(
    id = this.id,
    shiftId = this.shiftId,
    platformName = this.platformName,
    grossAmount = this.grossAmount,
    commissionAmount = this.commissionAmount,
    netAmount = this.netAmount,
    paymentMethod = this.paymentMethod,
    timestamp = this.timestamp,
    updatedAt = updatedAt,
    deleted = deleted
)

fun TripDto.toEntity(): TripEntity = TripEntity(
    id = this.id,
    shiftId = this.shiftId,
    platformName = this.platformName,
    grossAmount = this.grossAmount,
    commissionAmount = this.commissionAmount,
    netAmount = this.netAmount,
    paymentMethod = this.paymentMethod,
    timestamp = this.timestamp
)

fun List<TripEntity>.toTripDtoList(): List<TripDto> = map { it.toDto() }
fun List<TripDto>.toTripEntityList(): List<TripEntity> = map { it.toEntity() }

// ------------------------------------------------------------------------------
// BLOQUE 3: MAPEO BIDIRECCIONAL DE ABASTECIMIENTOS (FUELINGS)
// ------------------------------------------------------------------------------
fun FuelingEntity.toDto(
    updatedAt: Long = System.currentTimeMillis(),
    deleted: Boolean = false
): FuelingDto = FuelingDto(
    id = this.id,
    shiftId = this.shiftId,
    fuelType = this.fuelType,
    amountPaid = this.amountPaid,
    volume = this.volume,
    odometer = this.odometer,
    timestamp = this.timestamp,
    updatedAt = updatedAt,
    deleted = deleted
)

fun FuelingDto.toEntity(): FuelingEntity = FuelingEntity(
    id = this.id,
    shiftId = this.shiftId,
    fuelType = this.fuelType,
    amountPaid = this.amountPaid,
    volume = this.volume,
    odometer = this.odometer,
    timestamp = this.timestamp
)

fun List<FuelingEntity>.toFuelingDtoList(): List<FuelingDto> = map { it.toDto() }
fun List<FuelingDto>.toFuelingEntityList(): List<FuelingEntity> = map { it.toEntity() }

// ------------------------------------------------------------------------------
// BLOQUE 4: MAPEO BIDIRECCIONAL DE GASTOS OPERATIVOS (EXPENSES)
// ------------------------------------------------------------------------------
fun ExpenseEntity.toDto(
    updatedAt: Long = System.currentTimeMillis(),
    deleted: Boolean = false
): ExpenseDto = ExpenseDto(
    id = this.id,
    shiftId = this.shiftId,
    concept = this.concept,
    amount = this.amount,
    timestamp = this.timestamp,
    updatedAt = updatedAt,
    deleted = deleted
)

fun ExpenseDto.toEntity(): ExpenseEntity = ExpenseEntity(
    id = this.id,
    shiftId = this.shiftId,
    concept = this.concept,
    amount = this.amount,
    timestamp = this.timestamp
)

fun List<ExpenseEntity>.toExpenseDtoList(): List<ExpenseDto> = map { it.toDto() }
fun List<ExpenseDto>.toExpenseEntityList(): List<ExpenseEntity> = map { it.toEntity() }
