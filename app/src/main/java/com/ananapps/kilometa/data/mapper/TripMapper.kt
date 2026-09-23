// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/mapper/TripMapper.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Mapeadores de extensión bidireccionales entre TripEntity y Trip (Dominio).
// Asegura la conversión consistente de enumeraciones PlatformType, PaymentMethod y PaymentDestination.
// ==============================================================================

package com.ananapps.kilometa.data.mapper

import com.ananapps.kilometa.data.local.entity.TripEntity
import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.Trip

// ------------------------------------------------------------------------------
// BLOQUE 1: MAPEO ENTIDAD <-> DOMINIO DE CARRERAS
// ------------------------------------------------------------------------------
fun TripEntity.toDomain(): Trip {
    val platform = try {
        PlatformType.valueOf(this.platformName)
    } catch (_: IllegalArgumentException) {
        PlatformType.PRIVATE
    }

    val payment = try {
        PaymentMethod.valueOf(this.paymentMethod)
    } catch (_: IllegalArgumentException) {
        PaymentMethod.CASH
    }

    val destination = try {
        PaymentDestination.valueOf(this.paymentDestination)
    } catch (_: IllegalArgumentException) {
        PaymentDestination.CASH_IN_HAND
    }

    return Trip(
        id = this.id,
        shiftId = this.shiftId,
        platformType = platform,
        grossAmount = this.grossAmount,
        commissionAmount = this.commissionAmount,
        netAmount = this.netAmount,
        paymentMethod = payment,
        paymentDestination = destination,
        timestamp = this.timestamp,
        startOdometer = this.startOdometer,
        endOdometer = this.endOdometer,
        platformPromoDiscount = this.platformPromoDiscount,
        distanceKm = this.distanceKm,
        durationMinutes = this.durationMinutes
    )
}

fun Trip.toEntity(): TripEntity {
    return TripEntity(
        id = this.id,
        shiftId = this.shiftId,
        platformName = this.platformType.name,
        grossAmount = this.grossAmount,
        commissionAmount = this.commissionAmount,
        netAmount = this.netAmount,
        paymentMethod = this.paymentMethod.name,
        paymentDestination = this.paymentDestination.name,
        timestamp = this.timestamp,
        startOdometer = this.startOdometer,
        endOdometer = this.endOdometer,
        platformPromoDiscount = this.platformPromoDiscount,
        distanceKm = this.distanceKm,
        durationMinutes = this.durationMinutes
    )
}

fun List<TripEntity>.toDomain(): List<Trip> = map { it.toDomain() }
