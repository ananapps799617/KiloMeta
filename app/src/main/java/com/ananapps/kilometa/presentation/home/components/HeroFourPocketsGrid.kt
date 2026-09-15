// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/HeroFourPocketsGrid.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Cuadrícula compacta 2x2 para los 4 Bolsillos Soberanos integrada en la Hero Card (Orden #038).
// Muestra en cabecera el saldo real de Efectivo Físico, Nequi/Davi, Uber y DiDi,
// junto al botón directo para disparar el diálogo flotante de transferencias.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.CashSettlementSummary
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun HeroFourPocketsGrid(
    cashSettlement: CashSettlementSummary?,
    onTransferClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cash = cashSettlement?.cashPhysicalBalance ?: 0.0
    val digital = cashSettlement?.digitalWalletBalance ?: 0.0
    val uber = cashSettlement?.uberWalletBalance ?: 0.0
    val didi = cashSettlement?.didiWalletBalance ?: 0.0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Fila de Título y Botón de Transferencia
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TUS 4 BOLSILLOS SOBERANOS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = TextSecondary
            )

            FilledTonalButton(
                onClick = onTransferClick,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = ElectricBluePrimary.copy(alpha = 0.15f),
                    contentColor = ElectricBluePrimary
                ),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = "⇄ Transferir",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Fila 1: Efectivo Físico & Nequi/Daviplata
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PocketItemCell(
                icon = "💵",
                label = "Efectivo Físico",
                amount = cash,
                accentColor = StatusGreen,
                modifier = Modifier.weight(1f)
            )
            PocketItemCell(
                icon = "🟣",
                label = "Nequi / Davi",
                amount = digital,
                accentColor = Color(0xFFBA68C8),
                modifier = Modifier.weight(1f)
            )
        }

        // Fila 2: Billetera Uber & Billetera DiDi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PocketItemCell(
                icon = "🖤",
                label = "Billetera Uber",
                amount = uber,
                accentColor = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            PocketItemCell(
                icon = "🧡",
                label = "Billetera DiDi",
                amount = didi,
                accentColor = Color(0xFFFF7A00),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PocketItemCell(
    icon: String,
    label: String,
    amount: Double,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DeepObsidianSurfaceVariant)
            .border(1.dp, DeepObsidianBorder.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = icon, fontSize = 12.sp)
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "$ ${String.format(Locale.US, "%,.0f", amount)}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = if (amount >= 0) accentColor else StatusRed,
                maxLines = 1
            )
        }
    }
}
