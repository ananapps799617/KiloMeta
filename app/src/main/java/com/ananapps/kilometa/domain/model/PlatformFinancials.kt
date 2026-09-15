// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/PlatformFinancials.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelos de liquidación financiera para plataformas de transporte en Colombia.
// Modela el destino del dinero (Efectivo en mano vs Saldo en app) y el calendario de pagos.
// ==============================================================================

package com.ananapps.kilometa.domain.model

// ------------------------------------------------------------------------------
// BLOQUE 1: DESTINO DE FONDOS Y BOLSAS SOBERANAS (ORDEN #033)
// ------------------------------------------------------------------------------
enum class PaymentDestination(val displayName: String) {
    CASH_IN_HAND("Efectivo en Mano"),
    APP_RETAINED("Saldo en App")
}

enum class WalletType(val displayName: String) {
    CASH_PHYSICAL("Efectivo en Mano"),
    DIGITAL_WALLET("Nequi / Daviplata"),
    UBER("Billetera Uber"),
    DIDI("Billetera DiDi"),
    CAR_QUOTA("Cuota Carro")
}

enum class TransferType(val displayName: String) {
    PLATFORM_PAYOUT("Dispersión / Cobro de App"),
    CASH_DEPOSIT("Consignación Efectivo a Digital"),
    CASH_WITHDRAWAL("Retiro de Digital a Efectivo"),
    COMMISSION_PAYMENT("Pago de Comisión a App"),
    QUOTA_PROVISION("Abono Cuota Carro")
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
// BLOQUE 3: BALANCE DE BILLETERA INDIVIDUAL POR PLATAFORMA
// ------------------------------------------------------------------------------
data class PlatformWalletBalance(
    val platformType: PlatformType,
    val digitalGross: Double = 0.0,
    val digitalCommission: Double = 0.0,
    val cashCommissionOwed: Double = 0.0,
    val cashGross: Double = 0.0
) {
    val netDisbursementPending: Double get() = (digitalGross - digitalCommission).coerceAtLeast(0.0)
    // Cruce neto específico de la app: saldo a favor digital menos comisión adeudada por efectivo
    val netBalance: Double get() = (digitalGross - digitalCommission) - cashCommissionOwed
}

// ------------------------------------------------------------------------------
// BLOQUE 4: RESUMEN DE LIQUIDACIÓN DE 4 BOLSILLOS SOBERANOS (ORDEN #033)
// ------------------------------------------------------------------------------
data class CashSettlementSummary(
    val cashPhysicalBalance: Double = 0.0, // Bolsillo 1: Billetes físicos en mano
    val digitalWalletBalance: Double = 0.0, // Bolsillo 2: Nequi / Daviplata / Banco
    val uberWalletBalance: Double = 0.0,    // Bolsillo 3: Saldo neto Uber
    val didiWalletBalance: Double = 0.0,    // Bolsillo 4: Saldo neto DiDi
    val carQuotaWalletBalance: Double = 0.0, // Bolsillo 5: Cuota Carro acumulada
    val totalNetAssets: Double = 0.0,       // Patrimonio neto total líquido
    val cashInHandGross: Double = 0.0,
    val digitalBalanceGross: Double = 0.0,
    val totalNet: Double = 0.0,
    val cashInHandAvailable: Double = cashPhysicalBalance, // Compatibilidad retroactiva
    val digitalPendingDisbursement: Double = digitalWalletBalance,
    val cashCommissionsOwed: Double = 0.0,
    val netPlatformBalance: Double = 0.0,
    val uberWallet: PlatformWalletBalance = PlatformWalletBalance(PlatformType.UBER),
    val didiWallet: PlatformWalletBalance = PlatformWalletBalance(PlatformType.DIDI),
    val indriveWallet: PlatformWalletBalance = PlatformWalletBalance(PlatformType.INDRIVE),
    val platformWallets: Map<PlatformType, PlatformWalletBalance> = emptyMap()
)
