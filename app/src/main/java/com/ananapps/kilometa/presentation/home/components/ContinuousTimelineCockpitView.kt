// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/ContinuousTimelineCockpitView.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Vista principal de Cabina "Cockpit Dark" (99.9% fiel a la lámina de diseño).
// Orquesta con jerarquía visual de cabina:
// 1. Cabecera KiloMeta con campana y bloque "Cabina · Tu jornada, tus números, tu meta."
// 2. Tarjeta estilizada del vehículo activo con avatar circular y píldora de estado.
// 3. Tarjeta DayHeroCard ("En tu bolsillo hoy" con anillo circular de porcentaje).
// 4. Fila CockpitOperationalKpisRow (Horas hoy, Kilómetros, Rendimiento).
// 5. Sección CockpitWalletsCard (Bolsillos en grilla limpia 2x2).
// 6. Bloque DayOdometerChip ("Jornada del día" con los 4 estados de cabina).
// 7. Feed DayActivityFeedCard ("Actividad reciente" con separadores sutiles).
// Regla sagrada: Modularidad estricta < 280 LOC.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.R
import com.ananapps.kilometa.domain.model.*
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun ContinuousTimelineCockpitView(
    vehicle: VehicleProfile,
    lastKnownOdometer: Int,
    timelineSummary: DayContinuousSummary,
    wallets: List<Wallet> = emptyList(),
    cashSettlement: CashSettlementSummary? = null,
    weeklyProgress: WeeklyQuotaProgress? = null,
    auditSummary: AccountingAuditSummary? = null,
    onDeleteTrip: (String) -> Unit = {},
    onDeleteExpense: (String) -> Unit = {},
    onDeleteFueling: (String) -> Unit = {},
    onDeleteTransfer: (String) -> Unit = {},
    onDeleteMaintenance: (String) -> Unit = {},
    onEditTrip: (Trip) -> Unit = {},
    onEditExpense: (Expense) -> Unit = {},
    onEditFueling: (Fueling) -> Unit = {},
    onEditMaintenance: (MaintenanceRecord) -> Unit = {},
    onTransferClick: () -> Unit = {},
    onPocketClick: ((PocketType) -> Unit)? = null,
    onStartShiftClick: () -> Unit = {},
    onCloseShiftClick: () -> Unit = {},
    onOpenDayOdometerDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val effectiveSettlement = timelineSummary.cashSettlement ?: cashSettlement
    val isEnEspera = (!timelineSummary.hasActiveShift && !timelineSummary.isDayClosed && timelineSummary.events.isNotEmpty()) ||
            (timelineSummary.hasActiveShift && timelineSummary.events.isEmpty())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. TARJETA COMPACTA DEL VEHÍCULO ACTIVO ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DeepObsidianSurface)
                .border(1.dp, DeepObsidianBorder, RoundedCornerShape(18.dp))
                .clickable { onOpenDayOdometerDialog() }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(StatusGreen.copy(alpha = 0.15f))
                            .border(1.dp, StatusGreen.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = vehicle.vehicleType.icon.ifBlank { "🚗" }, fontSize = 18.sp)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "${vehicle.brand} ${vehicle.model}".ifBlank { "Vehículo Asignado" },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = vehicle.vehiclePlate.ifBlank { "ABC-123" },
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                color = TextSecondary
                            )
                            Text(text = "·", color = TextSecondary)
                            Text(
                                text = "Km ${String.format(Locale.US, "%,d", lastKnownOdometer)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                color = StatusGreen
                            )
                        }

                        // Píldora de Estado
                        val (statusBadgeText, statusBadgeColor) = when {
                            timelineSummary.isDayClosed -> Pair("● Jornada cerrada", TextSecondary)
                            isEnEspera -> Pair("● En espera", StatusAmber)
                            timelineSummary.hasActiveShift -> Pair("● En ruta", StatusGreen)
                            else -> Pair("● Vehículo activo", StatusGreen)
                        }
                        Text(
                            text = statusBadgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = statusBadgeColor
                        )
                    }
                }

                Text(
                    text = "›",
                    fontSize = 20.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        // --- 3. TARJETA RESUMEN «EN TU BOLSILLO HOY» (100% LIMPIA) ---
        DayHeroCard(
            summary = timelineSummary,
            vehicle = vehicle
        )

        // --- 4. FILA DE 3 INDICADORES OPERATIVOS ---
        CockpitOperationalKpisRow(
            summary = timelineSummary
        )

        // --- 5. SECCIÓN BOLSILLOS (GRILLA 2x2) ---
        CockpitWalletsCard(
            settlement = effectiveSettlement,
            wallets = wallets,
            cycleLabel = timelineSummary.cycleLabel,
            onTransferClick = onTransferClick,
            onPocketClick = onPocketClick
        )

        // --- 6. BLOQUE JORNADA DEL DÍA (4 ESTADOS) ---
        DayOdometerChip(
            dayStartOdometer = timelineSummary.dayStartOdometer,
            dayEndOdometer = timelineSummary.dayEndOdometer,
            isDayClosed = timelineSummary.isDayClosed,
            lastKnownOdometer = lastKnownOdometer,
            hasActiveShift = timelineSummary.hasActiveShift,
            hasEventsToday = timelineSummary.events.isNotEmpty(),
            elapsedHoursFormatted = timelineSummary.elapsedHoursFormatted,
            onStartShiftClick = onStartShiftClick,
            onCloseShiftClick = onCloseShiftClick,
            onOpenDayOdometerDialog = onOpenDayOdometerDialog
        )

        // --- 7. ACTIVIDAD RECIENTE ---
        DayActivityFeedCard(
            events = timelineSummary.events,
            onDeleteTrip = onDeleteTrip,
            onDeleteExpense = onDeleteExpense,
            onDeleteFueling = onDeleteFueling,
            onDeleteTransfer = onDeleteTransfer,
            onDeleteMaintenance = onDeleteMaintenance,
            onEditTrip = onEditTrip,
            onEditExpense = onEditExpense,
            onEditFueling = onEditFueling,
            onEditMaintenance = onEditMaintenance
        )

        Spacer(modifier = Modifier.height(72.dp))
    }
}
