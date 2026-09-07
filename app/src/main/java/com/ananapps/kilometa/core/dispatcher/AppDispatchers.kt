// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/core/dispatcher/AppDispatchers.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Anotaciones califiadoras (Qualifiers) para inyección de dependencias de Dagger Hilt.
// Permiten distinguir entre los distintos despachadores de corrutinas (IO, Default, Main).
// ==============================================================================

package com.ananapps.kilometa.core.dispatcher

import javax.inject.Qualifier

// ------------------------------------------------------------------------------
// BLOQUE 1: CALIFICADORES HILT PARA HILOS DE EJECUCIÓN
// ------------------------------------------------------------------------------

/**
 * Calificador para operaciones intensivas de Entrada/Salida (Base de datos Room, archivos locales).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Calificador para cálculos pesados en CPU (Cálculo del semáforo financiero, agregaciones).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/**
 * Calificador para actualizaciones en el hilo principal (UI Compose).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher
