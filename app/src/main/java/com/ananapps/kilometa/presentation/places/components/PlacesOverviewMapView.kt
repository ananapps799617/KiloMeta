// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/places/components/PlacesOverviewMapView.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Visor general de mapa nativo OSMDroid con todos los puntos y bombas agregados.
// Muestra pines clasificados por categoría, tarjeta flotante de detalle y
// botones directos para despachar navegación satelital hacia Waze y Google Maps.
// ==============================================================================

package com.ananapps.kilometa.presentation.places.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ananapps.kilometa.domain.model.Place
import com.ananapps.kilometa.presentation.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun PlacesOverviewMapView(
    places: List<Place>,
    onNavigateWaze: (Place) -> Unit,
    onNavigateMaps: (Place) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    val geoPlaces = remember(places) {
        places.filter { it.latitude != null && it.longitude != null }
    }

    remember(context) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    DisposableEffect(Unit) {
        onDispose {
            mapViewInstance?.onDetach()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapViewInstance = this
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true
                    controller.setZoom(15.0)

                    if (geoPlaces.isNotEmpty()) {
                        val first = geoPlaces.first()
                        controller.setCenter(GeoPoint(first.latitude!!, first.longitude!!))
                    } else {
                        controller.setCenter(GeoPoint(4.7110, -74.0721))
                    }
                }
            },
            update = { mapView ->
                mapView.overlays.removeAll { it is Marker }
                geoPlaces.forEach { place ->
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(place.latitude!!, place.longitude!!)
                        title = "${place.category.icon} ${place.name}"
                        snippet = place.address
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            selectedPlace = place
                            true
                        }
                    }
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Botón para centrar en todos los puntos
        if (geoPlaces.isNotEmpty()) {
            SmallFloatingActionButton(
                onClick = {
                    mapViewInstance?.let { map ->
                        if (geoPlaces.size == 1) {
                            val p = geoPlaces.first()
                            map.controller.animateTo(GeoPoint(p.latitude!!, p.longitude!!))
                            map.controller.setZoom(16.5)
                        } else {
                            val lats = geoPlaces.map { it.latitude!! }
                            val lngs = geoPlaces.map { it.longitude!! }
                            val box = BoundingBox(lats.max(), lngs.max(), lats.min(), lngs.min())
                            map.zoomToBoundingBox(box, true, 100)
                        }
                    }
                },
                containerColor = DeepObsidianSurface,
                contentColor = ElectricBluePrimary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Encuadrar puntos")
            }
        }

        // Tarjeta flotante inferior del lugar seleccionado
        AnimatedVisibility(
            visible = selectedPlace != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            selectedPlace?.let { place ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = place.category.icon,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = place.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = place.category.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricBluePrimary
                                    )
                                }
                            }
                            IconButton(onClick = { selectedPlace = null }, modifier = Modifier.size(32.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📍 ${place.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onNavigateWaze(place) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33CCFF), contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Text("🚙 Waze", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Button(
                                onClick = { onNavigateMaps(place) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853), contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Text("🗺️ Maps", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
