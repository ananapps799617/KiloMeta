// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/PocketType.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo canónico de Dominio que define los 5 Bolsillos Soberanos del Conductor (Orden #044).
// Modela billeteras físicas, digitales, billeteras de plataforma y la cuota del vehículo.
// ==============================================================================

package com.ananapps.kilometa.domain.model

/**
 * Enumeración canónica de los 5 bolsillos soberanos de cabina.
 */
enum class PocketType(
    val displayName: String,
    val icon: String,
    val isPlatform: Boolean = false,
    val platformType: PlatformType? = null
) {
    CASH_PHYSICAL("Efectivo Físico", "💵"),
    DIGITAL_WALLET("Nequi / Davi", "🟣"),
    UBER("Billetera Uber", "🖤", isPlatform = true, platformType = PlatformType.UBER),
    DIDI("Billetera DiDi", "🧡", isPlatform = true, platformType = PlatformType.DIDI),
    CAR_QUOTA("Cuota del Carro", "🚗")
}
