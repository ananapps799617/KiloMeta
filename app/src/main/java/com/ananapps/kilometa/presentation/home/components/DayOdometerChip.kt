// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/DayOdometerChip.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Chip y tarjeta interactiva de cabecera en el Cockpit (Inicio) para gestionar
// los odómetros de arranque y cierre de la jornada (Opción 4, Orden #043).
// Permite al conductor verificar su odómetro inicial y registrar el cierre del día
// sin interrumpir la agilidad del registro de carreras individuales.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun DayOdometerChip(
    dayStartOdometer: Int?,
    dayEndOdometer: Int?,
    isDayClosed: Boolean,
    lastKnownOdometer: Int,
    onOpenDayOdometerDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveStartOdo = (dayStartOdometer?.takeIf { it > 0 }) ?: lastKnownOdometer
    val totalKmDriven = if (dayEndOdometer != null && dayEndOdometer >= effectiveStartOdo) {
        dayEndOdometer - effectiveStartOdo
    } else {
        0
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onOpenDayOdometerDialog() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isDayClosed) "🏁 JORNADA CERRADA" else "🚗 ODÓMETRO DEL DÍA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (isDayClosed) StatusGreen else TextSecondary
                    )
                    if (isDayClosed && totalKmDriven > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StatusGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$totalKmDriven km hoy",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = StatusGreen
                            )
                        }
                    }
                }

                if (isDayClosed && dayEndOdometer != null) {
                    Text(
                        text = "${String.format(Locale.US, "%,d", effectiveStartOdo)} ➔ ${String.format(Locale.US, "%,d", dayEndOdometer)} km",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = TextPrimary
                    )
                } else {
                    Text(
                        text = "Inicio: ${String.format(Locale.US, "%,d", effectiveStartOdo)} km",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = TextPrimary
                    )
                }
            }

            if (isDayClosed) {
                IconButton(
                    onClick = onOpenDayOdometerDialog,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Odómetro",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Button(
                    onClick = onOpenDayOdometerDialog,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBluePrimary,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cierre",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
