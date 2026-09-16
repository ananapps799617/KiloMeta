// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/ContinuousTimelineCockpitView.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Vista principal continua de cabina inspirada en KiloMeta (Orden #038).
// Descongestiona radicalmente la pantalla Ruta manteniendo únicamente:
// 1. Encabezado de vehículo y odómetro real.
// 2. Tarjeta DayHeroCard (neto, meta diaria limpia, telemetría y subtotales).
// 3. Tarjeta CockpitWalletsCard (4 bolsillos soberanos y transferencias).
// 4. Feed de actividades sobre la pista de asfalto continua Drivvo.
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
    onTransferClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val effectiveSettlement = timelineSummary.cashSettlement ?: cashSettlement

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Encabezado Compacto de Vehículo Activo & Odómetro Real (Benchmark KiloMeta)
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
                    text = vehicle.vehicleType.icon,
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

        // 2. Tarjeta DayHeroCard (Producción, Meta limpia y Cobertura)
        DayHeroCard(
            summary = timelineSummary,
            vehicle = vehicle
        )

        // 3. Tarjeta CockpitWalletsCard (Saldos de los 5 bolsillos con ciclo semanal y aire)
        CockpitWalletsCard(
            settlement = effectiveSettlement,
            cycleLabel = timelineSummary.cycleLabel,
            onTransferClick = onTransferClick
        )

        // 4. Feed de Actividades de Hoy sobre la Pista de Asfalto Continua
        DayActivityFeedCard(
            events = timelineSummary.events,
            onDeleteTrip = onDeleteTrip,
            onDeleteExpense = onDeleteExpense,
            onDeleteFueling = onDeleteFueling,
            onDeleteTransfer = onDeleteTransfer,
            onDeleteMaintenance = onDeleteMaintenance,
            onEditTrip = onEditTrip,
            onEditExpense = onEditExpense,
            onEditFueling = onEditFueling
        )

        Spacer(modifier = Modifier.height(72.dp))
    }
}
