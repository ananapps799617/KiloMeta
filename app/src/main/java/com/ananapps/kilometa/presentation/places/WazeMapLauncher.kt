// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/places/WazeMapLauncher.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Despachador de Intents implícitos seguros hacia aplicaciones de navegación externa
// (Waze y Google Maps) sin requerir permisos de localización en segundo plano.
// ==============================================================================

package com.ananapps.kilometa.presentation.places

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object WazeMapLauncher {

    fun openWaze(context: Context, address: String, latitude: Double? = null, longitude: Double? = null) {
        val uri = if (latitude != null && longitude != null) {
            Uri.parse("waze://?ll=$latitude,$longitude&navigate=yes")
        } else {
            val encoded = URLEncoder.encode(address, "UTF-8")
            Uri.parse("waze://?q=$encoded&navigate=yes")
        }

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val webEncoded = URLEncoder.encode(address, "UTF-8")
            val fallbackUri = Uri.parse("https://waze.com/ul?q=$webEncoded")
            val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (err: Exception) {
                Toast.makeText(context, "No se pudo abrir Waze", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openGoogleMaps(context: Context, address: String, latitude: Double? = null, longitude: Double? = null) {
        val uri = if (latitude != null && longitude != null) {
            Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        } else {
            val encoded = URLEncoder.encode(address, "UTF-8")
            Uri.parse("geo:0,0?q=$encoded")
        }

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir Google Maps", Toast.LENGTH_SHORT).show()
        }
    }
}
