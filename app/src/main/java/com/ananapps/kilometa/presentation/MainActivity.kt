// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/MainActivity.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Actividad principal única (Single Activity Architecture) de KiloMeta.
// Anotada con @AndroidEntryPoint para inyección de dependencias Dagger Hilt.
// Orquesta la navegación de cabina entre HomeScreen (Cockpit) y HistoryScreen
// mediante la KiloMetaBottomBar ergonómica persistente.
// ==============================================================================

package com.ananapps.kilometa.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ananapps.kilometa.presentation.history.HistoryScreen
import com.ananapps.kilometa.presentation.home.HomeScreen
import com.ananapps.kilometa.presentation.navigation.AppDestination
import com.ananapps.kilometa.presentation.navigation.KiloMetaBottomBar
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.KiloMetaTheme
import dagger.hilt.android.AndroidEntryPoint

// ------------------------------------------------------------------------------
// BLOQUE 1: ACTIVIDAD PRINCIPAL DE CABINA
// ------------------------------------------------------------------------------
/**
 * Punto de visualización único de la aplicación KiloMeta.
 * Aloja el Scaffold maestro con conmutación ergonómica entre Cabina en Vivo e Historial.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // --------------------------------------------------------------------------
    // BLOQUE 2: CICLO DE VIDA Y RENDERIZADO COMPOSE CON NAVEGACIÓN
    // --------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KiloMetaTheme {
                var currentDestination by rememberSaveable { mutableStateOf(AppDestination.COCKPIT) }

                Scaffold(
                    bottomBar = {
                        KiloMetaBottomBar(
                            currentDestination = currentDestination,
                            onNavigate = { currentDestination = it }
                        )
                    },
                    containerColor = DeepObsidianBackground
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentDestination) {
                            AppDestination.COCKPIT -> HomeScreen()
                            AppDestination.HISTORY -> HistoryScreen()
                        }
                    }
                }
            }
        }
    }
}
