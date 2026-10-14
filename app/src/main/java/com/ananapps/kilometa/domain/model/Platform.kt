package com.ananapps.kilometa.domain.model

/**
 * Modelo de Dominio inmutable que representa una plataforma de transporte multi-país (Orden #054).
 * Desacopla la lógica de negocio de enumeraciones rígidas y habilita personalización total.
 */
data class Platform(
    val id: String,
    val name: String,
    val monogram: String,
    val brandColorHex: String,
    val defaultCommissionPct: Double,
    val disbursementDay: String,
    val isCustom: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
