// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/VehicleQuotaSection.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Subcomponente ergonómico para configurar la cuota financiera del vehículo:
// Periodicidad, monto, días laborados, selector de día de corte (Lun..Dom)
// y selector rápido de hora límite de pago (chips gremiales + stepper fino).
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.QuotaPeriodicity
import com.ananapps.kilometa.domain.model.VehicleTenure
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.ObsidianBorder
import com.ananapps.kilometa.presentation.theme.StatusAmber
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

@Composable
fun VehicleQuotaSection(
    tenureType: VehicleTenure,
    quotaAmount: Double,
    onQuotaAmountChange: (Double) -> Unit,
    quotaPeriodicity: QuotaPeriodicity,
    onQuotaPeriodicityChange: (QuotaPeriodicity) -> Unit,
    workDaysWeek: Int,
    onWorkDaysWeekChange: (Int) -> Unit,
    quotaCutoffDay: Int,
    onQuotaCutoffDayChange: (Int) -> Unit,
    quotaCutoffHour: Int,
    onQuotaCutoffHourChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isRented = tenureType == VehicleTenure.RENTED

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Título del bloque
        Text(
            text = if (isRented) "💰 Cuota y Liquidación de Entrega" else "🎯 Meta Financiera Personal",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        // Periodicidad
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuotaPeriodicity.entries.forEach { per ->
                FilterChip(
                    selected = quotaPeriodicity == per,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onQuotaPeriodicityChange(per)
                    },
                    label = { Text(per.displayName) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBluePrimary,
                        selectedLabelColor = DeepObsidianBackground
                    )
                )
            }
        }

        // Monto de cuota con formato de miles
        val formattedQuota = remember(quotaAmount) {
            if (quotaAmount > 0) "%,d".format(quotaAmount.toLong()).replace(',', '.') else ""
        }
        OutlinedTextField(
            value = formattedQuota,
            onValueChange = { onQuotaAmountChange(it.filter { ch -> ch.isDigit() }.toDoubleOrNull() ?: 0.0) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Text("$", color = ElectricBluePrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
            trailingIcon = { Text("COP", color = TextSecondary, modifier = Modifier.padding(end = 12.dp)) },
            label = { Text("Monto de Cuota (${quotaPeriodicity.displayName})") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Días Laborados por Semana
        Text(text = "Días Laborados: $workDaysWeek días/semana", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val daysLabels = listOf("L", "M", "M", "J", "V", "S", "D")
            daysLabels.forEachIndexed { index, label ->
                val isActive = index < workDaysWeek
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isActive) ElectricBluePrimary else DeepObsidianSurface)
                        .border(1.dp, if (isActive) ElectricBluePrimary else ObsidianBorder, CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onWorkDaysWeekChange(index + 1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isActive) DeepObsidianBackground else TextSecondary
                    )
                }
            }
        }

        // Bloque Permanente de Corte Semanal (Calibrable para cualquier meta semanal)
        AnimatedVisibility(
            visible = quotaPeriodicity == QuotaPeriodicity.WEEKLY,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DeepObsidianSurface)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "📅 Día de Entrega / Corte Semanal",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )

                // Selector de Día de Pago: 1 (Lunes) a 7 (Domingo)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val weekdays = listOf(
                        1 to "Lun", 2 to "Mar", 3 to "Mié",
                        4 to "Jue", 5 to "Vie", 6 to "Sáb", 7 to "Dom"
                    )
                    weekdays.forEach { (dayNum, dayLabel) ->
                        val isSelected = quotaCutoffDay == dayNum
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ElectricBluePrimary else DeepObsidianBackground)
                                .border(1.dp, if (isSelected) ElectricBluePrimary else ObsidianBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onQuotaCutoffDayChange(dayNum)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) DeepObsidianBackground else TextPrimary
                            )
                        }
                    }
                }

                // Selector de Hora Límite de Pago
                Text(
                    text = "⏰ Hora Límite de Corte: ${formatHourString(quotaCutoffHour)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                val hourRows = listOf(
                    listOf(6 to "06:00", 14 to "14:00", 18 to "18:00"),
                    listOf(20 to "20:00", 22 to "22:00", 23 to "23:59")
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    hourRows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { (h, label) ->
                                val isSelected = quotaCutoffHour == h
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) ElectricBluePrimary else DeepObsidianBackground)
                                        .border(1.dp, if (isSelected) ElectricBluePrimary else ObsidianBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onQuotaCutoffHourChange(h)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) DeepObsidianBackground else TextPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }

                val dayName = when (quotaCutoffDay) {
                    1 -> "LUNES"; 2 -> "MARTES"; 3 -> "MIÉRCOLES"; 4 -> "JUEVES"
                    5 -> "VIERNES"; 6 -> "SÁBADO"; else -> "DOMINGO"
                }
                Text(
                    text = "🔔 Tu cuota vencerá todos los $dayName a las ${formatHourString(quotaCutoffHour)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBluePrimary
                )
            }
        }
    }
}

private fun formatHourString(hour: Int): String {
    val h12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val ampm = if (hour < 12) "AM" else "PM"
    return String.format("%02d:00 hrs (%02d:00 %s)", hour, h12, ampm)
}
