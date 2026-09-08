// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/RentalAgreement.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo canónico de acuerdo de alquiler/arrendamiento de vehículo con corte y horario.
// Permite calcular con exactitud matemática el aporte diario exigido.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: MODELO DE ARRIENDO Y CUOTA
// ------------------------------------------------------------------------------
data class RentalAgreement(
    val quotaAmount: Double,
    val workDaysWeek: Int,
    val quotaCutoffDay: Int, // 1 = Lunes, 2 = Martes, etc.
    val quotaCutoffHour: Int
) {
    val dailyQuotaRequired: Double
        get() = if (workDaysWeek > 0) quotaAmount / workDaysWeek else 0.0
}
