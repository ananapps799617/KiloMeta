// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/SyncRepository.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contrato de Dominio para el motor de sincronización bidireccional y recuperación
// ante desastres (Disaster Recovery).
// Regla arquitectural: Cero dependencias del framework Android en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.ConnectionDiagnostic
import com.ananapps.kilometa.domain.model.RestoreProgress
import com.ananapps.kilometa.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------------
// BLOQUE 1: INTERFAZ DEL REPOSITORIO DE SINCRONIZACIÓN Y RESPALDO
// ------------------------------------------------------------------------------
/**
 * Define el contrato para el respaldo en la nube y restauración de base de datos.
 */
interface SyncRepository {

    /**
     * Emite el estado de sincronización en tiempo real con Firestore.
     */
    fun observeSyncStatus(): Flow<SyncStatus>

    /**
     * Envía las entidades modificadas o creadas localmente hacia la nube (Push incremental).
     */
    suspend fun pushPendingChanges(): Result<Unit>

    /**
     * Descarga cambios nuevos desde la nube e integra con la base de datos local (Pull).
     */
    suspend fun pullRemoteChanges(): Result<Unit>

    /**
     * Ejecuta una restauración total de emergencia descargando todas las entidades remotas
     * e insertándolas en la base de datos Room respetando la integridad referencial.
     */
    fun fullDisasterRecovery(): Flow<RestoreProgress>

    /**
     * Realiza una prueba activa de conectividad round-trip con Firestore y calcula la latencia.
     */
    suspend fun checkCloudConnection(): ConnectionDiagnostic
}
