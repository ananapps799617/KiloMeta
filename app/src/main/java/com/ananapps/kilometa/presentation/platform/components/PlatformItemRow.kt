package com.ananapps.kilometa.presentation.platform.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.core.util.ColorUtils
import com.ananapps.kilometa.domain.model.Platform
import com.ananapps.kilometa.presentation.theme.*

/**
 * Renglón desacoplado para representar una plataforma en el catálogo (Orden #054 / #055).
 * Touch target vehicular >= 48dp, iconografía y contraste certificado WCAG AAA/AA.
 */
@Composable
fun PlatformItemRow(
    platform: Platform,
    onToggleActive: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brandColor = ColorUtils.parseHexColor(platform.brandColorHex)
    val contentColor = ColorUtils.getContrastingContentColor(brandColor)
    val rowAlpha = if (platform.isActive) 1.0f else 0.5f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(rowAlpha)
            .clickable(onClick = onClick),
        color = DeepObsidianSurface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con Icono Vehicular y Monograma de 2 letras (44x44dp)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(brandColor, shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = platform.iconName.ifBlank { "🚘" },
                        fontSize = 14.sp
                    )
                    Text(
                        text = platform.monogram.take(2).uppercase(),
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Información comercial de la plataforma
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = platform.name,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (platform.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Personalizada",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Comisión: ${platform.defaultCommissionPct}% • Dispersión: ${platform.disbursementDay}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Switch vehicular de activación (target táctil de 48dp)
            Switch(
                checked = platform.isActive,
                onCheckedChange = onToggleActive,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ElectricBluePrimary,
                    uncheckedThumbColor = TextTertiary,
                    uncheckedTrackColor = DeepObsidianSurfaceVariant
                )
            )
        }
    }
}
