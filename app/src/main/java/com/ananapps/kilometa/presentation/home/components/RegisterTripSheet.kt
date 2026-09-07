// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterTripSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet express rediseñado para captura de carreras a 3 toques en cabina.
// Incorpora grilla 2x2 para plataformas de transporte (previniendo cortes de texto),
// campo editable híbrido con chips rápidos (+10k, +15k, +20k, +30k), resumen contable
// en vivo (Comisión y Neto a Caja) y botón táctil ergonómico >= 56dp.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: HOJA MODAL EXPRESS PARA REGISTRO DE CARRERAS
// ------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterTripSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirmTrip: (platform: PlatformType, gross: Double, commission: Double, payment: PaymentMethod) -> Unit
) {
    var selectedPlatform by remember { mutableStateOf(PlatformType.UBER) }
    var grossText by remember { mutableStateOf("15000") }
    var selectedPayment by remember { mutableStateOf(PaymentMethod.CASH) }

    val grossNumeric = grossText.toDoubleOrNull() ?: 0.0
    val defaultCommissionRate = when (selectedPlatform) {
        PlatformType.UBER -> 0.25
        PlatformType.DIDI -> 0.20
        PlatformType.INDRIVE -> 0.10
        PlatformType.PRIVATE -> 0.00
    }
    val calculatedCommission = grossNumeric * defaultCommissionRate
    val calculatedNet = grossNumeric - calculatedCommission

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidianSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "REGISTRAR CARRERA EXPRESS",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ElectricBluePrimary
            )

            // Selector de Plataforma en Grilla 2x2 (Erradica desbordamiento "Particula / r")
            Text(text = "Plataforma de Servicio", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlatformChipItem(PlatformType.UBER, selectedPlatform == PlatformType.UBER, Modifier.weight(1f)) { selectedPlatform = PlatformType.UBER }
                    PlatformChipItem(PlatformType.DIDI, selectedPlatform == PlatformType.DIDI, Modifier.weight(1f)) { selectedPlatform = PlatformType.DIDI }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlatformChipItem(PlatformType.INDRIVE, selectedPlatform == PlatformType.INDRIVE, Modifier.weight(1f)) { selectedPlatform = PlatformType.INDRIVE }
                    PlatformChipItem(PlatformType.PRIVATE, selectedPlatform == PlatformType.PRIVATE, Modifier.weight(1f)) { selectedPlatform = PlatformType.PRIVATE }
                }
            }

            // Monto Bruto Híbrido: Campo editable con leadingIcon "$"
            OutlinedTextField(
                value = grossText,
                onValueChange = { grossText = it.filter { ch -> ch.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Text("$", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ElectricBluePrimary, modifier = Modifier.padding(start = 12.dp, end = 4.dp)) },
                label = { Text("Monto de la Carrera ($ COP)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Fila de SuggestionChips rápidos (+10k, +15k, +20k, +30k)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10_000.0, 15_000.0, 20_000.0, 30_000.0).forEach { chipAmount ->
                    SuggestionChip(
                        onClick = { grossText = chipAmount.toInt().toString() },
                        label = { Text("+${chipAmount.toInt() / 1000}k", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Selector de Modalidad de Pago
            Text(text = "Modalidad de Pago", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    PaymentMethod.CASH to "Efectivo",
                    PaymentMethod.NEQUI to "Nequi",
                    PaymentMethod.APP_WALLET to "Billetera"
                ).forEach { (method, label) ->
                    FilterChip(
                        selected = selectedPayment == method,
                        onClick = { selectedPayment = method },
                        label = { Text(label, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusGreen, selectedLabelColor = DeepObsidianBackground)
                    )
                }
            }

            // Resumen en vivo de Comisión y Neto a Caja
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Comisión: -${formatCop(calculatedCommission)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(text = "Neto a Caja: +${formatCop(calculatedNet)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = StatusGreen)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Botón de Confirmación Ergonómico (>= 56dp)
            Button(
                onClick = {
                    if (grossNumeric > 0.0) {
                        onConfirmTrip(selectedPlatform, grossNumeric, calculatedCommission, selectedPayment)
                        onDismiss()
                    }
                },
                enabled = grossNumeric > 0.0,
                modifier = Modifier.fillMaxWidth().height(Constants.MIN_TOUCH_TARGET_DP.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
            ) {
                Text(
                    text = "Guardar Carrera (+${formatCop(calculatedNet)})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = DeepObsidianBackground
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: ITEM DE CHIP DE PLATAFORMA PARA GRILLA 2X2
// ------------------------------------------------------------------------------
@Composable
private fun PlatformChipItem(
    platform: PlatformType,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = platform.displayName, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ElectricBluePrimary,
            selectedLabelColor = DeepObsidianBackground
        )
    )
}

// ------------------------------------------------------------------------------
// BLOQUE 3: FORMATEADOR DE MONEDA
// ------------------------------------------------------------------------------
private fun formatCop(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return String.format(Locale.GERMANY, "$%,d COP", rounded)
}
