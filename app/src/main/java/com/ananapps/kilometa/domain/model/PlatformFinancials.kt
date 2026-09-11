// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/PlatformFinancials.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelos de liquidación financiera para plataformas de transporte en Colombia.
// Modela el destino del dinero (Efectivo en mano vs Saldo en app) y el calendario de pagos.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: DESTINO DE FONDOS
// ------------------------------------------------------------------------------
enum class PaymentDestination(val displayName: String) {
    CASH_IN_HAND("Efectivo en Mano"),
    APP_RETAINED("Saldo en App")
}

// ------------------------------------------------------------------------------
// BLOQUE 2: REGLAS COMERCIALES Y CALENDARIO DE DISPERSIÓN POR PLATAFORMA
// ------------------------------------------------------------------------------
data class PlatformRule(
    val platformType: PlatformType,
    val referenceCommissionPct: Double,
    val disbursementDay: String,
    val defaultDestination: PaymentDestination
) {
    val defaultCommissionPct: Double get() = referenceCommissionPct
}

object PlatformConfig {
    val rules: Map<PlatformType, PlatformRule> = mapOf(
        PlatformType.UBER to PlatformRule(PlatformType.UBER, 28.0, "Lunes", PaymentDestination.APP_RETAINED),
        PlatformType.DIDI to PlatformRule(PlatformType.DIDI, 21.5, "Martes", PaymentDestination.APP_RETAINED),
        PlatformType.INDRIVE to PlatformRule(PlatformType.INDRIVE, 10.0, "Miércoles", PaymentDestination.CASH_IN_HAND),
        PlatformType.PRIVATE to PlatformRule(PlatformType.PRIVATE, 0.0, "Inmediato", PaymentDestination.CASH_IN_HAND)
    )

    fun getRule(type: PlatformType): PlatformRule = rules[type] ?: PlatformRule(
        type, 0.0, "Inmediato", PaymentDestination.CASH_IN_HAND
    )
}

// ------------------------------------------------------------------------------
// BLOQUE 3: RESUMEN DE LIQUIDACIÓN DE DOBLE BOLSILLO
// ------------------------------------------------------------------------------
data class CashSettlementSummary(
    val cashInHandGross: Double = 0.0,
    val digitalBalanceGross: Double = 0.0,
    val totalNet: Double = 0.0,
    val cashInHandAvailable: Double = 0.0, // Efectivo disponible tras combustible y gastos
    val digitalPendingDisbursement: Double = 0.0, // Saldo neto en apps por cobrar
    val cashCommissionsOwed: Double = 0.0, // Comisiones que se adeudan a la app por viajes en efectivo
    val netPlatformBalance: Double = 0.0 // Cruce neto: digitalPendingDisbursement - cashCommissionsOwed
)
