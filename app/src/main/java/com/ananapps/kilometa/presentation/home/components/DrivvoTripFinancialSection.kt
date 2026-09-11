// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/DrivvoTripFinancialSection.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Sección financiera ergonómica estilo Drivvo: captura directa de "¿Cuánto recibí?"
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
fun DrivvoTripFinancialSection(
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
                onValueChange = onCommissionAmountChange,
                label = { Text("¿Te descuentan?") },
                placeholder = { Text("Comisión $") },
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

                if (gross > 0.0 && commission > 0.0) {
                    val pct = (commission / gross) * 100.0
                    Text(
                        text = "Comisión: ${"%.1f".format(pct)}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = StatusAmber
                    )
                }
            }
        }

        // 3. Medio de Recaudo (Efectivo vs Digital)
        val isCash = selectedPaymentMethod == PaymentMethod.CASH
        val isDigital = selectedPaymentMethod != PaymentMethod.CASH

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Medio de cobro",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

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
                            text = "💵 Efectivo (En Mano)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isCash) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCash) StatusGreen else TextSecondary
                            )
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = if (isDigital) 2.dp else 1.dp,
                            color = if (isDigital) ElectricBluePrimary else TextSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onPaymentMethodSelected(PaymentMethod.APP_WALLET) },
                    color = if (isDigital) ElectricBluePrimary.copy(alpha = 0.15f) else DeepObsidianSurfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "📱 Digital / Saldo App",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isDigital) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDigital) ElectricBluePrimary else TextSecondary
                            )
                        )
                    }
                }
            }

            if (isCash && commission > 0.0) {
                Text(
                    text = "⚠️ Al cobrar en efectivo, la comisión de ${currencyFormatter.format(commission)} queda como deuda pendiente con la app.",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusAmber
                )
            }
        }
    }
}
