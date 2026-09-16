// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterFuelingDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante unificado para Registro y Edición simétrica de Tanqueos (Orden #041).
// Tríada matemática completa, selección de bomba y origen de fondos con CockpitFloatingScaffold.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.Fueling
import com.ananapps.kilometa.domain.model.Place
import com.ananapps.kilometa.domain.model.PlaceCategory
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.WalletType
import com.ananapps.kilometa.presentation.common.CockpitFloatingScaffold
import com.ananapps.kilometa.presentation.common.PlaceSelectorField
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

private val FuelOrangeHeader = Color(0xFFE65100)

@Composable
fun RegisterFuelingDialog(
    currentOdometer: Int,
    onDismiss: () -> Unit,
    propulsionType: PropulsionType = PropulsionType.BI_FUEL,
    savedPlaces: List<Place> = emptyList(),
    existingFueling: Fueling? = null,
    onUpdateStationPrice: ((placeId: String, fuelType: FuelType, newPrice: Double) -> Unit)? = null,
    onConfirmFueling: (fuelType: FuelType, amountPaid: Double, volume: Double, odometer: Int, paymentSource: WalletType, timestamp: Long) -> Unit = { _, _, _, _, _, _ -> },
    onConfirmUpdate: (Fueling) -> Unit = {},
    initialTimestamp: Long? = null,
    modifier: Modifier = Modifier
) {
    val isEditing = existingFueling != null
    var selectedTimestamp by remember(existingFueling, initialTimestamp) {
        mutableStateOf(existingFueling?.timestamp ?: initialTimestamp ?: System.currentTimeMillis())
    }
    val availableFuels = remember(propulsionType) {
        when (propulsionType) {
            PropulsionType.ELECTRIC_100 -> listOf(FuelType.ELECTRICITY)
            PropulsionType.DIESEL -> listOf(FuelType.DIESEL)
            PropulsionType.GASOLINE_ONLY -> listOf(FuelType.GASOLINE)
            PropulsionType.GAS_ONLY -> listOf(FuelType.GAS)
            PropulsionType.BI_FUEL -> listOf(FuelType.GAS, FuelType.GASOLINE)
        }
    }

    var selectedFuelType by remember(existingFueling, propulsionType) {
        mutableStateOf(existingFueling?.fuelType ?: availableFuels.first())
    }
    var paymentSource by remember(existingFueling) {
        mutableStateOf(existingFueling?.paymentSource ?: WalletType.CASH_PHYSICAL)
    }
    var priceUnitText by remember(existingFueling, selectedFuelType) {
        mutableStateOf(
            if (existingFueling != null && existingFueling.volume > 0) {
                (existingFueling.amountPaid / existingFueling.volume).toInt().toString()
            } else ""
        )
    }
    var amountText by remember(existingFueling) {
        mutableStateOf(existingFueling?.amountPaid?.toLong()?.toString() ?: "")
    }
    var volumeText by remember(existingFueling) {
        mutableStateOf(existingFueling?.volume?.toString() ?: "")
    }
    var odometerText by remember(existingFueling, currentOdometer) {
        val odo = existingFueling?.odometer ?: currentOdometer
        mutableStateOf(if (odo > 0) odo.toString() else "")
    }
    var isFullTank by remember {
        mutableStateOf(true)
    }
    var gasStationText by remember { mutableStateOf("") }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var shouldUpdateStationPrice by remember { mutableStateOf(false) }

    val finalAmount = amountText.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
    val finalVolume = volumeText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val finalOdometer = odometerText.filter { it.isDigit() }.toIntOrNull() ?: currentOdometer
    val currentUnitPrice = priceUnitText.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
    val knownPrice = if (selectedFuelType == FuelType.GAS) selectedPlace?.gnvPricePerM3 else selectedPlace?.gasolinePricePerGal
    val hasPriceVariance = selectedPlace != null && knownPrice != null && currentUnitPrice > 0.0 && kotlin.math.abs(currentUnitPrice - knownPrice) >= 1.0
    val isFormValid = finalAmount > 0.0 && finalVolume > 0.0 && finalOdometer > 0

    val hasDirtyData = if (isEditing) {
        finalAmount != existingFueling.amountPaid ||
                finalVolume != existingFueling.volume ||
                finalOdometer != existingFueling.odometer ||
                selectedFuelType != existingFueling.fuelType ||
                paymentSource != existingFueling.paymentSource
    } else {
        amountText.isNotBlank() || volumeText.isNotBlank() || gasStationText.isNotBlank()
    }

    val dialogTitle = if (isEditing) "⛽ Editar Tanqueo" else "⛽ Registrar Combustible"
    val buttonText = if (isEditing) {
        "GUARDAR CAMBIOS"
    } else if (finalAmount > 0.0) {
        "GUARDAR TANQUEO (- $ %,.0f)".format(Locale.US, finalAmount)
    } else {
        "GUARDAR TANQUEO"
    }

    CockpitFloatingScaffold(
        title = dialogTitle,
        headerColor = FuelOrangeHeader,
        onDismiss = onDismiss,
        confirmButtonText = buttonText,
        confirmButtonColor = FuelOrangeHeader,
        isConfirmEnabled = isFormValid,
        onConfirm = {
            if (isFormValid) {
                if (shouldUpdateStationPrice && selectedPlace != null && currentUnitPrice > 0.0) {
                    onUpdateStationPrice?.invoke(selectedPlace!!.id, selectedFuelType, currentUnitPrice)
                }
                if (isEditing) {
                    onConfirmUpdate(
                        existingFueling.copy(
                            fuelType = selectedFuelType,
                            amountPaid = finalAmount,
                            volume = finalVolume,
                            odometer = finalOdometer,
                            paymentSource = paymentSource,
                            timestamp = selectedTimestamp
                        )
                    )
                } else {
                    onConfirmFueling(selectedFuelType, finalAmount, finalVolume, finalOdometer, paymentSource, selectedTimestamp)
                }
                onDismiss()
            }
        },
        hasDirtyData = hasDirtyData,
        modifier = modifier
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

        FuelingFullTankSwitch(
            isFullTank = isFullTank,
            onFullTankChange = { isFullTank = it },
            accentColor = FuelOrangeHeader
        )

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

        PlaceSelectorField(
            label = "Estación de Servicio / Gasolinera (Opcional)",
            currentValue = gasStationText,
            places = savedPlaces,
            categoryFilter = listOf(PlaceCategory.GAS_STATION_GNV, PlaceCategory.GAS_STATION_GASOLINE),
            onPlaceSelected = { place ->
                selectedPlace = place
                gasStationText = place.name
                val tariff = if (selectedFuelType == FuelType.GAS) place.gnvPricePerM3 else place.gasolinePricePerGal
                if (tariff != null && tariff > 0) {
                    priceUnitText = tariff.toInt().toString()
                }
            },
            onValueChanged = {
                gasStationText = it
                if (selectedPlace != null && it != selectedPlace?.name) {
                    selectedPlace = null
                }
            },
            placeholder = "Ej: Texaco Calle 80, Terpel Autonorte",
            fuelTypeForTariff = selectedFuelType
        )

        if (hasPriceVariance && selectedPlace != null) {
            FuelingStationPriceVarianceCard(
                station = selectedPlace!!,
                fuelType = selectedFuelType,
                knownPrice = knownPrice,
                currentPrice = currentUnitPrice,
                isChecked = shouldUpdateStationPrice,
                onCheckedChange = { shouldUpdateStationPrice = it },
                accentColor = FuelOrangeHeader
            )
        }
    }
}
