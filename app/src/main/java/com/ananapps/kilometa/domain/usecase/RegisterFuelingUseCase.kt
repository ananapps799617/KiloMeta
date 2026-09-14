// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/RegisterFuelingUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para el registro de abastecimiento bi-modal de combustible (GNV o Gasolina).
// Aplica Domain Guards estrictos que impiden valores monetarios o de volumen nulos o negativos,
// y asegura la integridad del odómetro antes de persistir en FuelingRepository.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.repository.FuelingRepository
import java.util.UUID
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE REGISTRO DE RECARGA CON DOMAIN GUARDS
// ------------------------------------------------------------------------------
/**
 * Valida y persiste una recarga de combustible en la estación de servicio.
 */
class RegisterFuelingUseCase @Inject constructor(
    private val fuelingRepository: FuelingRepository
) {

    /**
     * Valida las reglas de dominio para abastecimiento y almacena el registro.
     *
     * @param shiftId Identificador del turno activo.
     * @param fuelType Modalidad de recarga (GAS o GASOLINE).
     * @param amountPaid Monto total en COP cancelado en la estación.
     * @param volume Cantidad de m³ o galones suministrados.
     * @param odometer Lectura actual del odómetro al momento del tanqueo.
     * @param timestamp Momento Unix de la recarga en milisegundos.
     * @return [Fueling] instanciado y guardado.
     * @throws IllegalArgumentException si los datos violan las restricciones de dominio.
     */
    suspend operator fun invoke(
        shiftId: String,
        fuelType: FuelType,
        amountPaid: Double,
        volume: Double,
        odometer: Int,
        timestamp: Long = System.currentTimeMillis(),
        paymentSource: com.ananapps.kilometa.domain.model.WalletType = com.ananapps.kilometa.domain.model.WalletType.CASH_PHYSICAL
    ): Fueling {
        // Domain Guards: Validaciones estrictas de volumen, valor y odometría
        require(shiftId.isNotBlank()) { "El identificador del turno no puede estar vacío" }
        require(amountPaid > 0.0) { "El valor pagado por el combustible debe ser estrictamente mayor a cero" }
        require(volume > 0.0) { "El volumen de combustible cargado debe ser estrictamente mayor a cero" }
        require(odometer >= 0) { "La lectura del odómetro debe ser mayor o igual a cero" }

        val fueling = Fueling(
            id = UUID.randomUUID().toString(),
            shiftId = shiftId,
            fuelType = fuelType,
            amountPaid = amountPaid,
            volume = volume,
            odometer = odometer,
            timestamp = timestamp,
            paymentSource = paymentSource
        )

        fuelingRepository.addFueling(fueling)
        return fueling
    }
}
