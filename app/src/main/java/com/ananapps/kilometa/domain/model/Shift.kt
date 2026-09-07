// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/Shift.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo de Dominio que representa una Jornada o Turno de Trabajo del conductor.
// Es 100% inmutable, desacoplado de Room/Android y encapsula reglas de odometría.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO INMUTABLE DEL TURNO
// ------------------------------------------------------------------------------
/**
 * Representa un turno de conducción individual.
 *
 * @property id Identificador único UUID v4 del turno.
 * @property startTime Marca de tiempo Unix (milisegundos) de inicio de jornada.
 * @property endTime Marca de tiempo Unix de finalización (null si está activo).
 * @property startOdometer Kilometraje registrado en el odómetro al arrancar.
 * @property endOdometer Kilometraje registrado al cerrar turno (null si está activo).
 * @property dailyQuotaTarget Meta de cuota en COP establecida para este turno (ej. $83,333).
 * @property status Estado de la jornada (ACTIVE o COMPLETED).
 * @property notes Observaciones o notas adicionales del conductor.
 */
data class Shift(
    val id: String,
    val startTime: Long,
    val endTime: Long? = null,
    val startOdometer: Int,
    val endOdometer: Int? = null,
    val dailyQuotaTarget: Double,
    val status: ShiftStatus,
    val notes: String? = null
) {
    // --------------------------------------------------------------------------
    // BLOQUE 2: PROPIEDADES DE NEGOCIO CALCULADAS
    // --------------------------------------------------------------------------
    /**
     * Kilómetros totales recorridos durante el turno.
     * Retorna 0 si el odómetro final aún no se ha registrado o es inconsistente.
     */
    val kilometersDriven: Int
        get() = if (endOdometer != null && endOdometer >= startOdometer) {
            endOdometer - startOdometer
        } else {
            0
        }

    /**
     * Indica si el turno se encuentra en curso actualmente.
     */
    val isActive: Boolean
        get() = status == ShiftStatus.ACTIVE
}
