// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/ContinuousTimelineCockpitView.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Vista principal continua de cabina inspirada en KiloMeta.
// Erradica el estado bloqueante de "Cabina en Espera" y mantiene la pantalla
// 100% viva mostrando el odómetro real del vehículo, el hero del día y las actividades.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.*
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun ContinuousTimelineCockpitView(
    vehicle: VehicleProfile,
    lastKnownOdometer: Int,
    timelineSummary: DayContinuousSummary,
    weeklyProgress: WeeklyQuotaProgress?,
    auditSummary: AccountingAuditSummary?,
    cashSettlement: CashSettlementSummary?,
    onStartShiftClick: () -> Unit,
    onCloseShiftClick: () -> Unit,
    onDeleteTrip: (String) -> Unit = {},
    onDeleteExpense: (String) -> Unit = {},
    onDeleteFueling: (String) -> Unit = {},
    onEditTrip: (Trip) -> Unit = {},
    onEditExpense: (Expense) -> Unit = {},
    onEditFueling: (Fueling) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Encabezado Compacto de Vehículo Activo & Odómetro Real (Benchmark KiloMeta)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DeepObsidianSurface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (vehicle.vehicleType == VehicleType.MOTORCYCLE) "🏍️" else "🚗",
                    fontSize = 18.sp
                )
                Column {
                    Text(
                        text = "${vehicle.brand} ${vehicle.model}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = vehicle.vehiclePlate,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = ElectricBluePrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepObsidianSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Km ${String.format(Locale.US, "%,d", lastKnownOdometer)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = StatusGreen
                )
            }
        }

        // Hero Card Financiero del Día
        DayHeroCard(
            summary = timelineSummary,
            onStartShiftClick = onStartShiftClick,
            onCloseShiftClick = onCloseShiftClick
        )

        // Feed de Actividades del Día en Ruta
        DayActivityFeedCard(
            events = timelineSummary.events,
            onDeleteTrip = onDeleteTrip,
            onDeleteExpense = onDeleteExpense,
            onDeleteFueling = onDeleteFueling,
            onEditTrip = onEditTrip,
            onEditExpense = onEditExpense,
            onEditFueling = onEditFueling
        )

        // Liquidación de Efectivo en Mano vs Plataformas (si aplica)
        cashSettlement?.let { CockpitCashSettlementCard(settlement = it) }

        // Auditoría Semanal o Progreso de Cuota
        if (auditSummary != null) {
            AdvancedAccountingCard(auditSummary = auditSummary)
        } else if (weeklyProgress != null) {
            WeeklyQuotaCard(progress = weeklyProgress)
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}
