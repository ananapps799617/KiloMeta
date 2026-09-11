// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/CockpitCashSettlementCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tarjeta de cabina que muestra el desglose de doble bolsillo contable:
// 1. Dinero físico líquido en mano (recaudo cash menos gastos y combustible).
// 2. Saldo y billeteras independientes por plataforma (Uber, DiDi, inDrive).
// Regla inquebrantable: Archivo pedagógico < 280 líneas.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.CashSettlementSummary
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusGreenContainer
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.StatusRedContainer
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import com.ananapps.kilometa.presentation.theme.visualToken
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CockpitCashSettlementCard(
    settlement: CashSettlementSummary,
    modifier: Modifier = Modifier
) {
    val copFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💼 FLUJO DE CAJA DE CABINA",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
                Text(
                    text = "Doble Bolsillo",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bolsillo 1: Efectivo en Mano
                val isCashPositive = settlement.cashInHandAvailable >= 0
                val cashContainerColor = if (isCashPositive) StatusGreenContainer else StatusRedContainer
                val cashTextColor = if (isCashPositive) StatusGreen else StatusRed

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cashContainerColor)
                        .border(1.dp, cashTextColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "💵 En Mano (Líquido)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = cashTextColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = copFormat.format(settlement.cashInHandAvailable),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Caja física hoy",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                // Bolsillo 2: Saldo en Plataformas (Retenido)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepObsidianSurfaceVariant)
                        .border(1.dp, ElectricBluePrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "📱 Saldo en Apps",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricBluePrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = copFormat.format(settlement.digitalPendingDisbursement),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Corte semanal",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Desglose de Billeteras por Plataforma (Uber, DiDi, inDrive)
            val activeWallets = listOf(
                settlement.uberWallet,
                settlement.didiWallet,
                settlement.indriveWallet
            ).filter { it.digitalGross > 0 || it.cashCommissionOwed > 0 || it.cashGross > 0 }

            if (activeWallets.isNotEmpty()) {
                Text(
                    text = "Billeteras por Aplicación",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeWallets.forEach { wallet ->
                        val token = wallet.platformType.visualToken
                        val isPositive = wallet.netBalance >= 0
                        val valColor = if (isPositive) StatusGreen else StatusRed

                        Surface(
                            modifier = Modifier.weight(1f),
                            color = DeepObsidianSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, token.backgroundColor.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(token.backgroundColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = token.monogram,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black
                                            ),
                                            color = token.contentColor
                                        )
                                    }
                                    Text(
                                        text = token.badgeLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = (if (isPositive && wallet.netBalance > 0) "+" else "") + copFormat.format(wallet.netBalance),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = valColor
                                )
                                Text(
                                    text = if (isPositive) "A cobrar" else "Deuda app",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Cruce Global Referencial
            val isPlatformNetPositive = settlement.netPlatformBalance >= 0
            val balanceColor = if (isPlatformNetPositive) StatusGreen else StatusRed
            val balanceBg = if (isPlatformNetPositive) StatusGreenContainer else StatusRedContainer

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(balanceBg)
                    .border(1.dp, balanceColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPlatformNetPositive) "⚖️ Cruce: Saldo a favor" else "⚠️ Cruce: Comisión adeudada",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = balanceColor
                        )
                        Text(
                            text = if (isPlatformNetPositive)
                                "Tus viajes en app cubren las comisiones de efectivo"
                            else
                                "Comisión efectivo: ${copFormat.format(settlement.cashCommissionsOwed)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = copFormat.format(settlement.netPlatformBalance),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = balanceColor
                    )
                }
            }
        }
    }
}
