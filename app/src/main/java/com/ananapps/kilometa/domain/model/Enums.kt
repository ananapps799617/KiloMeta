// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/Enums.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Enumeraciones canónicas de la capa de Dominio para el modelo operativo y financiero.
// Representan tipos estrictos e inmutables para plataformas, combustibles, pagos,
// conceptos de gastos y estados de turno de trabajo.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: PLATAFORMAS DE TRANSPORTE
// ------------------------------------------------------------------------------
/**
 * Plataformas de intermediación o modalidad de servicio atendidas por el conductor.
 */
enum class PlatformType(val displayName: String) {
    UBER("Uber"),
    DIDI("DiDi"),
    INDRIVE("InDrive"),
    PRIVATE("Particular")
}

// ------------------------------------------------------------------------------
// BLOQUE 2: TIPOS DE COMBUSTIBLE BI-MODAL
// ------------------------------------------------------------------------------
/**
 * Sistema bi-combustible del vehículo (GNV vs Gasolina Corriente).
 */
enum class FuelType(val displayName: String, val unitLabel: String) {
    GAS("Gas Natural Vehicular", "m³"),
    GASOLINE("Gasolina", "Gal")
}

// ------------------------------------------------------------------------------
// BLOQUE 3: MÉTODOS DE PAGO
// ------------------------------------------------------------------------------
/**
 * Modalidades de recaudo para liquidación de carreras en caja o billetera.
 */
enum class PaymentMethod(val displayName: String) {
    CASH("Efectivo"),
    NEQUI("Nequi"),
    APP_WALLET("Billetera App")
}

// ------------------------------------------------------------------------------
// BLOQUE 4: CONCEPTOS DE GASTOS EN RUTA
// ------------------------------------------------------------------------------
/**
 * Clasificación de egresos incidentales u operativos durante la jornada.
 */
enum class ExpenseConcept(val displayName: String) {
    WASH("Lavado / Aseo"),
    TOLL("Peajes"),
    FOOD("Alimentación"),
    OTHER("Otros imprevistos")
}

// ------------------------------------------------------------------------------
// BLOQUE 5: ESTADOS DE LA JORNADA DE TRABAJO
// ------------------------------------------------------------------------------
/**
 * Ciclo de vida del turno de trabajo del conductor.
 */
enum class ShiftStatus(val displayName: String) {
    ACTIVE("En Turno"),
    COMPLETED("Finalizado")
}
