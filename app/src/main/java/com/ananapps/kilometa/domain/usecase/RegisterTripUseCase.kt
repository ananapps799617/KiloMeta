// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/RegisterTripUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para el registro de carreras y servicios multi-plataforma.
// Aplica Domain Guards estrictos para evitar montos negativos o inconsistencias contables
// donde la comisión sobrepase el valor bruto de la carrera, y persiste mediante TripRepository.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.domain.repository.TripRepository
import java.util.UUID
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE REGISTRO DE CARRERAS CON DOMAIN GUARDS
// ------------------------------------------------------------------------------
/**
 * Valida y persiste una nueva carrera en la jornada del conductor.
 */
class RegisterTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {

    /**
     * Valida las reglas de negocio de una carrera y la almacena en el repositorio.
     *
     * @param shiftId Identificador del turno activo al que pertenece el servicio.
     * @param platformType Plataforma que originó el viaje (Uber, DiDi, InDrive, Particular).
     * @param grossAmount Tarifa bruta pagada por el pasajero en COP.
     * @param commissionAmount Tarifa de intermediación descontada por la app (COP).
     * @param paymentMethod Medio de pago recaudado (Efectivo, Nequi, Billetera App).
     * @param timestamp Momento Unix de la carrera en milisegundos.
     * @return [Trip] instanciado y persistido.
     * @throws IllegalArgumentException si algún valor viola las reglas de dominio.
     */
    suspend operator fun invoke(
        shiftId: String,
        platformType: PlatformType,
        grossAmount: Double,
        commissionAmount: Double,
        paymentMethod: PaymentMethod,
        paymentDestination: com.ananapps.kilometa.domain.model.PaymentDestination = com.ananapps.kilometa.domain.model.PaymentDestination.CASH_IN_HAND,
        timestamp: Long = System.currentTimeMillis()
    ): Trip {
        // Domain Guards: Validaciones de consistencia contable
        require(shiftId.isNotBlank()) { "El identificador del turno no puede estar vacío" }
        require(grossAmount >= 0.0) { "La tarifa bruta de la carrera debe ser mayor o igual a cero" }
        require(commissionAmount >= 0.0) { "La comisión de la plataforma debe ser mayor o igual a cero" }
        require(commissionAmount <= grossAmount) {
            "La comisión ($commissionAmount COP) no puede exceder el monto bruto cobrado ($grossAmount COP)"
        }

        val netAmount = grossAmount - commissionAmount

        val trip = Trip(
            id = UUID.randomUUID().toString(),
            shiftId = shiftId,
            platformType = platformType,
            grossAmount = grossAmount,
            commissionAmount = commissionAmount,
            netAmount = netAmount,
            paymentMethod = paymentMethod,
            paymentDestination = paymentDestination,
            timestamp = timestamp
        )

        tripRepository.addTrip(trip)
        return trip
    }
}
