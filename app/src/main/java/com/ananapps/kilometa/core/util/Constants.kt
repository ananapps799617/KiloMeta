// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/core/util/Constants.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Constantes maestras inmutables de la aplicación KiloMeta.
// Define umbrales de negocio, ergonomía táctil en cabina y nombres del sistema local.
// ==============================================================================

package com.ananapps.kilometa.core.util

// ------------------------------------------------------------------------------
// BLOQUE 1: CONSTANTES DE NEGOCIO Y CONFIGURACIÓN PREDETERMINADA
// ------------------------------------------------------------------------------
object Constants {

    /**
     * Nombre de la base de datos local SQLite/Room.
     */
    const val DATABASE_NAME: String = "kilometa_database.db"

    /**
     * Cuota semanal estimada por defecto en COP (Pesos Colombianos) para conductores ($500.000 COP).
     */
    const val DEFAULT_WEEKLY_QUOTA_COP: Long = 500_000L

    /**
     * Meta de cuota semanal en tipo Double.
     */
    const val DEFAULT_WEEKLY_QUOTA_DOUBLE: Double = 500_000.0

    /**
     * Días laborales estándar por semana para el cálculo diario prorrateado.
     */
    const val WORKING_DAYS_PER_WEEK: Int = 6

    /**
     * Umbral porcentual de transición al estado Ámbar (>= 85% de la cuota diaria).
     */
    const val AMBER_THRESHOLD_PERCENTAGE: Double = 0.85

    // --------------------------------------------------------------------------
    // BLOQUE 2: ERGONOMÍA DE CABINA Y ACCESIBILIDAD
    // --------------------------------------------------------------------------
    /**
     * Altura mínima para botones de acción rápida en cabina (Touch Target Ergonómico >= 56dp).
     */
    const val MIN_TOUCH_TARGET_DP: Int = 56

    /**
     * Altura de la tarjeta principal del Semáforo en cabina para legibilidad a 60cm.
     */
    const val SEMAPHORE_CARD_MIN_HEIGHT_DP: Int = 190

    // --------------------------------------------------------------------------
    // BLOQUE 3: ETIQUETAS DE TRAZABILIDAD
    // --------------------------------------------------------------------------
    const val APP_TAG: String = "KiloMetaApp"
}
