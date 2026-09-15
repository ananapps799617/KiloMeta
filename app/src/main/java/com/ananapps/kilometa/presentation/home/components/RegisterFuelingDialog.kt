// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterFuelingDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante centrado de 3 capas para registro de combustible (Orden #037).
// Cabecera naranja fija con salida, cuerpo scrollable para la tríada de surtidor
// y pie fijo inamovible con botón GUARDAR TANQUEO (56dp).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.WalletType
import com.ananapps.kilometa.presentation.theme.*

private val FuelOrangeHeader = Color(0xFFE65100)

@Composable
fun RegisterFuelingDialog(
    currentOdometer: Int,
    onDismiss: () -> Unit,
    propulsionType: PropulsionType = PropulsionType.BI_FUEL,
    onConfirmFueling: (fuelType: FuelType, amountPaid: Double, volume: Double, odometer: Int, paymentSource: WalletType, timestamp: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
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

    Dialog(
        onDismissRequest = { if (hasDirtyData) showDiscardDialog = true else onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .border(1.dp, DeepObsidianBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = DeepObsidianSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // CAPA 1: CABECERA FIJA
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = FuelOrangeHeader,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⛽ Registrar Combustible",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        IconButton(onClick = { if (hasDirtyData) showDiscardDialog = true else onDismiss() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CAPA 2: CUERPO DESPLAZABLE INTERNO
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DateTimePickerRow(
                        selectedDateTimeMillis = selectedTimestamp,
                        onDateTimeSelected = { selectedTimestamp = it },
                        accentColor = FuelOrangeHeader
                    )

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

                    FuelingTriadFields(
                        selectedFuelType = selectedFuelType,
                        priceUnitText = priceUnitText,
                        onPriceUnitChange = { priceUnitText = it },
                        amountText = amountText,
                        onAmountChange = { amountText = it },
                        volumeText = volumeText,
                        onVolumeChange = { volumeText = it },
                        odometerText = odometerText,
                        onOdometerChange = { odometerText = it },
                        accentColor = FuelOrangeHeader
                    )

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

                    Text(text = "Origen de Fondos", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = paymentSource == WalletType.CASH_PHYSICAL,
                            onClick = { paymentSource = WalletType.CASH_PHYSICAL },
                            label = { Text("💵 Efectivo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusGreen, selectedLabelColor = Color.Black)
                        )
                        FilterChip(
                            selected = paymentSource == WalletType.DIGITAL_WALLET,
                            onClick = { paymentSource = WalletType.DIGITAL_WALLET },
                            label = { Text("🟣 Digital", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF7B1FA2), selectedLabelColor = Color.White)
                        )
                    }

                    OutlinedTextField(
                        value = gasStationText,
                        onValueChange = { gasStationText = it },
                        label = { Text("Estación de Servicio / Gasolinera (Opcional)") },
                        placeholder = { Text("Ej: Texaco Calle 80, Terpel Autonorte") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CAPA 3: BOTONERA DE ACCIÓN FIJA (56dp)
                Button(
                    onClick = {
                        if (isFormValid) {
                            onConfirmFueling(selectedFuelType, finalAmount, finalVolume, finalOdometer, paymentSource, selectedTimestamp)
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
