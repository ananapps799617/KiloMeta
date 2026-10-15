package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.core.util.ColorUtils
import com.ananapps.kilometa.domain.model.Platform
import com.ananapps.kilometa.presentation.theme.*

/**
 * Selector vehicular dinámico multi-país para registro de carreras (Orden #054 / #055).
 * Touch target vehicular de 48dp, iconografía y adaptación ergonómica a luz solar.
 */
@Composable
fun DynamicTripPlatformSelector(
    platforms: List<Platform>,
    selectedPlatformId: String,
    onPlatformSelected: (Platform) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Tipo de ingreso / Plataforma",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = TextSecondary
        )

        if (platforms.size <= 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                platforms.forEach { platform ->
                    DynamicPlatformChip(
                        platform = platform,
                        isSelected = platform.id.equals(selectedPlatformId, ignoreCase = true),
                        onClick = { onPlatformSelected(platform) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(platforms, key = { it.id }) { platform ->
                    DynamicPlatformChip(
                        platform = platform,
                        isSelected = platform.id.equals(selectedPlatformId, ignoreCase = true),
                        onClick = { onPlatformSelected(platform) },
                        modifier = Modifier.width(104.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicPlatformChip(
    platform: Platform,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brandColor = ColorUtils.parseHexColor(platform.brandColorHex)
    val contentColor = ColorUtils.getContrastingContentColor(brandColor)

    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) brandColor else TextSecondary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        color = if (isSelected) brandColor else DeepObsidianSurfaceVariant,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = platform.iconName.ifBlank { "🚘" },
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = platform.monogram.take(2).uppercase(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = if (isSelected) contentColor else TextPrimary
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = platform.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    color = if (isSelected) contentColor else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
