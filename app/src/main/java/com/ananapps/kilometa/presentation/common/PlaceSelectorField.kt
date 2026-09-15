// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/common/PlaceSelectorField.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Campo relacional inteligente con menú desplegable para vincular bombas y talleres
// guardados con soporte para texto libre personalizado y visualización de tarifas.
// ==============================================================================

package com.ananapps.kilometa.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.Place
import com.ananapps.kilometa.domain.model.PlaceCategory
import com.ananapps.kilometa.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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
    val filteredPlaces = remember(places, categoryFilter, currentValue) {
        val base = if (categoryFilter.isEmpty()) places else places.filter { it.category in categoryFilter }
        if (currentValue.isBlank()) {
            base
        } else {
            base.filter { it.name.contains(currentValue, ignoreCase = true) || it.address.contains(currentValue, ignoreCase = true) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = currentValue,
            onValueChange = {
                onValueChanged(it)
                expanded = true
            },
            label = { Text(label) },
            placeholder = { Text(placeholder, color = TextSecondary) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentValue.isNotBlank()) {
                        IconButton(onClick = { onValueChanged("") }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
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
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                .fillMaxWidth()
        )

        if (filteredPlaces.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(DeepObsidianSurface)
                    .widthIn(max = 340.dp)
            ) {
                filteredPlaces.forEach { place ->
                    DropdownMenuItem(
                        text = {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(place.category.icon, fontSize = 16.sp)
                                    Text(
                                        text = place.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
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
                                    place.gnvPricePerM3 != null ->
                                        "Tarifa GNV: $ %,.0f/m³".format(place.gnvPricePerM3)
                                    place.gasolinePricePerGal != null ->
                                        "Tarifa Gasolina: $ %,.0f/Gal".format(place.gasolinePricePerGal)
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
                        },
                        onClick = {
                            onPlaceSelected(place)
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
