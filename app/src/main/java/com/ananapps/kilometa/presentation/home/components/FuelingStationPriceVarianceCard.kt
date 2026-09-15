// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/FuelingStationPriceVarianceCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Componente de alerta interactiva cuando el precio de surtidor difiere del memorizado
// en el catálogo de bombas. Permite actualizar la tarifa con un solo toque (Orden #040).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.FuelType
import com.ananapps.kilometa.domain.model.Place
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun FuelingStationPriceVarianceCard(
    station: Place,
    fuelType: FuelType,
    knownPrice: Double,
    currentPrice: Double,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DeepObsidianSurfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "¿Actualizar tarifa de ${fuelType.displayName}?",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
                Text(
                    text = "${station.name}: $ %,.0f ➔ $ %,.0f / %s".format(
                        knownPrice,
                        currentPrice,
                        fuelType.unitLabel
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor
                )
            )
        }
    }
}
