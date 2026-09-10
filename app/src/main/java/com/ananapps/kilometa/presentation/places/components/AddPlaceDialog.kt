// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/places/components/AddPlaceDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal para registrar gasolineras, talleres o puntos clave del conductor.
// Incorpora botón de ubicación GPS One-Shot y selector interactivo con mapa Leaflet.
// ==============================================================================

package com.ananapps.kilometa.presentation.places.components

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ananapps.kilometa.domain.model.PlaceCategory
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import android.content.Context
import android.location.LocationManager
import android.os.Build
import java.util.Locale

@SuppressLint("MissingPermission")
@Composable
fun AddPlaceDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, category: PlaceCategory, address: String, notes: String?, lat: Double?, lng: Double?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PlaceCategory.GAS_STATION_GNV) }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var showMapPicker by remember { mutableStateOf(false) }

    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted && locationManager != null) {
            val isGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNet = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val provider = if (isGps) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
                locationManager.getCurrentLocation(provider, null, context.mainExecutor) { loc ->
                    if (loc != null) {
                        latitude = loc.latitude
                        longitude = loc.longitude
                        if (address.isBlank()) address = "GPS (${String.format(Locale.US, "%.4f, %.4f", loc.latitude, loc.longitude)})"
                    }
                }
            } else {
                val loc = if (isGps) locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                          else if (isNet) locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                          else null
                if (loc != null) {
                    latitude = loc.latitude
                    longitude = loc.longitude
                    if (address.isBlank()) address = "GPS (${String.format(Locale.US, "%.4f, %.4f", loc.latitude, loc.longitude)})"
                }
            }
        }
    }

    if (showMapPicker) {
        LocationPickerMapDialog(
            initialLat = latitude ?: 4.7110,
            initialLng = longitude ?: -74.0721,
            onDismiss = { showMapPicker = false },
            onLocationConfirmed = { lat, lng ->
                latitude = lat
                longitude = lng
                showMapPicker = false
                if (address.isBlank()) address = "Mapa (${String.format(Locale.US, "%.4f, %.4f", lat, lng)})"
            }
        )
    }

    val isFormValid = name.isNotBlank() && address.isNotBlank()
    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
        focusedBorderColor = ElectricBluePrimary, unfocusedBorderColor = DeepObsidianSurfaceVariant
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DeepObsidianSurface,
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(ElectricBluePrimary.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = ElectricBluePrimary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Nuevo Lugar", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    IconButton(onClick = onDismiss) {
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

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "GEOLOCALIZACIÓN (WAZE / MAPS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("📍 Mi Ubicación", fontSize = 12.sp, color = ElectricBluePrimary)
                    }
                    Button(
                        onClick = { showMapPicker = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepObsidianSurfaceVariant)
                    ) {
                        Text("🗺️ En el Mapa", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                if (latitude != null && longitude != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "🛰️ GPS: ${String.format(Locale.US, "%.5f, %.5f", latitude, longitude)}", color = StatusGreen, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }

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
                                onSave(name.trim(), selectedCategory, address.trim(), notes.trim().ifEmpty { null }, latitude, longitude)
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
