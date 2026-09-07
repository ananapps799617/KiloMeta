// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/SyncScheduler.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contrato de Dominio para la programación de tareas de sincronización en segundo plano.
// Regla de Oro: Cero dependencias de Android framework en capa de dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.SyncPreferences

// ------------------------------------------------------------------------------
// BLOQUE 1: CONTRATO DEL PROGRAMADOR DE SINCRONIZACIÓN (SCHEDULER)
// ------------------------------------------------------------------------------
/**
 * Abstracción de dominio para orquestar la sincronización en segundo plano sin acoplarse
 * directamente al motor subyacente (WorkManager / JobScheduler).
 */
interface SyncScheduler {

    /**
     * Agenda o actualiza la ejecución periódica de sincronización respetando
     * las restricciones de red y batería configuradas por el conductor.
     *
     * @param preferences Preferencias que determinan intervalo y política de conectividad.
     */
    fun schedulePeriodicSync(preferences: SyncPreferences)

    /**
     * Solicita una ejecución inmediata de sincronización fuera de la cadencia periódica.
     */
    fun triggerImmediateSync()

    /**
     * Cancela cualquier tarea periódica de sincronización programada.
     */
    fun cancelPeriodicSync()
}
