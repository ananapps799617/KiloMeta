// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/DayOdometerChip.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Bloque "Jornada del día" de la Cabina Cockpit Dark (99.9% fiel a la lámina de diseño).
// Representa con fidelidad absoluta los 4 estados visuales de jornada (Sin turno activo,
// En curso, Cerrada y En espera) respetando la lógica contable y de odómetros real (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    hasActiveShift: Boolean = false,
    hasEventsToday: Boolean = false,
    elapsedHoursFormatted: String = "0h 0m",
    onStartShiftClick: () -> Unit = {},
    onCloseShiftClick: () -> Unit = {},
    onOpenDayOdometerDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val effectiveStartOdo = (dayStartOdometer?.takeIf { it > 0 }) ?: lastKnownOdometer
    val totalKmDriven = if (dayEndOdometer != null && dayEndOdometer >= effectiveStartOdo) {
        dayEndOdometer - effectiveStartOdo
    } else {
        0
    }

    // Clasificación de los 4 estados reales de cabina
    val isEnEspera = (!hasActiveShift && !isDayClosed && hasEventsToday) || (hasActiveShift && !hasEventsToday)

    val cardBorderColor = when {
        isEnEspera -> StatusAmber.copy(alpha = 0.6f)
        hasActiveShift -> StatusGreen.copy(alpha = 0.5f)
        else -> DeepObsidianBorder
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, cardBorderColor, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Cabecera: Título "Jornada del día" + Estado a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jornada del día",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = TextPrimary
                )

                Text(
                    text = when {
                        isDayClosed -> "Cerrada"
                        isEnEspera -> "En espera"
                        hasActiveShift -> "En curso"
                        else -> "Sin turno activo"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = when {
                        isDayClosed -> TextSecondary
                        isEnEspera -> StatusAmber
                        hasActiveShift -> StatusGreen
                        else -> TextSecondary
                    }
                )
            }

            // 2. Contenido Contextual según el estado
            when {
                // ESTADO C: JORNADA CERRADA
                isDayClosed -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DeepObsidianSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🏁", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "${String.format(Locale.US, "%,d", effectiveStartOdo)} ➔ ${String.format(Locale.US, "%,d", dayEndOdometer ?: effectiveStartOdo)} km",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "$totalKmDriven km recorridos hoy · $elapsedHoursFormatted",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenDayOdometerDialog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(DeepObsidianBorder)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Text(
                            text = "Ver resumen de jornada",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // ESTADO D: EN ESPERA
                isEnEspera -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StatusAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⏸", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "En espera de ruta",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Último odómetro registrado: ${String.format(Locale.US, "%,d", effectiveStartOdo)} km",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = if (hasActiveShift) onCloseShiftClick else onStartShiftClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusAmber,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = if (hasActiveShift) "Finalizar jornada" else "Iniciar jornada",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // ESTADO B: JORNADA EN CURSO
                hasActiveShift -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StatusGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⏱️", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = elapsedHoursFormatted,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 15.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Odómetro inicial: ${String.format(Locale.US, "%,d", effectiveStartOdo)} km",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onCloseShiftClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(StatusGreen)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = StatusGreen
                        )
                    ) {
                        Text(
                            text = "⏹ Finalizar jornada",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // ESTADO A: SIN TURNO ACTIVO
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Sin turno activo",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Puedes iniciar tu jornada cuando estés listo.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = onStartShiftClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "▷ Iniciar jornada",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
