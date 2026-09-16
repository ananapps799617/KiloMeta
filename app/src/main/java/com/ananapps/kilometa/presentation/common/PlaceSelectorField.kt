// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/common/PlaceSelectorField.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Campo relacional inteligente para vincular bombas y talleres guardados.
// Diseñado para cabina: sugerencias en línea inmunes a colisiones con teclado,
// botón Done directo en teclado y acceso modal a catálogo completo «Mis Lugares».
// ==============================================================================

package com.ananapps.kilometa.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.Place
import com.ananapps.kilometa.domain.model.PlaceCategory
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun PlaceSelectorField(
    label: String,
    currentValue: String,
    places: List<Place>,
    categoryFilter: List<PlaceCategory>,
    onPlaceSelected: (Place) -> Unit,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Seleccionar o escribir...",
    fuelTypeForTariff: FuelType? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var showPickerDialog by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val filteredPlaces = remember(places, categoryFilter, currentValue) {
        val base = if (categoryFilter.isEmpty()) places else places.filter { it.category in categoryFilter }
        if (currentValue.isBlank()) {
            base
        } else {
            base.filter {
                it.name.contains(currentValue, ignoreCase = true) ||
                it.address.contains(currentValue, ignoreCase = true)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = currentValue,
            onValueChange = {
                onValueChanged(it)
                expanded = true
            },
            label = { Text(label) },
            placeholder = { Text(placeholder, color = TextSecondary) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentValue.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onValueChanged("")
                                expanded = false
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            showPickerDialog = true
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("📍", fontSize = 16.sp)
                    }
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Contraer" else "Desplegar",
                            tint = ElectricBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricBluePrimary,
                unfocusedBorderColor = DeepObsidianBorder,
                focusedLabelColor = ElectricBluePrimary,
                unfocusedLabelColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = DeepObsidianSurfaceVariant,
                unfocusedContainerColor = DeepObsidianSurfaceVariant
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // SUGERENCIAS EN LÍNEA: Inmunes al teclado, contenidas en el scroll de la cabina
        if (expanded && filteredPlaces.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DeepObsidianSurfaceVariant),
                border = BorderStroke(1.dp, ElectricBluePrimary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lugares guardados (${filteredPlaces.size}):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricBluePrimary
                        )
                        Text(
                            text = "Ocultar ✕",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = TextSecondary,
                            modifier = Modifier.clickable { expanded = false }
                        )
                    }

                    HorizontalDivider(color = DeepObsidianBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                    filteredPlaces.forEach { place ->
                        InlinePlaceSuggestionRow(
                            place = place,
                            fuelTypeForTariff = fuelTypeForTariff,
                            onSelect = {
                                onPlaceSelected(place)
                                expanded = false
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        )
                        HorizontalDivider(color = DeepObsidianBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }

    if (showPickerDialog) {
        PlacePickerDialog(
            places = places,
            categoryFilter = categoryFilter,
            onPlaceSelected = { place ->
                onPlaceSelected(place)
                expanded = false
            },
            onDismissRequest = { showPickerDialog = false },
            fuelTypeForTariff = fuelTypeForTariff
        )
    }
}

@Composable
private fun InlinePlaceSuggestionRow(
    place: Place,
    fuelTypeForTariff: FuelType?,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(place.category.icon, fontSize = 18.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (place.address.isNotBlank()) {
                Text(
                    text = "📍 ${place.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val tariffBadge = when {
                fuelTypeForTariff == FuelType.GAS && place.gnvPricePerM3 != null ->
                    "Tarifa GNV: $ %,.0f/m³".format(place.gnvPricePerM3)
                fuelTypeForTariff == FuelType.GASOLINE && place.gasolinePricePerGal != null ->
                    "Tarifa Gasolina: $ %,.0f/Gal".format(place.gasolinePricePerGal)
                place.gnvPricePerM3 != null && place.gasolinePricePerGal != null ->
                    "GNV: $ %,.0f | Gas: $ %,.0f".format(place.gnvPricePerM3, place.gasolinePricePerGal)
                place.gnvPricePerM3 != null -> "Tarifa GNV: $ %,.0f/m³".format(place.gnvPricePerM3)
                place.gasolinePricePerGal != null -> "Tarifa Gasolina: $ %,.0f/Gal".format(place.gasolinePricePerGal)
                else -> null
            }
            if (tariffBadge != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(FuelGas.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⛽ $tariffBadge",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = FuelGas
                    )
                }
            }
        }
    }
}
