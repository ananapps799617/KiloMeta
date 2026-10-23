// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/DayBreaksEditorSection.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Componente UI para seleccionar y editar tiempos de pausa y descanso de la jornada (Orden #069).
// Permite descontar comidas o descansos largos para obtener el tiempo efectivo real y $/hora limpio.
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun DayBreaksEditorSection(
    breakMinutes: Int,
    onBreakMinutesChange: (Int) -> Unit,
    grossMinutes: Int,
    modifier: Modifier = Modifier
) {
    val effectiveMinutes = (grossMinutes - breakMinutes).coerceAtLeast(0)

    fun formatMins(m: Int): String {
        val h = m / 60
        val rem = m % 60
        return if (h > 0) "${h}h ${rem}m" else "${rem}m"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PAUSAS Y DESCANSOS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
            if (breakMinutes > 0) {
                Text(
                    text = "Limpiar",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = StatusRed,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onBreakMinutesChange(0) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Fichas de ajuste rápido (Presets)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BreakPresetChip("☕ Desayuno +20m", modifier = Modifier.weight(1f)) {
                onBreakMinutesChange(breakMinutes + 20)
            }
            BreakPresetChip("🍽️ Almuerzo +60m", modifier = Modifier.weight(1f)) {
                onBreakMinutesChange(breakMinutes + 60)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BreakPresetChip("🛋️ Descanso +120m", modifier = Modifier.weight(1f)) {
                onBreakMinutesChange(breakMinutes + 120)
            }
            BreakPresetChip("💤 Siesta +180m", modifier = Modifier.weight(1f)) {
                onBreakMinutesChange(breakMinutes + 180)
            }
        }

        // Selector con botones +/-
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DeepObsidianSurfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Descanso: ${formatMins(breakMinutes)} ($breakMinutes min)",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (breakMinutes > 0) StatusAmber else TextSecondary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onBreakMinutesChange((breakMinutes - 15).coerceAtLeast(0)) },
                    modifier = Modifier.size(32.dp),
                    enabled = breakMinutes > 0
                ) {
                    Text("−", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                }
                IconButton(
                    onClick = { onBreakMinutesChange(breakMinutes + 15) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "+15m", tint = ElectricBluePrimary)
                }
            }
        }

        // Desglose en vivo de Horas Efectivas vs Brutas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ElectricBluePrimary.copy(alpha = 0.10f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bruto: ${formatMins(grossMinutes)}  ·  Descanso: -${formatMins(breakMinutes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "Tiempo Efectivo de Ruta:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
                Text(
                    text = formatMins(effectiveMinutes),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = StatusGreen
                )
            }
        }
    }
}

@Composable
private fun BreakPresetChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = DeepObsidianSurfaceVariant,
        tonalElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp),
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}
