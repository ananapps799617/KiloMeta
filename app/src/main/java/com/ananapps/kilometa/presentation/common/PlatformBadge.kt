// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/common/PlatformBadge.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Componentes visuales reutilizables para plataformas de transporte:
// 1. PlatformAvatar: Nodo circular con monograma de alta glanceabilidad para feeds.
// 2. PlatformBadge: Cápsula estilizada con nombre corporativo para cabeceras y tarjetas.
// ==============================================================================

package com.ananapps.kilometa.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.presentation.theme.visualToken

/**
 * Avatar circular de plataforma para feeds y timelines de actividades en cabina.
 * Tamaño por defecto 36dp con monograma tipográfico de alto contraste (WCAG AAA).
 */
@Composable
fun PlatformAvatar(
    platform: PlatformType,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val token = platform.visualToken
    val borderModifier = if (token.borderColor != null) {
        Modifier.border(1.dp, token.borderColor, CircleShape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(token.backgroundColor)
            .then(borderModifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = token.monogram,
            color = token.contentColor,
            fontSize = (size.value * 0.38f).sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif
        )
    }
}

/**
 * Micro-badge o chip cápsula con el nombre de la plataforma y su color corporativo.
 */
@Composable
fun PlatformBadge(
    platform: PlatformType,
    modifier: Modifier = Modifier
) {
    val token = platform.visualToken
    val borderModifier = if (token.borderColor != null) {
        Modifier.border(1.dp, token.borderColor, RoundedCornerShape(6.dp))
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(token.backgroundColor)
            .then(borderModifier)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = token.badgeLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            ),
            color = token.contentColor
        )
    }
}
