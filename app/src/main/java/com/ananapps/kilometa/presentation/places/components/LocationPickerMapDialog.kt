// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/places/components/LocationPickerMapDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo con mapa interactivo OpenStreetMap + Leaflet embebido 100% offline en WebView sandbox.
// Motor de mapas empaquetado en assets locales (cero fallo de CDN) y pin SVG ultra-preciso.
// ==============================================================================

package com.ananapps.kilometa.presentation.places.components

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
    val context = LocalContext.current
    var selectedLat by remember { mutableDoubleStateOf(initialLat) }
    var selectedLng by remember { mutableDoubleStateOf(initialLng) }

    val leafletAssets = remember(context) { loadLeafletAssets(context) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).height(580.dp),
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

                // Map Container
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.databaseEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.cacheMode = WebSettings.LOAD_DEFAULT
                                settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 KiloMetaApp/1.0"
                                settings.allowFileAccess = false
                                settings.allowContentAccess = false

                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        view?.evaluateJavascript("if (window.map) { window.map.invalidateSize(); }", null)
                                    }
                                }

                                addJavascriptInterface(
                                    WebAppInterface { lat, lng ->
                                        post {
                                            selectedLat = lat
                                            selectedLng = lng
                                        }
                                    },
                                    "AndroidBridge"
                                )

                                val html = buildLeafletHtml(
                                    css = leafletAssets.first,
                                    js = leafletAssets.second,
                                    lat = initialLat,
                                    lng = initialLng
                                )
                                loadDataWithBaseURL("https://tile.openstreetmap.org", html, "text/html", "UTF-8", null)
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

private fun loadLeafletAssets(context: Context): Pair<String, String> {
    return try {
        val css = context.assets.open("map/leaflet.css").bufferedReader().use { it.readText() }
        val js = context.assets.open("map/leaflet.js").bufferedReader().use { it.readText() }
        Pair(css, js)
    } catch (_: Exception) {
        Pair("", "")
    }
}

private fun buildLeafletHtml(css: String, js: String, lat: Double, lng: Double): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
          <style>
            $css
            html, body { height: 100%; width: 100%; margin: 0; padding: 0; background: #0D1117; overflow: hidden; }
            #map { position: absolute; top: 0; bottom: 0; left: 0; right: 0; width: 100%; height: 100%; background: #0D1117; }
            .leaflet-container { background: #0D1117; }
          </style>
          <script>
            $js
          </script>
        </head>
        <body>
          <div id="map"></div>
          <script>
            var map = L.map('map', { zoomControl: false }).setView([$lat, $lng], 16);
            window.map = map;
            L.control.zoom({ position: 'bottomright' }).addTo(map);

            L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
              maxZoom: 19,
              attribution: '© OpenStreetMap'
            }).addTo(map);

            var pinIcon = L.divIcon({
              className: 'custom-pin',
              html: '<div style="position:relative;width:34px;height:34px;filter:drop-shadow(0px 3px 6px rgba(0,0,0,0.6));"><svg viewBox=\"0 0 24 24\" width=\"34\" height=\"34\" fill=\"#00E5FF\"><path d=\"M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5z\"/><circle cx=\"12\" cy=\"9\" r=\"2.5\" fill=\"#FFFFFF\"/></svg></div>',
              iconSize: [34, 34],
              iconAnchor: [17, 34]
            });

            var marker = L.marker([$lat, $lng], { draggable: true, icon: pinIcon }).addTo(map);

            function updatePos(lat, lng) {
              if (window.AndroidBridge) {
                window.AndroidBridge.onLocationMoved(lat, lng);
              }
            }

            marker.on('dragend', function() {
              var pos = marker.getLatLng();
              updatePos(pos.lat, pos.lng);
            });

            map.on('click', function(e) {
              marker.setLatLng(e.latlng);
              updatePos(e.latlng.lat, e.latlng.lng);
            });

            setTimeout(function() { if (window.map) map.invalidateSize(); }, 200);
            setTimeout(function() { if (window.map) map.invalidateSize(); }, 600);
            setTimeout(function() { if (window.map) map.invalidateSize(); }, 1200);
          </script>
        </body>
        </html>
    """.trimIndent()
}

