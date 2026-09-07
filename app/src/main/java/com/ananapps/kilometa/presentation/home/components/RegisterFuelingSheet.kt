// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterFuelingSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet express para registro de abastecimiento bi-modal (GNV vs Gasolina).
// Provee selector de combustible, chips de montos usuales en estación de servicio,
// estimación automática de volumen y captura del odómetro para costeo kilométrico.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
// BLOQUE 1: BOTTOM SHEET BI-MODAL DE COMBUSTIBLE
// ------------------------------------------------------------------------------
/**
 * Hoja modal de captura de tanqueo para Gas Natural Vehicular y Gasolina.
 *
 * @param currentOdometer Último odómetro registrado para sugerencia de lectura.
 * @param sheetState Estado del ModalBottomSheet.
 * @param onDismiss Callback de cancelación.
 * @param onConfirmFueling Callback al confirmar abastecimiento bi-modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterFuelingSheet(
    currentOdometer: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirmFueling: (fuelType: FuelType, amountPaid: Double, volume: Double, odometer: Int) -> Unit
) {
    var selectedFuelType by remember { mutableStateOf(FuelType.GAS) }
    var amountPaid by remember { mutableDoubleStateOf(25_000.0) }
    var odometerInput by remember { mutableIntStateOf(currentOdometer) }

    // Precios de referencia de mercado en Colombia para cálculo automático de volumen
    // GNV ~ $2.200 COP / m³  |  Gasolina Corriente ~ $15.500 COP / Galón
    val referencePricePerUnit = if (selectedFuelType == FuelType.GAS) 2_200.0 else 15_500.0
    val estimatedVolume = if (referencePricePerUnit > 0.0) amountPaid / referencePricePerUnit else 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidianSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
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
                            amountPaid = if (isGas) 25_000.0 else 50_000.0
                        },
                        label = { Text("${fuelType.displayName} (${fuelType.unitLabel})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isGas) FuelGas else FuelGasoline,
                            selectedLabelColor = DeepObsidianBackground
                        )
                    )
                }
            }

            // Monto Pagado con Chips Rápidos
            Text(
                text = "Monto Tanqueo: ${formatCop(amountPaid)}",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
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
                        onClick = { amountPaid = chipAmount },
                        label = { Text("$${chipAmount.toInt() / 1000}k") }
                    )
                }
            }

            // Volumen calculado y Odómetro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = String.format(Locale.US, "%.2f %s", estimatedVolume, selectedFuelType.unitLabel),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Volumen Est.") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = odometerInput.toString(),
                    onValueChange = { newValue ->
                        newValue.filter { it.isDigit() }.toIntOrNull()?.let { odometerInput = it }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Odómetro Actual") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Botón de Guardado Ergonómico (>= 56dp)
            Button(
                onClick = {
                    onConfirmFueling(selectedFuelType, amountPaid, estimatedVolume, odometerInput)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
            ) {
                Text(
                    text = "Registrar Tanqueo (-${formatCop(amountPaid)})",
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
