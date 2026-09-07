// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/KiloMetaApp.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Punto de entrada principal de la aplicación Android. Inicializa el árbol de
// dependencias mediante Dagger Hilt en el ciclo de vida global del proceso.
// ==============================================================================

package com.ananapps.kilometa

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: INICIALIZACIÓN DE INYECCIÓN DE DEPENDENCIAS (HILT) Y WORKMANAGER
// ------------------------------------------------------------------------------
/**
 * Clase Application raíz anotada con @HiltAndroidApp.
 * Implementa [Configuration.Provider] para delegar la creación de Workers a [HiltWorkerFactory],
 * permitiendo inyección de dependencias en tareas de segundo plano de WorkManager.
 */
@HiltAndroidApp
class KiloMetaApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    // --------------------------------------------------------------------------
    // BLOQUE 2: CICLO DE VIDA DE LA APLICACIÓN
    // --------------------------------------------------------------------------
    override fun onCreate() {
        super.onCreate()
        // Inicializaciones ligeras de arranque si fueran necesarias.
        // Se mantiene libre de operaciones bloqueantes para arranque instantáneo en cabina.
    }
}
