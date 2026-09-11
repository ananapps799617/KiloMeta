// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterTripSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet express rediseñado para captura de carreras a 3 toques en cabina.
// Incorpora selector dual (Modo Bruto Cobrado vs Modo Ganancia Neta), ajuste fino de comisión
// en vivo con TripFinancialSummaryRow, chips de plataforma 2x2 y botón ergonómico >= 56dp.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformConfig
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterTripSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirmTrip: (
        platform: PlatformType,
        gross: Double,
        commission: Double,
        payment: PaymentMethod,
        destination: PaymentDestination
    ) -> Unit
) {
    var selectedPlatform by remember { mutableStateOf(PlatformType.UBER) }
    var grossText by remember { mutableStateOf("15000") }
    var isGrossMode by remember { mutableStateOf(true) }
    var selectedPayment by remember { mutableStateOf(PaymentMethod.CASH) }
    var selectedDestination by remember {
        mutableStateOf(PlatformConfig.getRule(PlatformType.UBER).defaultDestination)
    }

    var customCommissionPct by remember(selectedPlatform) {
        mutableStateOf(PlatformConfig.getRule(selectedPlatform).referenceCommissionPct)
    }
    var exactCommissionOverride by remember(selectedPlatform, grossText, isGrossMode) {
        mutableStateOf<Double?>(null)
    }

    val enteredNumeric = grossText.toDoubleOrNull() ?: 0.0
    val rule = PlatformConfig.getRule(selectedPlatform)

    val (finalGross, finalCommission, finalNet) = remember(
        isGrossMode, enteredNumeric, customCommissionPct, exactCommissionOverride
    ) {
        if (isGrossMode) {
            val gross = enteredNumeric
            val commission = exactCommissionOverride ?: (gross * (customCommissionPct / 100.0))
            val net = (gross - commission).coerceAtLeast(0.0)
            Triple(gross, commission, net)
        } else {
            val net = enteredNumeric
            val gross = if (customCommissionPct < 100.0) net / (1.0 - customCommissionPct / 100.0) else net
            val commission = exactCommissionOverride ?: (gross - net).coerceAtLeast(0.0)
            Triple(gross, commission, net)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = DeepObsidianSurface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "REGISTRAR CARRERA EXPRESS",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ElectricBluePrimary
            )

            // Selector de Plataforma en Grilla 2x2
            Text(text = "Plataforma de Servicio", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PlatformType.entries.chunked(2).forEach { rowPlatforms ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowPlatforms.forEach { platform ->
                            PlatformChipItem(platform, selectedPlatform == platform, Modifier.weight(1f)) {
                                selectedPlatform = platform
                                selectedDestination = PlatformConfig.getRule(platform).defaultDestination
                                customCommissionPct = PlatformConfig.getRule(platform).referenceCommissionPct
                                exactCommissionOverride = null
                            }
                        }
                    }
                }
            }

            // Segmentador Dual de Captura (Modo Bruto vs Modo Neto)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = isGrossMode,
                    onClick = { isGrossMode = true; exactCommissionOverride = null },
                    label = { Text("💵 Lo que cobré", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ElectricBluePrimary, selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = !isGrossMode,
                    onClick = { isGrossMode = false; exactCommissionOverride = null },
                    label = { Text("💳 Ganancia en App", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusGreen, selectedLabelColor = Color.Black)
                )
            }

            // Destino del Ingreso
            Text(text = "Destino del Ingreso", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val isCash = selectedDestination == PaymentDestination.CASH_IN_HAND
                FilterChip(
                    selected = isCash,
                    onClick = { selectedDestination = PaymentDestination.CASH_IN_HAND },
                    label = { Text("💵 En Mano", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusGreen, selectedLabelColor = DeepObsidianBackground)
                )
                FilterChip(
                    selected = !isCash,
                    onClick = { selectedDestination = PaymentDestination.APP_RETAINED },
                    label = { Text("📱 En App (${rule.disbursementDay})", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ElectricBluePrimary, selectedLabelColor = DeepObsidianBackground)
                )
            }

            // Campo de Entrada Numérica
            OutlinedTextField(
                value = grossText,
                onValueChange = { grossText = it.filter { ch -> ch.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Text("$", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ElectricBluePrimary, modifier = Modifier.padding(start = 12.dp, end = 4.dp)) },
                label = { Text(if (isGrossMode) "Efectivo Recibido ($ COP)" else "Ganancia Neta en Pantalla ($ COP)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Fila de SuggestionChips rápidos
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(8_000.0, 10_000.0, 15_000.0, 20_000.0).forEach { chipAmount ->
                    SuggestionChip(
                        onClick = { grossText = chipAmount.toInt().toString() },
                        label = { Text("+${chipAmount.toInt() / 1000}k", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Selector de Medio de Recaudo
            Text(text = "Medio de Recaudo", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(PaymentMethod.CASH to "Efectivo", PaymentMethod.NEQUI to "Nequi", PaymentMethod.APP_WALLET to "App Wallet").forEach { (method, label) ->
                    FilterChip(
                        selected = selectedPayment == method,
                        onClick = { selectedPayment = method },
                        label = { Text(label, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusGreen, selectedLabelColor = DeepObsidianBackground)
                    )
                }
            }

            // Resumen Contable Interactivo con Ajuste Fino
            TripFinancialSummaryRow(
                platform = selectedPlatform,
                calculatedGross = finalGross,
                calculatedCommission = finalCommission,
                calculatedNet = finalNet,
                commissionPct = customCommissionPct,
                paymentDestination = selectedDestination,
                onCommissionPctChange = {
                    customCommissionPct = it
                    exactCommissionOverride = null
                },
                onExactCommissionChange = { exactCommissionOverride = it }
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Botón de Confirmación Ergonómico (>= 56dp)
            Button(
                onClick = {
                    if (finalGross > 0.0) {
                        onConfirmTrip(selectedPlatform, finalGross, finalCommission, selectedPayment, selectedDestination)
                        onDismiss()
                    }
                },
                enabled = finalGross > 0.0,
                modifier = Modifier.fillMaxWidth().height(Constants.MIN_TOUCH_TARGET_DP.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
            ) {
                Text(
                    text = "Guardar Carrera (+${formatCop(finalNet)})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = DeepObsidianBackground
                )
            }
        }
    }
}

@Composable
private fun PlatformChipItem(
    platform: PlatformType,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = FilterChip(
    selected = isSelected,
    onClick = onClick,
    label = { Text(text = platform.displayName, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
    modifier = modifier,
    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ElectricBluePrimary, selectedLabelColor = DeepObsidianBackground)
)

private fun formatCop(amount: Double): String = String.format(Locale.GERMANY, "$%,d COP", kotlin.math.round(amount).toLong())
