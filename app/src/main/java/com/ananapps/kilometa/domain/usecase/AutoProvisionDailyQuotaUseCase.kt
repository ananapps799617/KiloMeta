// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/AutoProvisionDailyQuotaUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso que aparta automáticamente la cuota diaria del vehículo desde la
// billetera digital (Nequi/Davi) hacia el 5to bolsillo "Cuota Carro" (Micro-Orden #039.2).
// Respeta estrictamente los días laborados configurados (ej. 6 días -> meta / 6).
// En días de descanso no genera deducciones.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.TransferType
import com.ananapps.kilometa.domain.model.WalletTransfer
import com.ananapps.kilometa.domain.model.WalletType
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.repository.WalletTransferRepository
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class AutoProvisionDailyQuotaUseCase @Inject constructor(
    private val driverProfileRepository: DriverProfileRepository,
    private val shiftRepository: ShiftRepository,
    private val walletTransferRepository: WalletTransferRepository,
    private val calculateWeeklyCycleBoundsUseCase: CalculateWeeklyCycleBoundsUseCase,
    private val calculateDynamicQuotaUseCase: CalculateDynamicQuotaUseCase
) {

    suspend operator fun invoke(nowMillis: Long = System.currentTimeMillis()) {
        val vehicle = driverProfileRepository.getVehicleProfile().first()
        if (vehicle.quotaAmount <= 0.0) return

        val dailyQuota = calculateDynamicQuotaUseCase(vehicle)
        if (dailyQuota <= 0.0) return

        val cutoffDay = when (vehicle.quotaCutoffDay) {
            1 -> DayOfWeek.MONDAY
            2 -> DayOfWeek.TUESDAY
            3 -> DayOfWeek.WEDNESDAY
            4 -> DayOfWeek.THURSDAY
            5 -> DayOfWeek.FRIDAY
            6 -> DayOfWeek.SATURDAY
            7 -> DayOfWeek.SUNDAY
            else -> DayOfWeek.TUESDAY
        }

        val (cycleStartMillis, cycleEndMillis) = calculateWeeklyCycleBoundsUseCase(
            nowMillis = nowMillis,
            cutoffDay = cutoffDay,
            cutoffHour = vehicle.quotaCutoffHour
        )

        val allTrips = shiftRepository.getAllTrips().first()
        val cycleTrips = allTrips.filter { it.timestamp in cycleStartMillis..cycleEndMillis }
        if (cycleTrips.isEmpty()) return

        val zoneId = ZoneId.of("America/Bogota")
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val activeDayStrings = cycleTrips.map { trip ->
            Instant.ofEpochMilli(trip.timestamp).atZone(zoneId).format(dateFormatter)
        }.distinct().sorted()

        val existingTransfers = walletTransferRepository.getAllTransfers().first()

        activeDayStrings.forEach { dateStr ->
            val plate = vehicle.vehiclePlate.ifBlank { "default" }
            val transferId = "auto_quota_${plate}_$dateStr"
            val alreadyExists = existingTransfers.any { it.id == transferId }
            if (!alreadyExists) {
                val dayTrip = cycleTrips.first {
                    Instant.ofEpochMilli(it.timestamp).atZone(zoneId).format(dateFormatter) == dateStr
                }
                val transfer = WalletTransfer(
                    id = transferId,
                    timestamp = dayTrip.timestamp,
                    sourceWallet = WalletType.DIGITAL_WALLET,
                    destinationWallet = WalletType.CAR_QUOTA,
                    amount = dailyQuota,
                    transferType = TransferType.QUOTA_PROVISION,
                    notes = "Abono diario cuota carro ($dateStr)"
                )
                walletTransferRepository.insertTransfer(transfer)
            }
        }
    }
}
