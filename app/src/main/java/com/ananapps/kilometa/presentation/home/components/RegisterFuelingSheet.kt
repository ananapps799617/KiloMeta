// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterFuelingSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet express rediseñado para captura de tanqueos bi-modales (GNV vs Gasolina).
// Incorpora monto editable híbrido con chips contextuales, volumen 100% editable
// sincronizado con surtidor físico, odómetro numérico y botón ergonómico >= 56dp.
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.FuelGas
import com.ananapps.kilometa.presentation.theme.FuelGasoline
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: HOJA MODAL EXPRESS PARA REGISTRO DE COMBUSTIBLE
// ------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterFuelingSheet(
    currentOdometer: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirmFueling: (fuelType: FuelType, amountPaid: Double, volume: Double, odometer: Int) -> Unit
) {
    var selectedFuelType by remember { mutableStateOf(FuelType.GAS) }
    var amountText by remember { mutableStateOf("25000") }
    var volumeText by remember { mutableStateOf("11.36") }
    var odometerText by remember { mutableStateOf(currentOdometer.toString()) }

    // Precios de referencia de mercado en Colombia para cálculo automático de volumen
    // GNV ~ $2.200 COP / m³  |  Gasolina Corriente ~ $15.500 COP / Galón
    val referencePricePerUnit = if (selectedFuelType == FuelType.GAS) 2_200.0 else 15_500.0

    // Función sincronizadora: al cambiar monto por chip o teclado se recalcula el volumen sugerido
    fun onAmountUpdated(newAmountStr: String) {
        amountText = newAmountStr
        val amt = newAmountStr.toDoubleOrNull() ?: 0.0
        if (referencePricePerUnit > 0.0 && amt > 0.0) {
            val estimated = amt / referencePricePerUnit
            volumeText = String.format(Locale.US, "%.2f", estimated)
        }
    }

    val finalAmount = amountText.toDoubleOrNull() ?: 0.0
    val finalVolume = volumeText.toDoubleOrNull() ?: 0.0
    val finalOdometer = odometerText.filter { it.isDigit() }.toIntOrNull() ?: currentOdometer

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
            // Título de cabina
            Text(
                text = "REGISTRAR TANQUEADA BI-MODAL",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (selectedFuelType == FuelType.GAS) FuelGas else FuelGasoline
            )

            // Selector Bi-Combustible (GNV vs Gasolina)
            Text(
                text = "Tipo de Combustible",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FuelType.entries.forEach { fuelType ->
                    val isGas = fuelType == FuelType.GAS
                    FilterChip(
                        selected = selectedFuelType == fuelType,
                        onClick = {
                            selectedFuelType = fuelType
                            val defaultAmt = if (isGas) "25000" else "50000"
                            onAmountUpdated(defaultAmt)
                        },
                        label = {
                            Text(
                                text = "${fuelType.displayName} (${fuelType.unitLabel})",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isGas) FuelGas else FuelGasoline,
                            selectedLabelColor = DeepObsidianBackground
                        )
                    )
                }
            }

            // Monto de Tanqueo Híbrido (Campo numérico editable)
            OutlinedTextField(
                value = amountText,
                onValueChange = { newValue ->
                    onAmountUpdated(newValue.filter { it.isDigit() })
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Text(
                        text = "$",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedFuelType == FuelType.GAS) FuelGas else FuelGasoline,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                    )
                },
                label = { Text("Monto Pagado ($ COP)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Chips de apoyo contextuales (GNV vs Gasolina)
            val chips = if (selectedFuelType == FuelType.GAS) {
                listOf(20_000.0, 25_000.0, 30_000.0, 35_000.0)
            } else {
                listOf(30_000.0, 50_000.0, 70_000.0, 100_000.0)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chips.forEach { chipAmount ->
                    SuggestionChip(
                        onClick = { onAmountUpdated(chipAmount.toInt().toString()) },
                        label = { Text("$${chipAmount.toInt() / 1000}k") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Volumen Despachado 100% Editable y Odómetro Actual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Volumen Despachado (100% Editable para coincidir exactamente con el surtidor)
                OutlinedTextField(
                    value = volumeText,
                    onValueChange = { newValue ->
                        volumeText = newValue.replace(',', '.')
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text("Volumen (${selectedFuelType.unitLabel})") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                // Odómetro actual
                OutlinedTextField(
                    value = odometerText,
                    onValueChange = { newValue ->
                        odometerText = newValue.filter { it.isDigit() }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Odómetro Actual") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Botón de Confirmación Ergonómico (>= 56dp)
            Button(
                onClick = {
                    if (finalAmount > 0.0 && finalVolume > 0.0) {
                        onConfirmFueling(selectedFuelType, finalAmount, finalVolume, finalOdometer)
                        onDismiss()
                    }
                },
                enabled = finalAmount > 0.0 && finalVolume > 0.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
            ) {
                Text(
                    text = "Registrar Tanqueo (-${formatCop(finalAmount)})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
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
