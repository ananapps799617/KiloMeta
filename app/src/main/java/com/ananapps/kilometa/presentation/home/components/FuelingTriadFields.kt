// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/FuelingTriadFields.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Campos de entrada para la tríada de surtidor (Precio ↔ Total ↔ Volumen) y odómetro.
// Desacopla la lógica de captura matemática de RegisterFuelingDialog (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.FuelType
import java.util.Locale

@Composable
fun FuelingTriadFields(
    selectedFuelType: FuelType,
    priceUnitText: String,
    onPriceUnitChange: (String) -> Unit,
    amountText: String,
    onAmountChange: (String) -> Unit,
    volumeText: String,
    onVolumeChange: (String) -> Unit,
    odometerText: String,
    onOdometerChange: (String) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val finalAmount = amountText.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
    val finalVolume = volumeText.replace(',', '.').toDoubleOrNull() ?: 0.0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = priceUnitText,
                onValueChange = { input ->
                    val clean = input.filter { it.isDigit() }
                    onPriceUnitChange(clean)
                    val pr = clean.toDoubleOrNull() ?: 0.0
                    if (pr > 0.0 && finalAmount > 0.0) {
                        onVolumeChange(String.format(Locale.US, "%.2f", finalAmount / pr))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Precio / ${selectedFuelType.unitLabel}") },
                placeholder = { Text("0") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    val clean = input.filter { it.isDigit() }
                    onAmountChange(clean)
                    val amt = clean.toDoubleOrNull() ?: 0.0
                    val pr = priceUnitText.toDoubleOrNull() ?: 0.0
                    if (pr > 0.0 && amt > 0.0) {
                        onVolumeChange(String.format(Locale.US, "%.2f", amt / pr))
                    } else if (finalVolume > 0.0 && amt > 0.0) {
                        onPriceUnitChange(String.format(Locale.US, "%.0f", amt / finalVolume))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Text("$", color = accentColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                },
                label = { Text("Total Pagado ($)") },
                modifier = Modifier.weight(1.2f),
                singleLine = true
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = volumeText,
                onValueChange = { input ->
                    val clean = input.replace(',', '.')
                    onVolumeChange(clean)
                    val vol = clean.toDoubleOrNull() ?: 0.0
                    val pr = priceUnitText.toDoubleOrNull() ?: 0.0
                    if (vol > 0.0 && pr > 0.0) {
                        onAmountChange(String.format(Locale.US, "%.0f", vol * pr))
                    } else if (vol > 0.0 && finalAmount > 0.0) {
                        onPriceUnitChange(String.format(Locale.US, "%.0f", finalAmount / vol))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                label = { Text("Volumen (${selectedFuelType.unitLabel})") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = odometerText,
                onValueChange = { onOdometerChange(it.filter { ch -> ch.isDigit() }) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Odómetro (km)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}
