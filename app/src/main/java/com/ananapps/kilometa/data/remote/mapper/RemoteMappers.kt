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
import com.ananapps.kilometa.data.local.entity.MaintenanceEntity
import com.ananapps.kilometa.data.local.entity.ShiftEntity
import com.ananapps.kilometa.data.local.entity.TripEntity
import com.ananapps.kilometa.data.local.entity.WalletTransferEntity
import com.ananapps.kilometa.data.remote.dto.ExpenseDto
import com.ananapps.kilometa.data.remote.dto.FuelingDto
import com.ananapps.kilometa.data.remote.dto.MaintenanceDto
import com.ananapps.kilometa.data.remote.dto.ShiftDto
import com.ananapps.kilometa.data.remote.dto.TripDto
import com.ananapps.kilometa.data.remote.dto.WalletTransferDto

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
    paymentDestination = this.paymentDestination,
    timestamp = this.timestamp,
    startOdometer = this.startOdometer,
    endOdometer = this.endOdometer,
    platformPromoDiscount = this.platformPromoDiscount,
    distanceKm = this.distanceKm,
    durationMinutes = this.durationMinutes,
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
    paymentDestination = if (this.paymentDestination.isNotBlank()) this.paymentDestination else "CASH_IN_HAND",
    timestamp = this.timestamp,
    startOdometer = this.startOdometer,
    endOdometer = this.endOdometer,
    platformPromoDiscount = this.platformPromoDiscount,
    distanceKm = this.distanceKm,
    durationMinutes = this.durationMinutes
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
    paymentSource = this.paymentSource,
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
    timestamp = this.timestamp,
    paymentSource = this.paymentSource
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
    paymentSource = this.paymentSource,
    updatedAt = updatedAt,
    deleted = deleted
)

fun ExpenseDto.toEntity(): ExpenseEntity = ExpenseEntity(
    id = this.id,
    shiftId = this.shiftId,
    concept = this.concept,
    amount = this.amount,
    timestamp = this.timestamp,
    paymentSource = this.paymentSource
)

fun List<ExpenseEntity>.toExpenseDtoList(): List<ExpenseDto> = map { it.toDto() }
fun List<ExpenseDto>.toExpenseEntityList(): List<ExpenseEntity> = map { it.toEntity() }

// ------------------------------------------------------------------------------
// BLOQUE 5: MAPEO BIDIRECCIONAL DE TRANSFERENCIAS (WALLET TRANSFERS)
// ------------------------------------------------------------------------------
fun WalletTransferEntity.toDto(
    updatedAt: Long = System.currentTimeMillis(),
    deleted: Boolean = false
): WalletTransferDto = WalletTransferDto(
    id = this.id,
    timestamp = this.timestamp,
    sourceWallet = this.sourceWallet,
    destinationWallet = this.destinationWallet,
    amount = this.amount,
    transferType = this.transferType,
    notes = this.notes,
    updatedAt = updatedAt,
    deleted = deleted
)

fun WalletTransferDto.toEntity(): WalletTransferEntity = WalletTransferEntity(
    id = this.id,
    timestamp = this.timestamp,
    sourceWallet = this.sourceWallet,
    destinationWallet = this.destinationWallet,
    amount = this.amount,
    transferType = this.transferType,
    notes = this.notes
)

fun List<WalletTransferEntity>.toTransferDtoList(): List<WalletTransferDto> = map { it.toDto() }
fun List<WalletTransferDto>.toTransferEntityList(): List<WalletTransferEntity> = map { it.toEntity() }

// ------------------------------------------------------------------------------
// BLOQUE 6: MAPEO BIDIRECCIONAL DE MANTENIMIENTOS (MAINTENANCES)
// ------------------------------------------------------------------------------
fun MaintenanceEntity.toDto(
    updatedAt: Long = System.currentTimeMillis(),
    deleted: Boolean = false
): MaintenanceDto = MaintenanceDto(
    id = this.id,
    vehicleId = this.vehicleId,
    maintenanceType = this.maintenanceType,
    odometer = this.odometer,
    costCop = this.costCop,
    notes = this.notes,
    nextDueOdometer = this.nextDueOdometer,
    nextDueDateMillis = this.nextDueDateMillis,
    timestamp = this.timestamp,
    updatedAt = updatedAt,
    deleted = deleted
)

fun MaintenanceDto.toEntity(): MaintenanceEntity = MaintenanceEntity(
    id = this.id,
    vehicleId = this.vehicleId,
    maintenanceType = this.maintenanceType,
    odometer = this.odometer,
    costCop = this.costCop,
    notes = this.notes,
    nextDueOdometer = this.nextDueOdometer,
    nextDueDateMillis = this.nextDueDateMillis,
    timestamp = this.timestamp
)

fun List<MaintenanceEntity>.toMaintenanceDtoList(): List<MaintenanceDto> = map { it.toDto() }
fun List<MaintenanceDto>.toMaintenanceEntityList(): List<MaintenanceEntity> = map { it.toEntity() }
