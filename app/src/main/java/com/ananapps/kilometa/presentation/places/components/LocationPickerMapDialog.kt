// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/places/components/LocationPickerMapDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo con mapa nativo OpenStreetMap (OSMDroid) en Jetpack Compose.
// Cero WebViews, cero pantallas negras, aceleración por hardware nativa en Canvas
// y sin requerir API Keys pagas ni cuentas de terceros.
// ==============================================================================

package com.ananapps.kilometa.presentation.places.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

@Composable
fun LocationPickerMapDialog(
    initialLat: Double = 4.7110,
    initialLng: Double = -74.0721,
    onDismiss: () -> Unit,
    onLocationConfirmed: (lat: Double, lng: Double) -> Unit
) {
    val context = LocalContext.current
    var selectedLat by remember { mutableDoubleStateOf(initialLat) }
    var selectedLng by remember { mutableDoubleStateOf(initialLng) }

    remember(context) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var mapViewInstance by remember { androidx.compose.runtime.mutableStateOf<MapView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mapViewInstance?.onDetach()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).height(600.dp),
            shape = RoundedCornerShape(24.dp),
            color = DeepObsidianSurface,
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = ElectricBluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "SELECCIONA EN EL MAPA",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                    }
                }

                // Native OSMDroid Map Container
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    AndroidView(
                        factory = { ctx ->
                            MapView(ctx).apply {
                                mapViewInstance = this
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                isTilesScaledToDpi = true
                                controller.setZoom(16.5)
                                val initialPoint = GeoPoint(initialLat, initialLng)
                                controller.setCenter(initialPoint)

                                val marker = Marker(this).apply {
                                    position = initialPoint
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    title = "Ubicación Seleccionada"
                                    isDraggable = true
                                    setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                                        override fun onMarkerDrag(m: Marker?) {}
                                        override fun onMarkerDragEnd(m: Marker?) {
                                            m?.position?.let {
                                                selectedLat = it.latitude
                                                selectedLng = it.longitude
                                            }
                                        }
                                        override fun onMarkerDragStart(m: Marker?) {}
                                    })
                                }
                                overlays.add(marker)

                                val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                        p?.let {
                                            marker.position = it
                                            selectedLat = it.latitude
                                            selectedLng = it.longitude
                                            invalidate()
                                        }
                                        return true
                                    }
                                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                                })
                                overlays.add(0, eventsOverlay)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Footer Bar
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📍 Coordenadas: ${String.format(Locale.US, "%.5f", selectedLat)}, ${String.format(Locale.US, "%.5f", selectedLng)}",
                        color = StatusGreen,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onLocationConfirmed(selectedLat, selectedLng) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBluePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("FIJAR ESTA UBICACIÓN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}


