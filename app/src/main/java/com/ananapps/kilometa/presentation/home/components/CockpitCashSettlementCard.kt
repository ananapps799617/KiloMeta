// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/CockpitCashSettlementCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tarjeta de cabina con arquitectura contable de 4 bolsillos soberanos (Orden #033):
// 1. 💵 Efectivo Físico en Mano
// 2. 🟣 Billetera Digital (Nequi / Daviplata)
// 3. 🖤 Billetera Uber
// 4. 🧡 Billetera DiDi
// Incluye botón interactivo de transferencia inter-billeteras y patrimonio neto.
// Regla inquebrantable: Modularidad estricta < 280 líneas de código.
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.CashSettlementSummary
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
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
    modifier: Modifier = Modifier,
    onTransferClick: () -> Unit = {}
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cabecera con Título y Botón Transferir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💼 4 BOLSILLOS SOBERANOS",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
                OutlinedButton(
                    onClick = onTransferClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBluePrimary),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "⇄ Transferir",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                }
            }

            // Fila 1: Líquidos Personales (Efectivo y Digital)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CockpitPocketBox(
                    title = "💵 Efectivo Físico",
                    subtitle = "En mano",
                    amount = settlement.cashPhysicalBalance,
                    accentColor = StatusGreen,
                    modifier = Modifier.weight(1f)
                )
                CockpitPocketBox(
                    title = "🟣 Digital (Nequi)",
                    subtitle = "Billetera digital",
                    amount = settlement.digitalWalletBalance,
                    accentColor = Color(0xFFCE93D8),
                    modifier = Modifier.weight(1f)
                )
            }

            // Fila 2: Plataformas Estancas (Uber y DiDi)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CockpitPocketBox(
                    title = "🖤 Uber",
                    subtitle = if (settlement.uberWalletBalance >= 0) "A favor" else "Deuda app",
                    amount = settlement.uberWalletBalance,
                    accentColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                CockpitPocketBox(
                    title = "🧡 DiDi",
                    subtitle = if (settlement.didiWalletBalance >= 0) "A favor" else "Deuda app",
                    amount = settlement.didiWalletBalance,
                    accentColor = Color(0xFFFF7A00),
                    modifier = Modifier.weight(1f)
                )
            }

            // Patrimonio Neto Total
            val isTotalPositive = settlement.totalNetAssets >= 0
            val totalColor = if (isTotalPositive) StatusGreen else StatusRed
            val totalBg = if (isTotalPositive) StatusGreenContainer else StatusRedContainer

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(totalBg)
                    .border(1.dp, totalColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "💰 PATRIMONIO NETO LÍQUIDO",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = totalColor
                        )
                        Text(
                            text = "Suma real de los 4 compartimentos",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = copFormat.format(settlement.totalNetAssets),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = totalColor
                    )
                }
            }
        }
    }
}
