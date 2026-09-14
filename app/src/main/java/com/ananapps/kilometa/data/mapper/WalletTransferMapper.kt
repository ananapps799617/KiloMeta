// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/mapper/WalletTransferMapper.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Mapeador bidireccional entre la entidad Room WalletTransferEntity y el modelo de dominio WalletTransfer.
// Regla inquebrantable: Modularidad estricta < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.data.mapper

import com.ananapps.kilometa.data.local.entity.WalletTransferEntity
import com.ananapps.kilometa.domain.model.TransferType
import com.ananapps.kilometa.domain.model.WalletTransfer
import com.ananapps.kilometa.domain.model.WalletType

fun WalletTransferEntity.toDomain(): WalletTransfer {
    return WalletTransfer(
        id = id,
        timestamp = timestamp,
        sourceWallet = try {
            WalletType.valueOf(sourceWallet)
        } catch (e: Exception) {
            WalletType.CASH_PHYSICAL
        },
        destinationWallet = try {
            WalletType.valueOf(destinationWallet)
        } catch (e: Exception) {
            WalletType.DIGITAL_WALLET
        },
        amount = amount,
        transferType = try {
            TransferType.valueOf(transferType)
        } catch (e: Exception) {
            TransferType.CASH_DEPOSIT
        },
        notes = notes
    )
}

fun WalletTransfer.toEntity(): WalletTransferEntity {
    return WalletTransferEntity(
        id = id,
        timestamp = timestamp,
        sourceWallet = sourceWallet.name,
        destinationWallet = destinationWallet.name,
        amount = amount,
        transferType = transferType.name,
        notes = notes
    )
}
