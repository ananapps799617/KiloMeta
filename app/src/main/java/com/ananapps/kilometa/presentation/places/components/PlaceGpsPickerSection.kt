// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/places/components/PlaceGpsPickerSection.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Selector de geolocalización One-Shot por GPS o mapa interactivo para AddPlaceDialog.
// ==============================================================================

package com.ananapps.kilometa.presentation.places.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@SuppressLint("MissingPermission")
@Composable
fun PlaceGpsPickerSection(
    latitude: Double?,
    longitude: Double?,
    onLocationResolved: (lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
                    if (loc != null) onLocationResolved(loc.latitude, loc.longitude)
                }
            } else {
                val loc = if (isGps) locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                else if (isNet) locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                else null
                if (loc != null) onLocationResolved(loc.latitude, loc.longitude)
            }
        }
    }

    if (showMapPicker) {
        val initialCenter = if (latitude != null && longitude != null) Pair(latitude, longitude) else Pair(4.7110, -74.0721)
        LocationPickerMapDialog(
            initialLat = initialCenter.first,
            initialLng = initialCenter.second,
            onDismiss = { showMapPicker = false },
            onLocationConfirmed = { lat, lng ->
                onLocationResolved(lat, lng)
                showMapPicker = false
            }
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
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
    }
}
