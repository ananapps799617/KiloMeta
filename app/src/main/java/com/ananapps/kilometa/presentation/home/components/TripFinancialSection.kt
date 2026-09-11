// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/TripFinancialSection.kt
// DESCRIPCIÃ“N PEDAGÃ“GICA:
// SecciÃ³n financiera ergonÃ³mica estilo Cockpit KiloMeta: captura directa de "Â¿CuÃ¡nto recibÃ­?"
// y "Â¿CuÃ¡nto descuentan?", cÃ¡lculo reactivo de ganancia neta y selecciÃ³n de medio de pago.
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
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    val gross = grossAmountText.toDoubleOrNull() ?: 0.0
    val commission = commissionAmountText.toDoubleOrNull() ?: 0.0
    val net = (gross - commission).coerceAtLeast(0.0)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. Campos de Monto Recibido y Descuento de App
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = grossAmountText,
                onValueChange = onGrossAmountChange,
                label = { Text("Â¿CuÃ¡nto cobraste?") },
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
                onValueChange = onCommissionAmountChange,
                label = { Text("Â¿Te descuentan?") },
                placeholder = { Text("ComisiÃ³n $") },
                prefix = { Text("$ ", fontWeight = FontWeight.Bold, color = StatusRed) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StatusAmber,
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

                if (commission > 0.0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Descuento",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "-${currencyFormatter.format(commission)}",
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
                // OpciÃ³n 1: Efectivo
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = if (isCash) 2.dp else 1.dp,
                            color = if (isCash) StatusGreen else TextSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onPaymentMethodSelected(PaymentMethod.CASH) },
                    color = if (isCash) StatusGreen.copy(alpha = 0.15f) else DeepObsidianSurfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "ðŸ’µ Efectivo",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isCash) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCash) StatusGreen else TextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // OpciÃ³n 2: Nequi / Daviplata
                Surface(
                    modifier = Modifier
                        .weight(1.15f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = if (isNequi) 2.dp else 1.dp,
                            color = if (isNequi) PlatformPrivate else TextSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onPaymentMethodSelected(PaymentMethod.NEQUI) },
                    color = if (isNequi) PlatformPrivate.copy(alpha = 0.15f) else DeepObsidianSurfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "ðŸŸ£ Nequi / Davi",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isNequi) FontWeight.Bold else FontWeight.Normal,
                                color = if (isNequi) PlatformPrivate else TextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // OpciÃ³n 3: Saldo en App
                Surface(
                    modifier = Modifier
                        .weight(1.05f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = if (isAppWallet) 2.dp else 1.dp,
                            color = if (isAppWallet) ElectricBluePrimary else TextSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onPaymentMethodSelected(PaymentMethod.APP_WALLET) },
                    color = if (isAppWallet) ElectricBluePrimary.copy(alpha = 0.15f) else DeepObsidianSurfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "ðŸ“± Saldo App",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isAppWallet) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAppWallet) ElectricBluePrimary else TextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            if ((isCash || isNequi) && commission > 0.0) {
                Text(
                    text = "âš ï¸ Al cobrar en ${if (isCash) "efectivo" else "Nequi/Daviplata"}, la comisiÃ³n de ${currencyFormatter.format(commission)} queda como deuda pendiente con la app.",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusAmber
                )
            } else if (isAppWallet && commission > 0.0) {
                Text(
                    text = "â„¹ï¸ La app descuenta su comisiÃ³n en la fuente. ${currencyFormatter.format(net)} netos ingresan a tu saldo en la app.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricBluePrimary
                )
            }
        }
    }
}

