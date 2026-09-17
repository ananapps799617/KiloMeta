// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/PocketLedgerViewModel.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ViewModel dedicado para el diálogo de Extracto Contable y Libro Mayor por Bolsillo (Orden #044).
// Orquesta el bolsillo seleccionado, el desplazamiento de semanas (weekOffset) y expone
// el resumen contable reactivo mediante GetPocketLedgerUseCase.
// ==============================================================================

package com.ananapps.kilometa.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ananapps.kilometa.domain.model.PocketLedgerSummary
import com.ananapps.kilometa.domain.model.PocketType
import com.ananapps.kilometa.domain.usecase.GetPocketLedgerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class PocketLedgerViewModel @Inject constructor(
    private val getPocketLedgerUseCase: GetPocketLedgerUseCase
) : ViewModel() {

    private val _selectedPocket = MutableStateFlow<PocketType?>(null)
    val selectedPocket: StateFlow<PocketType?> = _selectedPocket.asStateFlow()

    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val ledgerSummary: StateFlow<PocketLedgerSummary?> = combine(_selectedPocket, _weekOffset) { pocket, offset ->
        pocket to offset
    }.flatMapLatest { pair: Pair<PocketType?, Int> ->
        val pocket = pair.first
        val offset = pair.second
        if (pocket == null) flowOf<PocketLedgerSummary?>(null)
        else getPocketLedgerUseCase(pocket, offset)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    fun selectPocket(pocket: PocketType?) {
        _selectedPocket.value = pocket
        _weekOffset.value = 0
    }

    fun previousWeek() {
        _weekOffset.value += 1
    }

    fun nextWeek() {
        if (_weekOffset.value > 0) {
            _weekOffset.value -= 1
        }
    }
}
