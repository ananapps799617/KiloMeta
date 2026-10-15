package com.ananapps.kilometa.data.mapper

import com.ananapps.kilometa.core.util.IconCatalog
import com.ananapps.kilometa.data.local.entity.WalletEntity
import com.ananapps.kilometa.domain.model.Wallet

/**
 * Mapeadores bidireccionales entre WalletEntity (Room) y Wallet (Dominio).
 * Garantiza inmutabilidad y tipado 100% estricto (Orden #055).
 */
fun WalletEntity.toDomain(): Wallet {
    return Wallet(
        id = this.id,
        name = this.name,
        category = this.category,
        iconName = this.iconName.ifBlank { IconCatalog.DEFAULT_WALLET_ICON },
        colorHex = this.colorHex,
        isSystemProtected = this.isSystemProtected,
        isActive = this.isActive,
        balance = this.balance
    )
}

fun Wallet.toEntity(): WalletEntity {
    return WalletEntity(
        id = this.id,
        name = this.name,
        category = this.category,
        iconName = this.iconName.ifBlank { IconCatalog.DEFAULT_WALLET_ICON },
        colorHex = this.colorHex,
        isSystemProtected = this.isSystemProtected,
        isActive = this.isActive,
        balance = this.balance
    )
}

fun List<WalletEntity>.toWalletDomain(): List<Wallet> = map { it.toDomain() }
