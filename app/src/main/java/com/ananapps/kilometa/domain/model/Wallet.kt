package com.ananapps.kilometa.domain.model

/**
 * Modelo de Dominio inmutable que representa un bolsillo o billetera soberana (Orden #054).
 * Aísla saldos y protege billeteras críticas del sistema (Efectivo y Digital).
 */
data class Wallet(
    val id: String,
    val name: String,
    val category: String,
    val colorHex: String,
    val isSystemProtected: Boolean = false,
    val isActive: Boolean = true,
    val balance: Double = 0.0
)
