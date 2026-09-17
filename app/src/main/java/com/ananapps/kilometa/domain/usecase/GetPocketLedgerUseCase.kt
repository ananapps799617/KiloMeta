// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/GetPocketLedgerUseCase.kt
// DESCRIPCI?N PEDAG?GICA:
// Caso de uso reactivo que computa el extracto contable y libro mayor de un bolsillo espec?fico (Orden #044).
// Aplica ventana semanal estricta (Lunes-Domingo para Apps; Corte de cuota para Efectivo/Caja).
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.*
import com.ananapps.kilometa.domain.repository.DriverProfileRepository
import com.ananapps.kilometa.domain.repository.ExpenseRepository
import com.ananapps.kilometa.domain.repository.MaintenanceRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.repository.WalletTransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

class GetPocketLedgerUseCase @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val expenseRepository: ExpenseRepository,
    private val walletTransferRepository: WalletTransferRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val driverProfileRepository: DriverProfileRepository,
    private val calculateWeeklyCycleBoundsUseCase: CalculateWeeklyCycleBoundsUseCase
) {

    @Suppress("UNCHECKED_CAST")
    operator fun invoke(
        pocketType: PocketType,
        weekOffset: Int = 0
    ): Flow<PocketLedgerSummary> {
        return combine(
            shiftRepository.getAllTrips(),
            shiftRepository.getAllFuelings(),
            expenseRepository.getAllExpenses(),
            walletTransferRepository.getAllTransfers(),
            maintenanceRepository.getAllMaintenances(),
            driverProfileRepository.getVehicleProfile()
        ) { args: Array<Any?> ->
            val trips = args[0] as List<Trip>
            val fuelings = args[1] as List<Fueling>
            val expenses = args[2] as List<Expense>
            val transfers = args[3] as List<WalletTransfer>
            val maintenances = args[4] as List<MaintenanceRecord>
            val vehicle = args[5] as VehicleProfile

            computeSummary(pocketType, weekOffset, vehicle, trips, fuelings, expenses, transfers, maintenances)
        }
    }

    private fun computeSummary(
        pocketType: PocketType,
        weekOffset: Int,
        vehicle: VehicleProfile,
        trips: List<Trip>,
        fuelings: List<Fueling>,
        expenses: List<Expense>,
        transfers: List<WalletTransfer>,
        maintenances: List<MaintenanceRecord>,
        nowMillis: Long = System.currentTimeMillis()
    ): PocketLedgerSummary {
        val zoneId = ZoneId.of("America/Bogota")
        val esCo = Locale("es", "CO")

        val (startMillis, endMillis, title, subtitle) = if (pocketType.isPlatform) {
            val (start, end) = calculateWeeklyCycleBoundsUseCase(
                nowMillis = nowMillis,
                cutoffDay = DayOfWeek.SUNDAY,
                cutoffHour = 23,
                zoneId = zoneId,
                weekOffset = weekOffset
            )
            val sLd = Instant.ofEpochMilli(start).atZone(zoneId).toLocalDate()
            val eLd = Instant.ofEpochMilli(end).atZone(zoneId).toLocalDate()
            val fmt = DateTimeFormatter.ofPattern("d 'de' MMM", esCo)
            val t = if (weekOffset == 0) "Esta Semana (${sLd.format(fmt)} ? ${eLd.format(fmt)})" else "Semana: ${sLd.format(fmt)} ? ${eLd.format(fmt)} (Cerrada)"
            val sub = "Lun 00:00 ? Dom 23:59 (Plataformas)"
            Tuple4(start, end, t, sub)
        } else {
            val cutoffDay = DayOfWeek.of(vehicle.quotaCutoffDay.coerceIn(1, 7))
            val (start, end) = calculateWeeklyCycleBoundsUseCase(
                nowMillis = nowMillis,
                cutoffDay = cutoffDay,
                cutoffHour = vehicle.quotaCutoffHour,
                zoneId = zoneId,
                weekOffset = weekOffset
            )
            val sLd = Instant.ofEpochMilli(start).atZone(zoneId).toLocalDate()
            val eLd = Instant.ofEpochMilli(end).atZone(zoneId).toLocalDate()
            val fmt = DateTimeFormatter.ofPattern("d 'de' MMM", esCo)
            val t = if (weekOffset == 0) "Esta Semana" else "Semana: ${sLd.format(fmt)} ? ${eLd.format(fmt)} (Cerrada)"
            val sub = "${sLd.dayOfWeek.getDisplayName(TextStyle.SHORT, esCo)} 00:00 ? ${eLd.dayOfWeek.getDisplayName(TextStyle.SHORT, esCo)} 23:59"
            Tuple4(start, end, t, sub)
        }

        val fTrips = trips.filter { it.timestamp in startMillis..endMillis }
        val fFuelings = fuelings.filter { it.timestamp in startMillis..endMillis }
        val fExpenses = expenses.filter { it.timestamp in startMillis..endMillis }
        val fTransfers = transfers.filter { it.timestamp in startMillis..endMillis }
        val fMaintenances = maintenances.filter { it.timestamp in startMillis..endMillis }

        val items = mutableListOf<PocketTransactionItem>()

        when (pocketType) {
            PocketType.UBER, PocketType.DIDI -> {
                val targetPlatform = pocketType.platformType
                fTrips.filter { it.platformType == targetPlatform }.forEach { trip ->
                    if (trip.paymentMethod == PaymentMethod.APP_WALLET || trip.paymentDestination == PaymentDestination.APP_RETAINED) {
                        items.add(
                            PocketTransactionItem(
                                id = trip.id,
                                timestamp = trip.timestamp,
                                title = "Carrera Digital",
                                subtitle = "Bruto $ ${trip.grossAmount.toInt()} - Com. $ ${trip.commissionAmount.toInt()}",
                                amount = trip.netAmount,
                                icon = "??"
                            )
                        )
                    } else if (trip.paymentMethod == PaymentMethod.CASH && trip.commissionAmount > 0) {
                        items.add(
                            PocketTransactionItem(
                                id = "${trip.id}_comm",
                                timestamp = trip.timestamp,
                                title = "Comisi?n Efectivo (${trip.platformType.displayName})",
                                subtitle = "Descuento de comisi?n de carrera en efectivo",
                                amount = -trip.commissionAmount,
                                icon = "??"
                            )
                        )
                    }
                }

                val targetWallet = if (pocketType == PocketType.UBER) WalletType.UBER else WalletType.DIDI
                fTransfers.forEach { tr ->
                    if (tr.sourceWallet == targetWallet) {
                        items.add(PocketTransactionItem(tr.id, tr.timestamp, "Dispersi?n / Retiro", tr.notes ?: "Traslado a cuenta bancaria", -tr.amount, "??"))
                    } else if (tr.destinationWallet == targetWallet) {
                        items.add(PocketTransactionItem(tr.id, tr.timestamp, "Recarga de Billetera", tr.notes ?: "Ingreso a app", tr.amount, "??"))
                    }
                }
            }

            PocketType.CASH_PHYSICAL -> {
                fTrips.filter { it.paymentMethod == PaymentMethod.CASH }.forEach { trip ->
                    items.add(PocketTransactionItem(trip.id, trip.timestamp, "Carrera en Efectivo (${trip.platformType.displayName})", "Cobro f?sico en mano", trip.grossAmount, "??"))
                }
                fFuelings.filter { it.paymentSource == WalletType.CASH_PHYSICAL }.forEach { fuel ->
                    items.add(PocketTransactionItem(fuel.id, fuel.timestamp, "Tanqueo ${fuel.fuelType.displayName}", "${fuel.volume} m?/gal", -fuel.amountPaid, "?"))
                }
                fExpenses.filter { it.paymentSource == WalletType.CASH_PHYSICAL }.forEach { exp ->
                    items.add(PocketTransactionItem(exp.id, exp.timestamp, "Gasto: ${exp.concept.displayName}", "Efectivo en ruta", -exp.amount, "??"))
                }
                fMaintenances.filter { it.paymentSource == WalletType.CASH_PHYSICAL }.forEach { m ->
                    items.add(PocketTransactionItem(m.id, m.timestamp, "Mantenimiento: ${m.type.displayName}", m.serviceTitle ?: "Taller", -m.costCop, "??"))
                }
                fTransfers.forEach { tr ->
                    if (tr.sourceWallet == WalletType.CASH_PHYSICAL) {
                        items.add(PocketTransactionItem(tr.id, tr.timestamp, "Apartado / Traslado", tr.notes ?: "Salida de efectivo", -tr.amount, "?"))
                    } else if (tr.destinationWallet == WalletType.CASH_PHYSICAL) {
                        items.add(PocketTransactionItem(tr.id, tr.timestamp, "Entrada a Efectivo", tr.notes ?: "Ingreso en mano", tr.amount, "?"))
                    }
                }
            }

            PocketType.DIGITAL_WALLET -> {
                fTrips.filter { it.paymentMethod == PaymentMethod.NEQUI }.forEach { trip ->
                    items.add(PocketTransactionItem(trip.id, trip.timestamp, "Carrera Directa (${trip.platformType.displayName})", "Transferencia recibida", trip.grossAmount, "??"))
                }
                fFuelings.filter { it.paymentSource == WalletType.DIGITAL_WALLET }.forEach { fuel ->
                    items.add(PocketTransactionItem(fuel.id, fuel.timestamp, "Tanqueo ${fuel.fuelType.displayName}", "${fuel.volume} m?/gal", -fuel.amountPaid, "?"))
                }
                fExpenses.filter { it.paymentSource == WalletType.DIGITAL_WALLET }.forEach { exp ->
                    items.add(PocketTransactionItem(exp.id, exp.timestamp, "Gasto: ${exp.concept.displayName}", "Transferencia digital", -exp.amount, "??"))
                }
                fMaintenances.filter { it.paymentSource == WalletType.DIGITAL_WALLET }.forEach { m ->
                    items.add(PocketTransactionItem(m.id, m.timestamp, "Mantenimiento: ${m.type.displayName}", m.serviceTitle ?: "Taller", -m.costCop, "??"))
                }
                fTransfers.forEach { tr ->
                    if (tr.sourceWallet == WalletType.DIGITAL_WALLET) {
                        items.add(PocketTransactionItem(tr.id, tr.timestamp, "Salida Nequi/Davi", tr.notes ?: "Traslado de fondos", -tr.amount, "?"))
                    } else if (tr.destinationWallet == WalletType.DIGITAL_WALLET) {
                        items.add(PocketTransactionItem(tr.id, tr.timestamp, "Recibido en Nequi/Davi", tr.notes ?: "Ingreso a cuenta", tr.amount, "??"))
                    }
                }
            }

            PocketType.CAR_QUOTA -> {
                fTransfers.forEach { tr ->
                    if (tr.destinationWallet == WalletType.CAR_QUOTA) {
                        items.add(PocketTransactionItem(tr.id, tr.timestamp, "Apartado para Cuota", tr.notes ?: "Abono a cuota", tr.amount, "??"))
                    }
                    if (tr.sourceWallet == WalletType.CAR_QUOTA) {
                        items.add(PocketTransactionItem(tr.id, tr.timestamp, "Retiro de Cuota", tr.notes ?: "Traslado de fondos", -tr.amount, "?"))
                    }
                }
                fExpenses.filter { it.paymentSource == WalletType.CAR_QUOTA }.forEach { exp ->
                    items.add(PocketTransactionItem(exp.id, exp.timestamp, "Pago Cuota Carro", exp.concept.displayName, -exp.amount, "??"))
                }
            }
        }

        val sortedItems = items.sortedByDescending { it.timestamp }
        val totalInflow = sortedItems.filter { it.amount > 0 }.sumOf { it.amount }
        val totalOutflow = sortedItems.filter { it.amount < 0 }.sumOf { -it.amount }
        val netBalance = totalInflow - totalOutflow

        return PocketLedgerSummary(
            pocketType = pocketType,
            cycleStartMillis = startMillis,
            cycleEndMillis = endMillis,
            cycleTitle = title,
            cycleSubtitle = subtitle,
            totalInflow = totalInflow,
            totalOutflow = totalOutflow,
            netPeriodBalance = netBalance,
            transactions = sortedItems,
            canGoNext = weekOffset > 0
        )
    }

    private data class Tuple4(val v1: Long, val v2: Long, val v3: String, val v4: String)
}
