// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/common/ShareIntentHandler.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Extractor seguro de Uri de imágenes compartidas mediante Intent.ACTION_SEND (Orden #044).
// Aísla la compatibilidad retroactiva entre Android 13+ (TIRAMISU) y versiones anteriores.
// ==============================================================================

package com.ananapps.kilometa.presentation.common

import android.content.Intent
import android.net.Uri
import android.os.Build

fun extractSharedImageUri(intent: Intent?): Uri? {
    if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }
    }
    return null
}
