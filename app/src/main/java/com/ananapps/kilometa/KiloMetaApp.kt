// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/KiloMetaApp.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Punto de entrada principal de la aplicación Android. Inicializa el árbol de
// dependencias mediante Dagger Hilt en el ciclo de vida global del proceso.
// ==============================================================================

package com.ananapps.kilometa

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// ------------------------------------------------------------------------------
// BLOQUE 1: INICIALIZACIÓN DE INYECCIÓN DE DEPENDENCIAS (HILT)
// ------------------------------------------------------------------------------
/**
 * Clase Application raíz anotada con @HiltAndroidApp.
 * Dispara la generación de código de Hilt que actúa como contenedor de
 * dependencias a nivel de aplicación (SingletonComponent).
 */
@HiltAndroidApp
class KiloMetaApp : Application() {

    // --------------------------------------------------------------------------
    // BLOQUE 2: CICLO DE VIDA DE LA APLICACIÓN
    // --------------------------------------------------------------------------
    override fun onCreate() {
        super.onCreate()
        // Inicializaciones ligeras de arranque si fueran necesarias.
        // Se mantiene libre de operaciones bloqueantes para arranque instantáneo en cabina.
    }
}
