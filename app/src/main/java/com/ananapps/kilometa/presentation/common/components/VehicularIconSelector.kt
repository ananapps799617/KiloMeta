package com.ananapps.kilometa.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.core.util.IconCatalog
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.ObsidianBorder

/**
 * Selector ergonómico de glifos/emojis vehiculares certificados para cabina (Orden #055).
 * Cumple con un área táctil mínima de 48x48 dp y selección visual de alto contraste.
 */
@Composable
fun VehicularIconSelector(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    icons: List<String> = IconCatalog.VEHICULAR_ICONS
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        items(icons) { icon ->
            val isSelected = selectedIcon == icon

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isSelected) ElectricBluePrimary.copy(alpha = 0.25f) else DeepObsidianSurfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) ElectricBluePrimary else ObsidianBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onIconSelected(icon) }
            ) {
                Text(
                    text = icon,
                    fontSize = 22.sp
                )
            }
        }
    }
}
