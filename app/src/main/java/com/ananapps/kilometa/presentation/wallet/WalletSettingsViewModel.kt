package com.ananapps.kilometa.presentation.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.Wallet
import com.ananapps.kilometa.domain.usecase.wallet.GetAllWalletsUseCase
import com.ananapps.kilometa.domain.usecase.wallet.SaveWalletUseCase
import com.ananapps.kilometa.domain.usecase.wallet.ToggleWalletStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la gestión de bolsillos y billeteras soberanas (Orden #054).
 * Regla inquebrantable: Modularidad estricta < 280 líneas de código.
 */
@HiltViewModel
class WalletSettingsViewModel @Inject constructor(
    getAllWalletsUseCase: GetAllWalletsUseCase,
    private val saveWalletUseCase: SaveWalletUseCase,
    private val toggleWalletStatusUseCase: ToggleWalletStatusUseCase
) : ViewModel() {

    val wallets: StateFlow<List<Wallet>> = getAllWalletsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _editingWallet = MutableStateFlow<Wallet?>(null)
    val editingWallet: StateFlow<Wallet?> = _editingWallet.asStateFlow()

    private val _isSheetOpen = MutableStateFlow(false)
    val isSheetOpen: StateFlow<Boolean> = _isSheetOpen.asStateFlow()

    fun openCreateWallet() {
        _editingWallet.value = null
        _errorMessage.value = null
        _isSheetOpen.value = true
    }

    fun openEditWallet(wallet: Wallet) {
        _editingWallet.value = wallet
        _errorMessage.value = null
        _isSheetOpen.value = true
    }

    fun closeSheet() {
        _editingWallet.value = null
        _errorMessage.value = null
        _isSheetOpen.value = false
    }

    fun toggleWalletActive(id: String, isActive: Boolean) {
        viewModelScope.launch {
            toggleWalletStatusUseCase(id, isActive).onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }

    fun saveWallet(wallet: Wallet) {
        viewModelScope.launch {
            saveWalletUseCase(wallet).onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
