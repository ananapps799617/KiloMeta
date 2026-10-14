package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.Wallet
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de Dominio para la persistencia y gestión soberana de bolsillos y billeteras (Orden #054).
 */
interface WalletRepository {

    /**
     * Emite reactivamente la lista de bolsillos activos para selección operativa.
     */
    fun observeActiveWallets(): Flow<List<Wallet>>

    /**
     * Emite reactivamente todos los bolsillos del sistema e individuales.
     */
    fun observeAllWallets(): Flow<List<Wallet>>

    /**
     * Obtiene un bolsillo por su identificador único.
     */
    suspend fun getWalletById(id: String): Wallet?

    /**
     * Guarda o actualiza un bolsillo en la base de datos.
     */
    suspend fun saveWallet(wallet: Wallet): Result<Unit>

    /**
     * Conmuta el estado activo de un bolsillo aplicando la defensa en profundidad (MND-04).
     */
    suspend fun toggleWalletStatus(id: String, isActive: Boolean): Result<Unit>

    /**
     * Incrementa o decrementa el saldo de un bolsillo en tiempo real.
     */
    suspend fun updateWalletBalance(id: String, delta: Double): Result<Unit>

    /**
     * Elimina un bolsillo personalizado sin historial ni saldo.
     */
    suspend fun deleteCustomWallet(id: String): Result<Unit>
}
