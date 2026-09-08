// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/CockpitCashSettlementCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tarjeta de cabina que muestra el desglose de doble bolsillo contable:
// 1. Dinero físico líquido en mano (recaudo cash menos gastos y combustible).
// 2. Saldo retenido en plataformas (Uber/DiDi) que será desembolsado en el corte semanal.
// Regla inquebrantable: Archivo pedagógico < 280 líneas.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        }
    }
}
