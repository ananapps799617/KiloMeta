// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/HistoryDialogsHost.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contenedor desacoplado de diálogos modales para la pantalla de Historial (Orden #043).
// Centraliza edición de entidades, ajuste de métricas del día y registros contextuales.
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.runtime.Composable
import com.ananapps.kilometa.domain.model.*
import com.ananapps.kilometa.presentation.home.components.*

@Composable
fun HistoryDialogsHost(
    editingTrip: Trip?,
    editingFueling: Fueling?,
    editingExpense: Expense?,
    editingMaintenance: MaintenanceRecord?,
    editingDayMetrics: DayHistorySummary?,
    registeringTripTimestamp: Long?,
    registeringFuelingTimestamp: Long?,
    registeringExpenseTimestamp: Long?,
    registeringMaintenanceTimestamp: Long?,
    lastKnownOdometer: Int,
    vehicleProfile: VehicleProfile,
    onDismissTripEdit: () -> Unit,
    onDismissFuelingEdit: () -> Unit,
    onDismissExpenseEdit: () -> Unit,
    onDismissMaintenanceEdit: () -> Unit,
    onDismissDayMetricsEdit: () -> Unit,
    onDismissRegisterTrip: () -> Unit,
    onDismissRegisterFueling: () -> Unit,
    onDismissRegisterExpense: () -> Unit,
    onDismissRegisterMaintenance: () -> Unit,
    onUpdateTrip: (Trip) -> Unit,
    onUpdateFueling: (Fueling) -> Unit,
    onUpdateExpense: (Expense) -> Unit,
    onUpdateMaintenance: (MaintenanceRecord) -> Unit,
    onUpdateDayMetrics: (startOdo: Int, endOdo: Int, startTimeMillis: Long, endTimeMillis: Long) -> Unit,
    onConfirmAddTrip: (PlatformType, Double, Double, PaymentMethod, PaymentDestination, Long, Int?, Int?) -> Unit,
    onConfirmAddFueling: (FuelType, Double, Double, Int, WalletType, Long) -> Unit,
    onConfirmAddExpense: (ExpenseConcept, Double, WalletType, Long) -> Unit,
    onConfirmAddMaintenance: (MaintenanceType, Int, Double, String?, WalletType, String?, String?, Long) -> Unit
) {
    editingTrip?.let { trip ->
        TripEditDialog(trip = trip, onDismiss = onDismissTripEdit, onConfirm = onUpdateTrip)
    }

    editingFueling?.let { fueling ->
        FuelingEditDialog(fueling = fueling, onDismiss = onDismissFuelingEdit, onConfirm = onUpdateFueling)
    }

    editingExpense?.let { expense ->
        ExpenseEditDialog(expense = expense, onDismiss = onDismissExpenseEdit, onConfirm = onUpdateExpense)
    }

    editingMaintenance?.let { maintenance ->
        MaintenanceEditDialog(
            maintenance = maintenance,
            vehicleProfile = vehicleProfile,
            onDismiss = onDismissMaintenanceEdit,
            onConfirm = onUpdateMaintenance
        )
    }

    editingDayMetrics?.let { day ->
        EditDayMetricsDialog(
            day = day,
            onDismiss = onDismissDayMetricsEdit,
            onConfirmSave = onUpdateDayMetrics
        )
    }

    registeringTripTimestamp?.let { timestamp ->
        RegisterTripDialog(
            lastKnownOdometer = lastKnownOdometer,
            onDismiss = onDismissRegisterTrip,
            initialTimestamp = timestamp,
            onConfirmTrip = { plat, gross, comm, pay, dest, time, sOdo, eOdo ->
                onConfirmAddTrip(plat, gross, comm, pay, dest, time, sOdo, eOdo)
            }
        )
    }

    registeringFuelingTimestamp?.let { timestamp ->
        RegisterFuelingDialog(
            currentOdometer = lastKnownOdometer,
            onDismiss = onDismissRegisterFueling,
            initialTimestamp = timestamp,
            propulsionType = vehicleProfile.propulsionType,
            onConfirmFueling = { fType, amount, vol, odo, source, time ->
                onConfirmAddFueling(fType, amount, vol, odo, source, time)
            }
        )
    }

    registeringExpenseTimestamp?.let { timestamp ->
        RegisterExpenseDialog(
            onDismiss = onDismissRegisterExpense,
            initialTimestamp = timestamp,
            onConfirmExpense = { concept, amount, source, time ->
                onConfirmAddExpense(concept, amount, source, time)
            }
        )
    }

    registeringMaintenanceTimestamp?.let { timestamp ->
        RegisterMaintenanceDialog(
            vehicleProfile = vehicleProfile,
            lastKnownOdometer = lastKnownOdometer,
            onDismiss = onDismissRegisterMaintenance,
            initialTimestamp = timestamp,
            onConfirmMaintenance = { type, odo, cost, notes, source, title, loc, time ->
                onConfirmAddMaintenance(type, odo, cost, notes, source, title, loc, time)
            }
        )
    }
}
