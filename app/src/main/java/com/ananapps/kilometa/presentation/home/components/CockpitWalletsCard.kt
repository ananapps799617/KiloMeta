// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/CockpitWalletsCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tarjeta noble e independiente de cabina para los 4 Bolsillos Soberanos (Micro-Orden #038.2).
// Desacoplada de DayHeroCard para dar aire, espacio negativo y acceso vehicular a transferencias.
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
fun CockpitWalletsCard(
    settlement: CashSettlementSummary?,
    cycleLabel: String? = null,
    onTransferClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effective = settlement ?: CashSettlementSummary()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "💼 TUS 5 BOLSILLOS SOBERANOS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ElectricBluePrimary
                    )
                    if (!cycleLabel.isNullOrBlank()) {
                        Text(
                            text = cycleLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                AssistChip(
                    onClick = onTransferClick,
                    label = { Text("⇄ Transferir", fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = ElectricBluePrimary.copy(alpha = 0.15f),
                        labelColor = ElectricBluePrimary
                    ),
                    border = null
                )
            }

            // Bolsillo 5: Cuota Carro (Apartado Semanal Sagrado)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DeepObsidianSurfaceVariant)
                    .border(1.dp, ElectricBluePrimary.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🚘", fontSize = 18.sp)
                        Column {
                            Text(
                                text = "Cuota Carro (Apartado)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "$ ${String.format(Locale.US, "%,.0f", effective.carQuotaWalletBalance)}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (effective.carQuotaWalletBalance >= 0) ElectricBluePrimary else StatusRed
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricBluePrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Semanal",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = ElectricBluePrimary
                        )
                    }
                }
            }

            // Cuadrícula 2x2 para Efectivo, Digital, Uber y DiDi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WalletMiniChip("💵 Efectivo Físico", effective.cashPhysicalBalance, StatusGreen, Modifier.weight(1f))
                WalletMiniChip("🟣 Nequi / Davi", effective.digitalWalletBalance, PlatformPrivate, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WalletMiniChip("🖤 Billetera Uber", effective.uberWalletBalance, TextPrimary, Modifier.weight(1f))
                WalletMiniChip("🧡 Billetera DiDi", effective.didiWalletBalance, StatusAmber, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WalletMiniChip(
    label: String,
    balance: Double,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DeepObsidianSurfaceVariant)
            .border(1.dp, DeepObsidianBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$ ${String.format(Locale.US, "%,.0f", balance)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = if (balance >= 0) accentColor else StatusRed,
                maxLines = 1
            )
        }
    }
}
