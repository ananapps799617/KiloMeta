// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/places/components/LocationPickerMapDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo con mapa interactivo OpenStreetMap + Leaflet en WebView sandbox seguro.
// Permite ubicar con precisión satelital exacta la casa, bomba o taller del chofer
// con cero costos de API Keys y estricto respeto a la soberanía de datos.
// ==============================================================================

package com.ananapps.kilometa.presentation.places.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import java.util.Locale

class WebAppInterface(private val onMoved: (Double, Double) -> Unit) {
    @JavascriptInterface
    fun onLocationMoved(lat: Double, lng: Double) {
        onMoved(lat, lng)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LocationPickerMapDialog(
    initialLat: Double = 4.7110,
    initialLng: Double = -74.0721,
    onDismiss: () -> Unit,
    onLocationConfirmed: (lat: Double, lng: Double) -> Unit
) {
    var selectedLat by remember { mutableDoubleStateOf(initialLat) }
    var selectedLng by remember { mutableDoubleStateOf(initialLng) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(580.dp),
            shape = RoundedCornerShape(24.dp),
            color = DeepObsidianSurface,
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextSecondary
                        )
                    }
                }

                // Map Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.allowFileAccess = false
                                settings.allowContentAccess = false
                                webViewClient = WebViewClient()
                                addJavascriptInterface(
                                    WebAppInterface { lat, lng ->
                                        post {
                                            selectedLat = lat
                                            selectedLng = lng
                                        }
                                    },
                                    "AndroidBridge"
                                )

                                val html = """
                                    <!DOCTYPE html>
                                    <html>
                                    <head>
                                      <meta charset="utf-8" />
                                      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                                      <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                                      <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                                      <style>
                                        html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #0D1117; }
                                      </style>
                                    </head>
                                    <body>
                                      <div id="map"></div>
                                      <script>
                                        var map = L.map('map').setView([${initialLat}, ${initialLng}], 15);
                                        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                                          maxZoom: 19,
                                          attribution: '© OpenStreetMap'
                                        }).addTo(map);

                                        var marker = L.marker([${initialLat}, ${initialLng}], {draggable: true}).addTo(map);

                                        function updatePos(lat, lng) {
                                          if (window.AndroidBridge) {
                                            window.AndroidBridge.onLocationMoved(lat, lng);
                                          }
                                        }

                                        marker.on('dragend', function(e) {
                                          var pos = marker.getLatLng();
                                          updatePos(pos.lat, pos.lng);
                                        });

                                        map.on('click', function(e) {
                                          marker.setLatLng(e.latlng);
                                          updatePos(e.latlng.lat, e.latlng.lng);
                                        });
                                      </script>
                                    </body>
                                    </html>
                                """.trimIndent()

                                loadDataWithBaseURL("https://openstreetmap.org", html, "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Footer Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
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
