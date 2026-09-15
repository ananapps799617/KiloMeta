// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/places/components/AddPlaceDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal para registrar gasolineras, talleres o puntos clave del conductor.
// Incorpora memoria de tarifas para GNV y Gasolina, selector GPS One-Shot y mapa.
// ==============================================================================

package com.ananapps.kilometa.presentation.places.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ananapps.kilometa.domain.model.PlaceCategory
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun AddPlaceDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: PlaceCategory,
        address: String,
        notes: String?,
        lat: Double?,
        lng: Double?,
        gnvPrice: Double?,
        gasPrice: Double?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PlaceCategory.GAS_STATION_GNV) }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var gnvPriceText by remember { mutableStateOf("") }
    var gasPriceText by remember { mutableStateOf("") }

    val isFormValid = name.isNotBlank() && address.isNotBlank()
    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
        focusedBorderColor = ElectricBluePrimary, unfocusedBorderColor = DeepObsidianSurfaceVariant
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DeepObsidianSurface,
            modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nuevo Lugar Frecuente",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "CATEGORÍA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlaceCategory.values().forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .background(color = if (isSelected) ElectricBluePrimary else DeepObsidianSurfaceVariant, shape = RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(text = "${cat.icon} ${cat.displayName}", fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.White else TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nombre del lugar") },
                    placeholder = { Text("Ej: Casa, Bomba Terpel Cll 80", color = TextSecondary) },
                    singleLine = true, colors = tfColors, modifier = Modifier.fillMaxWidth()
                )

                if (selectedCategory == PlaceCategory.GAS_STATION_GNV || selectedCategory == PlaceCategory.GAS_STATION_GASOLINE) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("TARIFAS DE SURTIDOR (OPCIONAL)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = gnvPriceText,
                            onValueChange = { gnvPriceText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("GNV ($/m³)") },
                            placeholder = { Text("Ej: 3899", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true, colors = tfColors, modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = gasPriceText,
                            onValueChange = { gasPriceText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Gasolina ($/Gal)") },
                            placeholder = { Text("Ej: 15600", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true, colors = tfColors, modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                PlaceGpsPickerSection(
                    latitude = latitude,
                    longitude = longitude,
                    onLocationResolved = { lat, lng ->
                        latitude = lat
                        longitude = lng
                        if (address.isBlank()) address = "GPS (%.4f, %.4f)".format(lat, lng)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = address, onValueChange = { address = it },
                    label = { Text("Dirección o Referencia") },
                    placeholder = { Text("Ej: Calle 6 # 22 - 40", color = TextSecondary) },
                    singleLine = true, colors = tfColors, modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notas adicionales (Opcional)") },
                    maxLines = 2, colors = tfColors, modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (isFormValid) {
                                val gnv = gnvPriceText.toDoubleOrNull()
                                val gas = gasPriceText.toDoubleOrNull()
                                onSave(name.trim(), selectedCategory, address.trim(), notes.trim().ifEmpty { null }, latitude, longitude, gnv, gas)
                            }
                        },
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary, contentColor = Color.White, disabledContainerColor = DeepObsidianSurfaceVariant, disabledContentColor = TextSecondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar Lugar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
