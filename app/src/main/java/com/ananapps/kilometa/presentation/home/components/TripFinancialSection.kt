// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/TripFinancialSection.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Módulo financiero Cockpit Minimalista: Hero Card integrado con "TU GANANCIA LIMPIA",
// captura dual de cobro y comisión/bono, y selector ergonómico de medio de pago (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.presentation.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TripFinancialSection(
    grossAmountText: String,
    onGrossAmountChange: (String) -> Unit,
    commissionAmountText: String,
    onCommissionAmountChange: (String) -> Unit,
    selectedPaymentMethod: PaymentMethod,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    platformPromoDiscount: Double = 0.0,
    isBonus: Boolean = false,
    onIsBonusToggle: () -> Unit = {},
    platformName: String = "la app",
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    val gross = grossAmountText.toDoubleOrNull() ?: 0.0
    val appPart = commissionAmountText.toDoubleOrNull() ?: 0.0
    val effectiveBonus = isBonus || platformPromoDiscount > 0.0
    val net = if (effectiveBonus) (gross + appPart) else (gross - appPart).coerceAtLeast(0.0)

    val realPct = if (gross > 0.0 && appPart > 0.0) (appPart / gross) * 100.0 else 0.0
    val pctFormatted = if (realPct > 0.0) String.format(Locale.US, "%.1f", realPct) else null

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. Hero Card Financiero Unificado (Monto limpio + Inputs en un solo contenedor sólido)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DeepObsidianSurfaceVariant,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (net > 0.0) StatusGreen.copy(alpha = 0.45f) else TextSecondary.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fila Superior: Trofeo de Ganancia Limpia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TU GANANCIA LIMPIA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = TextSecondary
                        )
                        Text(
                            text = currencyFormatter.format(net),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                            color = if (net > 0.0) StatusGreen else TextPrimary
                        )
                    }

                    if (gross > 0.0 && appPart > 0.0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (effectiveBonus) StatusGreen.copy(alpha = 0.15f) else StatusAmber.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (effectiveBonus) "+$pctFormatted% bono" else "-$pctFormatted% app",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (effectiveBonus) StatusGreen else StatusAmber,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = TextSecondary.copy(alpha = 0.15f), thickness = 1.dp)

                // Fila de Entradas: Cobrado y Comisión/Bono
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = grossAmountText,
                        onValueChange = onGrossAmountChange,
                        label = { Text("¿Cuánto cobraste?", style = MaterialTheme.typography.labelSmall) },
                        placeholder = { Text("Ej: 15000", color = TextSecondary.copy(alpha = 0.4f)) },
                        prefix = { Text("$ ", fontWeight = FontWeight.Bold, color = StatusGreen) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DeepObsidianSurface,
                            unfocusedContainerColor = DeepObsidianSurface,
                            focusedBorderColor = StatusGreen,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.1f)
                    )

                    OutlinedTextField(
                        value = commissionAmountText,
                        onValueChange = { input ->
                            if (input.contains("+")) {
                                if (!isBonus) onIsBonusToggle()
                            } else if (input.contains("-")) {
                                if (isBonus) onIsBonusToggle()
                            }
                            val cleanDigits = input.filter { it.isDigit() }
                            onCommissionAmountChange(cleanDigits)
                        },
                        label = {
                            Text(
                                if (effectiveBonus) "Bono App (+)" else "Parte App (-)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        placeholder = { Text("0", color = TextSecondary.copy(alpha = 0.4f)) },
                        prefix = {
                            Surface(
                                onClick = onIsBonusToggle,
                                shape = RoundedCornerShape(4.dp),
                                color = if (effectiveBonus) StatusGreen.copy(alpha = 0.2f) else StatusAmber.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (effectiveBonus) "+$ " else "-$ ",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = if (effectiveBonus) StatusGreen else StatusRed,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DeepObsidianSurface,
                            unfocusedContainerColor = DeepObsidianSurface,
                            focusedBorderColor = if (effectiveBonus) StatusGreen else StatusAmber,
                            unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Medio de Recaudo (Segmented Control Cockpit de 3 estados)
        val isCash = selectedPaymentMethod == PaymentMethod.CASH
        val isNequi = selectedPaymentMethod == PaymentMethod.NEQUI
        val isAppWallet = selectedPaymentMethod == PaymentMethod.APP_WALLET

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Medio de cobro",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Triple(PaymentMethod.CASH, "💵 Efectivo", isCash to StatusGreen),
                    Triple(PaymentMethod.NEQUI, "🟣 Nequi / Davi", isNequi to PlatformPrivate),
                    Triple(PaymentMethod.APP_WALLET, "📱 Saldo App", isAppWallet to ElectricBluePrimary)
                ).forEach { (method, label, state) ->
                    val (selected, activeColor) = state
                    Surface(
                        modifier = Modifier
                            .weight(if (method == PaymentMethod.NEQUI) 1.15f else 1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) activeColor else TextSecondary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onPaymentMethodSelected(method) },
                        color = if (selected) activeColor.copy(alpha = 0.15f) else DeepObsidianSurfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) activeColor else TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            val appLabel = platformName.ifBlank { "la app" }
            val promoAmount = if (platformPromoDiscount > 0.0) platformPromoDiscount else if (effectiveBonus) appPart else 0.0
            if (promoAmount > 0.0) {
                Text(
                    text = "🎁 $appLabel abonó ${currencyFormatter.format(promoAmount)} a favor en tu Billetera $appLabel.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricBluePrimary
                )
            } else if ((isCash || isNequi) && appPart > 0.0) {
                Text(
                    text = "⚠️ Al cobrar en ${if (isCash) "efectivo" else "Nequi/Daviplata"}, la comisión de ${currencyFormatter.format(appPart)} queda como deuda pendiente con $appLabel.",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusAmber
                )
            } else if (isAppWallet && appPart > 0.0) {
                Text(
                    text = "ℹ️ $appLabel descuenta su comisión en la fuente. ${currencyFormatter.format(net)} netos ingresan a tu saldo en $appLabel.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricBluePrimary
                )
            }
        }
    }
}
