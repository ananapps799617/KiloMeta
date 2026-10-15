package com.ananapps.kilometa.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.ColorUtils
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.ObsidianBorder

/**
 * Selector ergonómico de colores vehiculares oficiales para cabina (Orden #055).
 * Cumple con un área táctil mínima de 48x48 dp (Thumb Zone) y espaciado de 10 dp.
 */
@Composable
fun VehicularColorSelector(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    palette: List<String> = ColorUtils.OFFICIAL_VEHICULAR_PALETTE
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        items(palette) { hex ->
            val color = ColorUtils.parseHexColor(hex)
            val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
            val isWhite = hex.equals("#FFFFFF", ignoreCase = true)

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onColorSelected(hex) }
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(color, CircleShape)
                        .border(
                            width = if (isSelected) 3.dp else if (isWhite) 1.5.dp else 1.dp,
                            color = when {
                                isSelected -> ElectricBluePrimary
                                isWhite -> ObsidianBorder
                                else -> Color.Transparent
                            },
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
