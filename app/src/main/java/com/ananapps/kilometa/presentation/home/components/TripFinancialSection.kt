// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/TripFinancialSection.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Sección financiera ergonómica estilo Cockpit KiloMeta: captura directa de "¿Cuánto recibí?"
// y "¿Cuánto descuentan?", cálculo reactivo de ganancia neta y selección de medio de pago.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
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
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    val gross = grossAmountText.toDoubleOrNull() ?: 0.0
    val appPart = commissionAmountText.toDoubleOrNull() ?: 0.0
    val effectiveBonus = isBonus || platformPromoDiscount > 0.0
    val net = if (effectiveBonus) (gross + appPart) else (gross - appPart).coerceAtLeast(0.0)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. Campos de Monto Recibido y Descuento/Bono de App
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = grossAmountText,
                onValueChange = onGrossAmountChange,
                label = { Text("¿Cuánto cobraste?") },
                placeholder = { Text("Ej: 15000") },
                prefix = { Text("$ ", fontWeight = FontWeight.Bold, color = StatusGreen) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StatusGreen,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
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
                label = { Text(if (effectiveBonus) "Bono App (+)" else "Parte de la App (-)") },
                placeholder = { Text("Comisión / Bono $") },
                prefix = {
                    Surface(
                        onClick = onIsBonusToggle,
                        shape = RoundedCornerShape(4.dp),
                        color = if (effectiveBonus) StatusGreen.copy(alpha = 0.2f) else StatusAmber.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (effectiveBonus) "+$ " else "-$ ",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = if (effectiveBonus) StatusGreen else StatusRed,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (effectiveBonus) StatusGreen else StatusAmber,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.3f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // 2. Card Hero de Ganancia Neta Real
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DeepObsidianSurfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TU GANANCIA LIMPIA",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                    Text(
                        text = currencyFormatter.format(net),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = StatusGreen
                    )
                }

                if (effectiveBonus && appPart > 0.0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Abono App",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "+${currencyFormatter.format(appPart)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = StatusGreen
                        )
                    }
                } else if (!effectiveBonus && appPart > 0.0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Descuento",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "-${currencyFormatter.format(appPart)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = StatusAmber
                        )
                    }
                }
            }
        }

        // 3. Medio de Recaudo (Efectivo vs Nequi vs Saldo en App)
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
                            .height(48.dp)
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

            if (platformPromoDiscount > 0.0) {
                Text(
                    text = "🎁 DiDi abonó ${currencyFormatter.format(platformPromoDiscount)} a favor directamente en tu Billetera DiDi.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricBluePrimary
                )
            } else if ((isCash || isNequi) && appPart > 0.0) {
                Text(
                    text = "⚠️ Al cobrar en ${if (isCash) "efectivo" else "Nequi/Daviplata"}, la comisión de ${currencyFormatter.format(appPart)} queda como deuda pendiente con la app.",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusAmber
                )
            } else if (isAppWallet && appPart > 0.0) {
                Text(
                    text = "ℹ️ La app descuenta su comisión en la fuente. ${currencyFormatter.format(net)} netos ingresan a tu saldo en la app.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricBluePrimary
                )
            }
        }
    }
}

