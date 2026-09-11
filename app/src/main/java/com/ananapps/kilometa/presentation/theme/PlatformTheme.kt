// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/theme/PlatformTheme.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Fuente Única de la Verdad (SSOT) para los tokens de identidad visual de las
// plataformas de transporte (Uber, DiDi, inDrive, Particular). Mantiene la pureza
// de la capa Domain proveyendo estilos para la capa de Presentación.
// ==============================================================================

package com.ananapps.kilometa.presentation.theme

import androidx.compose.ui.graphics.Color
import com.ananapps.kilometa.domain.model.PlatformType

/**
 * Token inmutable que encapsula la especificación cromática y tipográfica
 * de cada plataforma para garantizar legibilidad diurna solar (WCAG AAA).
 */
data class PlatformVisualToken(
    val brandColor: Color,
    val backgroundColor: Color,
    val contentColor: Color,
    val borderColor: Color? = null,
    val monogram: String,
    val badgeLabel: String
)

/**
 * Propiedad de extensión que mapea la entidad de Dominio `PlatformType`
 * a su correspondiente token visual para la interfaz de cabina.
 */
val PlatformType.visualToken: PlatformVisualToken
    get() = when (this) {
        PlatformType.UBER -> PlatformVisualToken(
            brandColor = PlatformUber,
            backgroundColor = Color(0xFF1C1C1E),
            contentColor = Color.White,
            borderColor = Color(0xFF3A3A3C),
            monogram = "UB",
            badgeLabel = "Uber"
        )
        PlatformType.DIDI -> PlatformVisualToken(
            brandColor = PlatformDiDi,
            backgroundColor = PlatformDiDi,
            contentColor = Color.White,
            borderColor = null,
            monogram = "DD",
            badgeLabel = "DiDi"
        )
        PlatformType.INDRIVE -> PlatformVisualToken(
            brandColor = PlatformInDrive,
            backgroundColor = PlatformInDrive,
            contentColor = Color.Black,
            borderColor = null,
            monogram = "IN",
            badgeLabel = "inDrive"
        )
        PlatformType.PRIVATE -> PlatformVisualToken(
            brandColor = PlatformPrivate,
            backgroundColor = Color(0xFF7B1FA2),
            contentColor = Color.White,
            borderColor = null,
            monogram = "PR",
            badgeLabel = "Particular"
        )
    }
