// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/SyncModels.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelos inmutables de Dominio para la sesión de usuario, telemetría de sincronización
// en la nube y progreso de recuperación de desastres (Disaster Recovery).
// Regla arquitectural: Cero dependencias del framework Android en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: SESIÓN DE USUARIO AUTENTICADO
// ------------------------------------------------------------------------------
/**
 * Modela la identidad del conductor autenticado en el subsistema de respaldo.
 *
 * @property uid Identificador único de usuario (Firebase UID).
 * @property email Correo electrónico asociado a la cuenta (opcional).
 * @property displayName Nombre legible del conductor para visualización en cabina.
 * @property isAnonymous Indica si la sesión opera con autenticación anónima temporal.
 */
data class UserSession(
    val uid: String,
    val email: String?,
    val displayName: String? = null,
    val isAnonymous: Boolean = false
)

// ------------------------------------------------------------------------------
// BLOQUE 2: ESTADO Y TELEMETRÍA DE SINCRONIZACIÓN EN LA NUBE
// ------------------------------------------------------------------------------
/**
 * Estados del motor reactivo de sincronización Cloud Sync Engine.
 */
enum class SyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    OFFLINE,
    ERROR
}

/**
 * Estado reactivo e inmutable de la sincronización de datos con Firestore.
 *
 * @property state Fase actual del ciclo de vida de sincronización.
 * @property lastSyncTimestamp Marca de tiempo Unix en milisegundos del último sync exitoso.
 * @property pendingMutationsCount Cantidad de entidades locales pendientes de subida.
 * @property errorMessage Mensaje de diagnóstico descriptivo en caso de anomalía.
 */
data class SyncStatus(
    val state: SyncState,
    val lastSyncTimestamp: Long? = null,
    val pendingMutationsCount: Int = 0,
    val errorMessage: String? = null
)

// ------------------------------------------------------------------------------
// BLOQUE 3: PROGRESO DE RESTAURACIÓN ANTE DESASTRE (DISASTER RECOVERY)
// ------------------------------------------------------------------------------
/**
 * Jerarquía sellada inmutable para el seguimiento paso a paso de la hidratación de datos.
 */
sealed interface RestoreProgress {

    /**
     * Estado en reposo, previo al inicio del proceso de hidratación remota.
     */
    data object Idle : RestoreProgress

    /**
     * Fase de descarga e inserción activa en la base de datos Room.
     *
     * @property currentEntity Entidad contable en procesamiento ("Shifts", "Trips", etc.).
     * @property restoredCount Número de registros restaurados hasta el momento.
     * @property percentage Porcentaje de avance normalizado (0.0f a 1.0f).
     */
    data class Running(
        val currentEntity: String,
        val restoredCount: Int,
        val percentage: Float
    ) : RestoreProgress

    /**
     * Proceso completado exitosamente con balance de entidades reconstruidas.
     *
     * @property totalShifts Total de jornadas de trabajo recuperadas.
     * @property totalTrips Total de servicios/carreras restablecidos.
     * @property totalFuelings Total de abastecimientos bi-combustible reincorporados.
     * @property totalExpenses Total de gastos incidentales en ruta recuperados.
     */
    data class Completed(
        val totalShifts: Int,
        val totalTrips: Int,
        val totalFuelings: Int,
        val totalExpenses: Int
    ) : RestoreProgress

    /**
     * Fallo en la recuperación de datos desde el backend en la nube.
     *
     * @property reason Motivo técnico o de red que impidió la finalización.
     */
    data class Failed(
        val reason: String
    ) : RestoreProgress
}
