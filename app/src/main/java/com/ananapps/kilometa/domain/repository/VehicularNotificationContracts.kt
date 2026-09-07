// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/VehicularNotificationContracts.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contratos de Dominio para la emisión y programación de notificaciones vehiculares
// sobre metas de cuota y cierre de ciclo semanal.
// Regla de Oro: Cero dependencias de Android framework (Context, NotificationManager, etc.).
// ==============================================================================

package com.ananapps.kilometa.domain.repository

// ------------------------------------------------------------------------------
// BLOQUE 1: CONTRATO DEL EMISOR DE NOTIFICACIONES VEHICULARES
// ------------------------------------------------------------------------------
/**
 * Define las operaciones para presentar alertas vehiculares de cuota y celebraciones
 * de hitos alcanzados, protegiendo la confidencialidad mediante DataGuard.
 */
interface VehicularNotificationNotifier {

    /**
     * Muestra aviso preventivo de cierre semanal (ej. Martes a las 18:00 hrs).
     *
     * @param percentage Porcentaje de avance de la meta semanal cumplido (0 a 100%).
     * @param remainingAmount Monto pendiente en COP para completar la meta.
     * @param deadlineHour Hora límite de cierre (ej. "23:59").
     */
    fun showWeeklyCutoffWarning(
        percentage: Int,
        remainingAmount: Double,
        deadlineHour: String
    )

    /**
     * Muestra el balance final del ciclo semanal alcanzado al corte del martes a las 23:59:59.
     *
     * @param achieved Indica si se cubrió el 100% o más de la meta semanal.
     * @param achievedAmount Monto total acumulado en el ciclo en COP.
     * @param targetAmount Meta fijada para el ciclo en COP.
     */
    fun showWeeklyCutoffFinal(
        achieved: Boolean,
        achievedAmount: Double,
        targetAmount: Double
    )

    /**
     * Notificación de celebración al superar el 100% de la cuota en turno o ciclo.
     *
     * @param title Título motivacional de la notificación.
     * @param message Mensaje pedagógico sin exhibir cifras líquidas exactas en pantalla de bloqueo.
     */
    fun showMilestoneCelebration(
        title: String,
        message: String
    )
}

// ------------------------------------------------------------------------------
// BLOQUE 2: CONTRATO DEL PROGRAMADOR DE TAREAS TEMPORIZADAS DE CUOTA
// ------------------------------------------------------------------------------
/**
 * Define la programación de alarmas y workers en segundo plano para el ciclo semanal.
 */
interface VehicularNotificationScheduler {

    /**
     * Programa las alertas de corte de cuota para el próximo Martes a las 18:00 y 23:59:59.
     */
    fun scheduleWeeklyCutoffAlarms()

    /**
     * Cancela las tareas programadas de alerta de cierre de cuota.
     */
    fun cancelScheduledAlarms()
}
