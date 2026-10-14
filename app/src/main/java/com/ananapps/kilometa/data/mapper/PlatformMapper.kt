package com.ananapps.kilometa.data.mapper

import com.ananapps.kilometa.data.local.entity.PlatformEntity
import com.ananapps.kilometa.domain.model.Platform

/**
 * Mapeadores bidireccionales entre PlatformEntity (Room) y Platform (Dominio).
 * Garantiza inmutabilidad y tipado 100% estricto.
 */
fun PlatformEntity.toDomain(): Platform {
    return Platform(
        id = this.id,
        name = this.name,
        monogram = this.monogram,
        brandColorHex = this.brandColorHex,
        defaultCommissionPct = this.defaultCommissionPct,
        disbursementDay = this.disbursementDay,
        isCustom = this.isCustom,
        isActive = this.isActive,
        createdAt = this.createdAt
    )
}

fun Platform.toEntity(): PlatformEntity {
    return PlatformEntity(
        id = this.id,
        name = this.name,
        monogram = this.monogram,
        brandColorHex = this.brandColorHex,
        defaultCommissionPct = this.defaultCommissionPct,
        disbursementDay = this.disbursementDay,
        isCustom = this.isCustom,
        isActive = this.isActive,
        createdAt = this.createdAt
    )
}

fun List<PlatformEntity>.toPlatformDomain(): List<Platform> = map { it.toDomain() }
