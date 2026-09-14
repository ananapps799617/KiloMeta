// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterFuelingSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet de alta fidelidad Drivvo para registro de combustible.
// Incorpora tríada matemática reactiva (Precio ↔ Total ↔ Volumen), switch de tanque
// lleno, estación, cabecera naranja #E65100 y Swipe-Down Lockdown.
// Regla sagrada: < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.WalletType
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

private val FuelOrangeHeader = Color(0xFFE65100)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterFuelingSheet(
    currentOdometer: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    propulsionType: PropulsionType = PropulsionType.BI_FUEL,
    onConfirmFueling: (fuelType: FuelType, amountPaid: Double, volume: Double, odometer: Int, paymentSource: WalletType) -> Unit
) {
    val availableFuels = remember(propulsionType) {
        when (propulsionType) {
            PropulsionType.ELECTRIC_100 -> listOf(FuelType.ELECTRICITY)
            PropulsionType.DIESEL -> listOf(FuelType.DIESEL)
            PropulsionType.GASOLINE_ONLY -> listOf(FuelType.GASOLINE)
            PropulsionType.GAS_ONLY -> listOf(FuelType.GAS)
            PropulsionType.BI_FUEL -> listOf(FuelType.GAS, FuelType.GASOLINE)
        }
    }

    var selectedFuelType by remember(propulsionType) { mutableStateOf(availableFuels.first()) }
    var paymentSource by remember { mutableStateOf(WalletType.CASH_PHYSICAL) }
    var priceUnitText by remember(selectedFuelType) { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var volumeText by remember { mutableStateOf("") }
    var odometerText by remember(currentOdometer) {
        mutableStateOf(if (currentOdometer > 0) currentOdometer.toString() else "")
    }
    var isFullTank by remember { mutableStateOf(true) }
    var gasStationText by remember { mutableStateOf("") }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasDirtyData = amountText.isNotBlank() || volumeText.isNotBlank() || gasStationText.isNotBlank()
    val finalAmount = amountText.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
    val finalVolume = volumeText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val finalOdometer = odometerText.filter { it.isDigit() }.toIntOrNull() ?: currentOdometer
    val isFormValid = finalAmount > 0.0 && finalVolume > 0.0 && finalOdometer > 0

    ModalBottomSheet(
        onDismissRequest = { if (hasDirtyData) showDiscardDialog = true else onDismiss() },
        sheetState = sheetState,
        containerColor = DeepObsidianSurface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera Oficial Drivvo Naranja con Flecha y "Combustible"
            Surface(modifier = Modifier.fillMaxWidth(), color = FuelOrangeHeader, shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (hasDirtyData) showDiscardDialog = true else onDismiss() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    Text(text = "Combustible", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White, modifier = Modifier.padding(start = 4.dp))
                }
            }

            // Selector de combustible si es bi-combustible
            if (availableFuels.size > 1) {
                Text(text = "Tipo de Combustible", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    availableFuels.forEach { fuelType ->
                        FilterChip(
                            selected = selectedFuelType == fuelType,
                            onClick = { selectedFuelType = fuelType },
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

            // Tríada Matemática Reactiva: Precio Unitario y Costo Total
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = priceUnitText,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }
                        priceUnitText = clean
                        val pr = clean.toDoubleOrNull() ?: 0.0
                        if (pr > 0.0 && finalAmount > 0.0) {
                            volumeText = String.format(Locale.US, "%.2f", finalAmount / pr)
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
                        amountText = clean
                        val amt = clean.toDoubleOrNull() ?: 0.0
                        val pr = priceUnitText.toDoubleOrNull() ?: 0.0
                        if (pr > 0.0 && amt > 0.0) {
                            volumeText = String.format(Locale.US, "%.2f", amt / pr)
                        } else if (finalVolume > 0.0 && amt > 0.0) {
                            priceUnitText = String.format(Locale.US, "%.0f", amt / finalVolume)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Text("$", color = FuelOrangeHeader, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp)) },
                    label = { Text("Total Pagado ($)") },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true
                )
            }

            // Volumen Despachado y Odómetro
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = volumeText,
                    onValueChange = { input ->
                        val clean = input.replace(',', '.')
                        volumeText = clean
                        val vol = clean.toDoubleOrNull() ?: 0.0
                        val pr = priceUnitText.toDoubleOrNull() ?: 0.0
                        if (vol > 0.0 && pr > 0.0) {
                            amountText = String.format(Locale.US, "%.0f", vol * pr)
                        } else if (vol > 0.0 && finalAmount > 0.0) {
                            priceUnitText = String.format(Locale.US, "%.0f", finalAmount / vol)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text("Volumen (${selectedFuelType.unitLabel})") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = odometerText,
                    onValueChange = { odometerText = it.filter { ch -> ch.isDigit() } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Odómetro (km)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Switch ¿Está llenando el tanque? (Tanque Lleno)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DeepObsidianSurfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Tanque Lleno", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                        Text(text = if (isFullTank) "Tanque abastecido al 100%" else "Carga parcial de combustible", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(
                        checked = isFullTank,
                        onCheckedChange = { isFullTank = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FuelOrangeHeader)
                    )
                }
            }

            // Selector de Origen de Fondos
            Text(text = "Origen de Fondos", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = paymentSource == WalletType.CASH_PHYSICAL,
                    onClick = { paymentSource = WalletType.CASH_PHYSICAL },
                    label = { Text("💵 Efectivo Físico", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusGreen, selectedLabelColor = Color.Black)
                )
                FilterChip(
                    selected = paymentSource == WalletType.DIGITAL_WALLET,
                    onClick = { paymentSource = WalletType.DIGITAL_WALLET },
                    label = { Text("🟣 Billetera Digital", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF7B1FA2), selectedLabelColor = Color.White)
                )
            }

            // Estación de Servicio
            OutlinedTextField(
                value = gasStationText,
                onValueChange = { gasStationText = it },
                label = { Text("Estación de Servicio / Gasolinera (Opcional)") },
                placeholder = { Text("Ej: Texaco Calle 80, Terpel Autonorte") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Botón Único Principal GUARDAR
            Button(
                onClick = {
                    if (isFormValid) {
                        onConfirmFueling(selectedFuelType, finalAmount, finalVolume, finalOdometer, paymentSource)
                        onDismiss()
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FuelOrangeHeader, disabledContainerColor = FuelOrangeHeader.copy(alpha = 0.3f))
            ) {
                Text(
                    text = if (finalAmount > 0.0) "GUARDAR TANQUEO (- $ %,.0f)".format(finalAmount) else "GUARDAR TANQUEO",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            title = "¿Descartar Tanqueo?",
            message = "Tienes datos digitados sin guardar. ¿Deseas salir y descartar este tanqueo?",
            onConfirmDiscard = {
                showDiscardDialog = false
                onDismiss()
            },
            onDismissRequest = { showDiscardDialog = false }
        )
    }
}
