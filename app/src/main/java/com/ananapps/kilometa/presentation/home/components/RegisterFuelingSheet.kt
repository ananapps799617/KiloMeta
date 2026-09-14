// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterFuelingSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet express adaptable al sistema de propulsión vehicular del conductor
// (Gasolina, GNV, Bi-Combustible, Diésel, 100% Eléctrico EV).
// Permite transcripción 100% editable de lo que marca el surtidor / estación de carga.
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.WalletType
import com.ananapps.kilometa.presentation.theme.CyanSavings
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.FuelGas
import com.ananapps.kilometa.presentation.theme.FuelGasoline
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: HOJA MODAL ADAPTABLE AL TIPO DE PROPULSIÓN
// ------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterFuelingSheet(
    currentOdometer: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    propulsionType: PropulsionType = PropulsionType.BI_FUEL,
    onConfirmFueling: (fuelType: FuelType, amountPaid: Double, volume: Double, odometer: Int, paymentSource: WalletType) -> Unit
) {
    val availableFuels = when (propulsionType) {
        PropulsionType.ELECTRIC_100 -> listOf(FuelType.ELECTRICITY)
        PropulsionType.DIESEL -> listOf(FuelType.DIESEL)
        PropulsionType.GASOLINE_ONLY -> listOf(FuelType.GASOLINE)
        PropulsionType.GAS_ONLY -> listOf(FuelType.GAS)
        PropulsionType.BI_FUEL -> listOf(FuelType.GAS, FuelType.GASOLINE)
    }

    var selectedFuelType by remember(propulsionType) { mutableStateOf(availableFuels.first()) }
    var paymentSource by remember { mutableStateOf(WalletType.CASH_PHYSICAL) }
    var amountText by remember(selectedFuelType) {
        val defaultAmt = when (selectedFuelType) {
            FuelType.GAS -> "25000"
            FuelType.GASOLINE -> "50000"
            FuelType.DIESEL -> "60000"
            FuelType.ELECTRICITY -> "25000"
        }
        mutableStateOf(defaultAmt)
    }
    var volumeText by remember(selectedFuelType) { mutableStateOf("11.36") }
    var odometerText by remember { mutableStateOf(currentOdometer.toString()) }

    val refPrice = when (selectedFuelType) {
        FuelType.GAS -> 2_200.0        // COP / m³
        FuelType.GASOLINE -> 15_500.0  // COP / Galón
        FuelType.DIESEL -> 10_500.0    // COP / Galón
        FuelType.ELECTRICITY -> 1_200.0 // COP / kWh
    }

    fun onAmountUpdated(newAmountStr: String) {
        amountText = newAmountStr
        val amt = newAmountStr.toDoubleOrNull() ?: 0.0
        if (refPrice > 0.0 && amt > 0.0) {
            val estimated = amt / refPrice
            volumeText = String.format(Locale.US, "%.2f", estimated)
        }
    }

    val finalAmount = amountText.toDoubleOrNull() ?: 0.0
    val finalVolume = volumeText.toDoubleOrNull() ?: 0.0
    val finalOdometer = odometerText.filter { it.isDigit() }.toIntOrNull() ?: currentOdometer

    val title = when (propulsionType) {
        PropulsionType.ELECTRIC_100 -> "REGISTRAR RECARGA ELÉCTRICA"
        PropulsionType.DIESEL -> "REGISTRAR TANQUEO DIÉSEL (ACPM)"
        PropulsionType.GASOLINE_ONLY -> "REGISTRAR TANQUEADA GASOLINA"
        PropulsionType.GAS_ONLY -> "REGISTRAR TANQUEADA GNV"
        PropulsionType.BI_FUEL -> "REGISTRAR TANQUEADA BI-MODAL"
    }

    val themeColor = when (selectedFuelType) {
        FuelType.GAS -> FuelGas
        FuelType.GASOLINE -> FuelGasoline
        FuelType.DIESEL -> Color(0xFFFFB300)
        FuelType.ELECTRICITY -> CyanSavings
    }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = themeColor)

            // Selector Bi-Combustible solo si el vehículo soporta múltiples combustibles
            if (availableFuels.size > 1) {
                Text(text = "Combustible Utilizado", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    availableFuels.forEach { fuelType ->
                        FilterChip(
                            selected = selectedFuelType == fuelType,
                            onClick = {
                                selectedFuelType = fuelType
                                onAmountUpdated(if (fuelType == FuelType.GAS) "25000" else "50000")
                            },
                            label = { Text("${fuelType.displayName} (${fuelType.unitLabel})", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (fuelType == FuelType.GAS) FuelGas else FuelGasoline,
                                selectedLabelColor = DeepObsidianBackground
                            )
                        )
                    }
                }
            }

            // Monto de Tanqueo / Recarga
            OutlinedTextField(
                value = amountText,
                onValueChange = { onAmountUpdated(it.filter { ch -> ch.isDigit() }) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Text("$", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = themeColor, modifier = Modifier.padding(start = 12.dp, end = 4.dp)) },
                label = { Text("Monto Pagado ($ COP)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Chips Rápidos Contextuales
            val chips = when (selectedFuelType) {
                FuelType.GAS -> listOf(20_000.0, 25_000.0, 30_000.0, 35_000.0)
                FuelType.GASOLINE -> listOf(30_000.0, 50_000.0, 70_000.0, 100_000.0)
                FuelType.DIESEL -> listOf(30_000.0, 50_000.0, 80_000.0, 120_000.0)
                FuelType.ELECTRICITY -> listOf(15_000.0, 25_000.0, 40_000.0, 60_000.0)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chips.forEach { chipAmount ->
                    SuggestionChip(
                        onClick = { onAmountUpdated(chipAmount.toInt().toString()) },
                        label = { Text("$${chipAmount.toInt() / 1000}k", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Volumen Despachado 100% Editable y Odómetro
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = volumeText,
                    onValueChange = { volumeText = it.replace(',', '.') },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text(if (selectedFuelType == FuelType.ELECTRICITY) "Energía (kWh)" else "Volumen (${selectedFuelType.unitLabel})") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = odometerText,
                    onValueChange = { odometerText = it.filter { ch -> ch.isDigit() } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Odómetro Actual") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Origen de Fondos (Bolsillo de Pago)
            Text(text = "Origen de Fondos", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = paymentSource == WalletType.CASH_PHYSICAL,
                    onClick = { paymentSource = WalletType.CASH_PHYSICAL },
                    label = { Text("💵 Efectivo Físico", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = paymentSource == WalletType.DIGITAL_WALLET,
                    onClick = { paymentSource = WalletType.DIGITAL_WALLET },
                    label = { Text("🟣 Billetera Digital", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Botón de Confirmación Ergonómico (>= 56dp)
            Button(
                onClick = {
                    if (finalAmount > 0.0 && finalVolume > 0.0) {
                        onConfirmFueling(selectedFuelType, finalAmount, finalVolume, finalOdometer, paymentSource)
                        onDismiss()
                    }
                },
                enabled = finalAmount > 0.0 && finalVolume > 0.0,
                modifier = Modifier.fillMaxWidth().height(Constants.MIN_TOUCH_TARGET_DP.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedFuelType == FuelType.ELECTRICITY) CyanSavings else StatusRed)
            ) {
                val actionLabel = if (selectedFuelType == FuelType.ELECTRICITY) "Registrar Recarga" else "Registrar Tanqueo"
                Text(
                    text = "$actionLabel (-${formatCop(finalAmount)})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (selectedFuelType == FuelType.ELECTRICITY) DeepObsidianBackground else TextPrimary
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: FORMATEADOR DE MONEDA
// ------------------------------------------------------------------------------
private fun formatCop(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return String.format(Locale.GERMANY, "$%,d COP", rounded)
}
