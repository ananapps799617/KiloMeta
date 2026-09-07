// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/MainActivity.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Actividad principal única (Single Activity Architecture) de KiloMeta.
// Anotada con @AndroidEntryPoint para recibir inyección de dependencias Hilt.
// Inicializa la interfaz Jetpack Compose bajo el tema Obsidian Cockpit.
// ==============================================================================

package com.ananapps.kilometa.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ananapps.kilometa.presentation.home.HomeScreen
import com.ananapps.kilometa.presentation.theme.KiloMetaTheme
import dagger.hilt.android.AndroidEntryPoint

// ------------------------------------------------------------------------------
// BLOQUE 1: ACTIVIDAD PRINCIPAL DE CABINA
// ------------------------------------------------------------------------------
/**
 * Punto de visualización único de la aplicación.
 * Conduce directamente al conductor al Cockpit principal sin pantallas de bloqueo.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // --------------------------------------------------------------------------
    // BLOQUE 2: CICLO DE VIDA Y RENDERIZADO COMPOSE
    // --------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KiloMetaTheme {
                HomeScreen()
            }
        }
    }
}
