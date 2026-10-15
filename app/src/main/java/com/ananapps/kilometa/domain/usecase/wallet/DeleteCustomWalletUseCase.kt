package com.ananapps.kilometa.domain.usecase.wallet

import com.ananapps.kilometa.domain.repository.WalletRepository
import javax.inject.Inject
import kotlin.math.abs

/**
 * Caso de uso para eliminar de forma segura bolsillos personalizados creados por el usuario (Orden #056).
 * Aplica triple guardrail de saldo cero ($0 COP con tolerancia epsilon < 0.01) y protege bolsillos del sistema.
 */
class DeleteCustomWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = runCatching {
        require(id.isNotBlank()) { "El identificador de bolsillo no puede estar vacío." }
        if (id in listOf("cash_physical", "digital_wallet")) {
            throw SecurityException("Violación de integridad (MND-04): Los bolsillos de sistema son inmutables.")
        }
        val existing = walletRepository.getWalletById(id)
            ?: throw NoSuchElementException("No existe el bolsillo con ID '$id'")

        if (existing.isSystemProtected) {
            throw SecurityException("Violación de integridad: El bolsillo '${existing.name}' es protegido.")
        }

        // Mandato MND-DG-01: Tolerancia Epsilon Financiera en $ COP
        if (abs(existing.balance) >= 0.01) {
            throw IllegalStateException("El bolsillo '${existing.name}' contiene un saldo activo de $${existing.balance}. Transfiera primero los fondos.")
        }

        walletRepository.deleteCustomWallet(id).getOrThrow()
    }
}
