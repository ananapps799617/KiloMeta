// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/mapper/ShiftMapper.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Mapeadores de extensión bidireccionales entre ShiftEntity y Shift (Dominio).
// Incluye la función de consolidación matemática para transformar ShiftWithDetails
// en ShiftFinancialSummary con tipado estricto e inmutable.
// ==============================================================================

package com.ananapps.kilometa.data.mapper

import com.ananapps.kilometa.data.local.entity.ShiftEntity
import com.ananapps.kilometa.data.local.relation.ShiftWithDetails
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.Shift
import com.ananapps.kilometa.domain.model.ShiftFinancialSummary
import com.ananapps.kilometa.domain.model.ShiftStatus

// ------------------------------------------------------------------------------
// BLOQUE 1: MAPEO BIDIRECCIONAL ENTIDAD <-> DOMINIO
// ------------------------------------------------------------------------------
fun ShiftEntity.toDomain(): Shift {
    val domainStatus = try {
        ShiftStatus.valueOf(this.status)
    } catch (_: IllegalArgumentException) {
        ShiftStatus.ACTIVE
    }

    return Shift(
        id = this.id,
        startTime = this.startTime,
        endTime = this.endTime,
        startOdometer = this.startOdometer,
        endOdometer = this.endOdometer,
        dailyQuotaTarget = this.dailyQuotaTarget,
        status = domainStatus,
        notes = this.notes
    )
}

fun Shift.toEntity(): ShiftEntity {
    return ShiftEntity(
        id = this.id,
        startTime = this.startTime,
        endTime = this.endTime,
        startOdometer = this.startOdometer,
        endOdometer = this.endOdometer,
        dailyQuotaTarget = this.dailyQuotaTarget,
        status = this.status.name,
        notes = this.notes
    )
}

fun List<ShiftEntity>.toDomain(): List<Shift> = map { it.toDomain() }

// ------------------------------------------------------------------------------
// BLOQUE 2: CONSOLIDACIÓN FINANCIERA DESDE AGREGADO RELACIONAL
// ------------------------------------------------------------------------------
fun ShiftWithDetails.toFinancialSummary(): ShiftFinancialSummary {
    val domainStatus = try {
        ShiftStatus.valueOf(shift.status)
    } catch (_: IllegalArgumentException) {
        ShiftStatus.ACTIVE
    }

    val totalKm = if (shift.endOdometer != null && shift.endOdometer >= shift.startOdometer) {
        shift.endOdometer - shift.startOdometer
    } else {
        0
    }

    val gross = trips.sumOf { it.grossAmount }
    val commission = trips.sumOf { it.commissionAmount }
    val net = trips.sumOf { it.netAmount }

    val gasExpense = fuelings
        .filter { it.fuelType == FuelType.GAS.name }
        .sumOf { it.amountPaid }

    val gasolineExpense = fuelings
        .filter { it.fuelType == FuelType.GASOLINE.name }
        .sumOf { it.amountPaid }

    val totalFuel = gasExpense + gasolineExpense
    val totalOperational = expenses.sumOf { it.amount }
    val totalExp = totalFuel + totalOperational
    val profit = net - totalExp

    val progress = if (shift.dailyQuotaTarget > 0.0) {
        (profit / shift.dailyQuotaTarget) * 100.0
    } else {
        0.0
    }

    val costPerKm = if (totalKm > 0) totalExp / totalKm else 0.0
    val grossPerKm = if (totalKm > 0) gross / totalKm else 0.0
    val netPerKm = if (totalKm > 0) net / totalKm else 0.0

    return ShiftFinancialSummary(
        shiftId = shift.id,
        status = domainStatus,
        startTime = shift.startTime,
        endTime = shift.endTime,
        startOdometer = shift.startOdometer,
        endOdometer = shift.endOdometer,
        totalKilometers = totalKm,
        dailyQuotaTarget = shift.dailyQuotaTarget,
        totalGrossIncome = gross,
        totalCommissions = commission,
        totalNetIncome = net,
        totalGasExpense = gasExpense,
        totalGasolineExpense = gasolineExpense,
        totalFuelExpense = totalFuel,
        totalOperationalExpenses = totalOperational,
        totalExpenses = totalExp,
        netProfit = profit,
        quotaProgressPercentage = progress,
        costPerKilometer = costPerKm,
        grossIncomePerKilometer = grossPerKm,
        netIncomePerKilometer = netPerKm,
        tripsCount = trips.size,
        fuelingsCount = fuelings.size,
        expensesCount = expenses.size
    )
}
