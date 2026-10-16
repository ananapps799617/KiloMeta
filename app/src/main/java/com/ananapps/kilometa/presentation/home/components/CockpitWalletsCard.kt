// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/CockpitWalletsCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Sección "Bolsillos" de cabina Cockpit Dark (99.9% fiel a la lámina de diseño).
// Encabezado limpio exterior con acción en verde y grilla simétrica 2×2 de tarjetas
// individuales con icono cuadrado, nombre y saldo legible (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.ananapps.kilometa.core.util.ColorUtils
import com.ananapps.kilometa.domain.model.CashSettlementSummary
import com.ananapps.kilometa.domain.model.PocketType
import com.ananapps.kilometa.domain.model.Wallet
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun CockpitWalletsCard(
    settlement: CashSettlementSummary?,
    wallets: List<Wallet> = emptyList(),
    cycleLabel: String? = null,
    onTransferClick: () -> Unit,
    onPocketClick: ((PocketType) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Encabezado Exterior Limpio: "💼 Bolsillos" y "⇄ Transferir / Ver todos"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "💼", fontSize = 16.sp)
                Text(
                    text = "Bolsillos",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = TextPrimary
                )
            }

            Text(
                text = "⇄ Transferir",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = StatusGreen,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTransferClick() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }

        // 2. Grilla 2x2 de Bolsillos Individuales
        if (wallets.isNotEmpty()) {
            val displayWallets = wallets.take(4)
            displayWallets.chunked(2).forEach { rowPair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowPair.forEach { w ->
                        val chipColor = ColorUtils.parseHexColor(w.colorHex)
                        WalletCockpitCard(
                            name = w.name,
                            balance = w.balance,
                            iconGlyph = w.iconName.ifBlank { "💵" },
                            accentColor = chipColor,
                            modifier = Modifier.weight(1f)
                        ) {
                            mapWalletIdToPocketType(w.id)?.let { onPocketClick?.invoke(it) }
                        }
                    }
                    if (rowPair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            val effective = settlement ?: CashSettlementSummary()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WalletCockpitCard("Efectivo", effective.cashPhysicalBalance, "💵", StatusGreen, Modifier.weight(1f)) {
                    onPocketClick?.invoke(PocketType.CASH_PHYSICAL)
                }
                WalletCockpitCard("Billetera Digital", effective.digitalWalletBalance, "💳", StatusGreen, Modifier.weight(1f)) {
                    onPocketClick?.invoke(PocketType.DIGITAL_WALLET)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WalletCockpitCard("Uber", effective.uberWalletBalance, "🖤", TextPrimary, Modifier.weight(1f)) {
                    onPocketClick?.invoke(PocketType.UBER)
                }
                WalletCockpitCard("DiDi", effective.didiWalletBalance, "🧡", StatusAmber, Modifier.weight(1f)) {
                    onPocketClick?.invoke(PocketType.DIDI)
                }
            }
        }
    }
}

@Composable
private fun WalletCockpitCard(
    name: String,
    balance: Double,
    iconGlyph: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DeepObsidianSurface)
            .border(1.dp, DeepObsidianBorder, RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Recuadro de Icono coloreado a la izquierda
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconGlyph, fontSize = 16.sp)
            }

            // Datos: Nombre arriba y saldo abajo
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$ ${String.format(Locale.US, "%,.0f", balance)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ),
                    color = if (balance >= 0) TextPrimary else StatusRed,
                    maxLines = 1
                )
            }
        }
    }
}

private fun mapWalletIdToPocketType(id: String): PocketType? = when (id) {
    "cash_physical" -> PocketType.CASH_PHYSICAL
    "digital_wallet" -> PocketType.DIGITAL_WALLET
    "uber" -> PocketType.UBER
    "didi" -> PocketType.DIDI
    "car_quota" -> PocketType.CAR_QUOTA
    else -> null
}
