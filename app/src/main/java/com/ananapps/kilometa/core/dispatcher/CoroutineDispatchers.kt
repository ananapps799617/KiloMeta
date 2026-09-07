// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/core/dispatcher/CoroutineDispatchers.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contenedor inmutable de despachadores de corrutinas para facilitar inyección
// y sustitución por TestDispatchers en pruebas unitarias deterministas.
// ==============================================================================

package com.ananapps.kilometa.core.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO INMUTABLE DE DISPATCHERS
// ------------------------------------------------------------------------------
/**
 * Agrupa los tres despachadores fundamentales de la aplicación.
 * Garantiza inmutabilidad y tipado estricto sin tipos genéricos o nulos.
 *
 * @property io Despachador para I/O (disco, SQLite).
 * @property default Despachador para operaciones CPU-bound (matemática del semáforo).
 * @property main Despachador para UI e interacciones con Jetpack Compose.
 */
data class CoroutineDispatchers(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
    val main: CoroutineDispatcher
)
