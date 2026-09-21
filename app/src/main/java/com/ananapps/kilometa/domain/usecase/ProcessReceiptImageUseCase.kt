// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/ProcessReceiptImageUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso que orquesta el reconocimiento óptico de caracteres (OCR) local y offline
// con Google ML Kit Text Recognition para recibos de viajes de Uber y DiDi (Orden #044).
// Procesa imágenes en < 150 ms sin requerir conexión a internet ni costos de API.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import android.content.Context
import android.net.Uri
import com.ananapps.kilometa.domain.model.ScannedTripReceipt
import com.ananapps.kilometa.domain.parser.DidiReceiptParser
import com.ananapps.kilometa.domain.parser.UberReceiptParser
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

class ProcessReceiptImageUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend operator fun invoke(imageUri: Uri): Result<ScannedTripReceipt> = withContext(Dispatchers.Default) {
        try {
            val inputImage = withContext(Dispatchers.IO) {
                InputImage.fromFilePath(context, imageUri)
            }

            val rawText = recognizeText(inputImage)

            if (rawText.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("No se detectó texto en la imagen seleccionada"))
            }

            // Enrutador inteligente de plataformas
            val parsedReceipt = when {
                UberReceiptParser.canParse(rawText) -> UberReceiptParser.parse(rawText)
                DidiReceiptParser.canParse(rawText) -> DidiReceiptParser.parse(rawText)
                else -> {
                    // Intento de fallback cruzado
                    UberReceiptParser.parse(rawText) ?: DidiReceiptParser.parse(rawText)
                }
            }

            if (parsedReceipt != null) {
                Result.success(parsedReceipt)
            } else {
                Result.failure(IllegalArgumentException("No se reconoció un formato válido de Uber o DiDi en el pantallazo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun recognizeText(image: InputImage): String = suspendCancellableCoroutine { continuation ->
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (continuation.isActive) {
                    continuation.resume(visionText.text)
                }
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) {
                    continuation.resume("")
                }
            }
    }
}
