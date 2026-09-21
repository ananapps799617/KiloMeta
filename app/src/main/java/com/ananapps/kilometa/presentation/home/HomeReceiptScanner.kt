// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/HomeReceiptScanner.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Delegado de presentación encargado de gestionar el ciclo de escaneo OCR
// de pantallazos de Uber y DiDi (Orden #044). Mantiene HomeViewModel modular (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.home

import android.net.Uri
import com.ananapps.kilometa.domain.model.ScannedTripReceipt
import com.ananapps.kilometa.domain.usecase.ProcessReceiptImageUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeReceiptScanner @Inject constructor(
    private val processReceiptImageUseCase: ProcessReceiptImageUseCase
) {
    private val _scannedReceipt = MutableStateFlow<ScannedTripReceipt?>(null)
    val scannedReceipt: StateFlow<ScannedTripReceipt?> = _scannedReceipt.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError.asStateFlow()

    fun clearScannedReceipt() {
        _scannedReceipt.value = null
        _scanError.value = null
    }

    fun scanReceipt(
        scope: CoroutineScope,
        imageUri: Uri,
        onSuccess: (ScannedTripReceipt) -> Unit = {}
    ) = scope.launch {
        _isScanning.value = true
        _scanError.value = null
        val result = processReceiptImageUseCase(imageUri)
        result.onSuccess { receipt ->
            _scannedReceipt.value = receipt
            onSuccess(receipt)
        }.onFailure { error ->
            _scanError.value = error.localizedMessage ?: "No se pudo leer el recibo del viaje"
        }
        _isScanning.value = false
    }
}
