// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/OnboardingFinancialStep.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pestaña 3 del Onboarding: Metas de trabajo y horario operativo.
// Semántica adaptada (Ganancia Limpia vs Cuota de Renta), corte semanal condicional y cero métodos de pago (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.QuotaPeriodicity
import com.ananapps.kilometa.domain.model.VehicleTenure
import com.ananapps.kilometa.presentation.theme.*

private val CUTOFF_DAYS = listOf(1 to "Lunes", 2 to "Martes", 3 to "Miércoles", 4 to "Jueves", 5 to "Viernes", 6 to "Sábado", 7 to "Domingo")
private val CUTOFF_HOURS = listOf(6 to "06:00 AM", 14 to "02:00 PM (14:00)", 18 to "06:00 PM (18:00)", 20 to "08:00 PM (20:00)", 22 to "10:00 PM (22:00)", 23 to "11:59 PM (Cierre)")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingFinancialStep(
    tenureType: VehicleTenure,
    onTenureChange: (VehicleTenure) -> Unit = {},
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
    userEmail: String = "",
    onResetData: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isRented = tenureType == VehicleTenure.RENTED
    var perExpanded by remember { mutableStateOf(false) }
    var tenureExpanded by remember { mutableStateOf(false) }
    var cutoffDayExpanded by remember { mutableStateOf(false) }
    var cutoffHourExpanded by remember { mutableStateOf(false) }

    val formattedQuota = remember(quotaAmount) {
        if (quotaAmount > 0) "%,d".format(quotaAmount.toLong()).replace(',', '.') else ""
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = EmeraldAccent,
        unfocusedBorderColor = ObsidianBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedContainerColor = DeepObsidianSurface,
        unfocusedContainerColor = DeepObsidianSurface
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Título de la Etapa
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Metas de trabajo",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = TextPrimary
            )
            Text(
                text = "Configura tu meta financiera y tu horario de trabajo.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // 1. Periodicidad
        ExposedDropdownMenuBox(
            expanded = perExpanded,
            onExpandedChange = { perExpanded = it }
        ) {
            OutlinedTextField(
                value = quotaPeriodicity.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Periodicidad *", color = TextSecondary) },
                leadingIcon = { Text("🔄", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = perExpanded) },
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = perExpanded,
                onDismissRequest = { perExpanded = false }
            ) {
                QuotaPeriodicity.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayName, color = if (quotaPeriodicity == option) EmeraldAccent else TextPrimary) },
                        onClick = {
                            onQuotaPeriodicityChange(option)
                            perExpanded = false
                        }
                    )
                }
            }
        }

        // 2. Monto con semántica adaptada a la tenencia
        OutlinedTextField(
            value = formattedQuota,
            onValueChange = { onQuotaAmountChange(it.filter { ch -> ch.isDigit() }.toDoubleOrNull() ?: 0.0) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = {
                Text(
                    text = if (isRented) "Monto de la cuota (COP) *" else "Tu Meta de Ganancia Limpia (COP) *",
                    color = TextSecondary
                )
            },
            placeholder = { Text("Ingresa el monto (ej: 500.000)", color = TextSecondary.copy(alpha = 0.5f)) },
            leadingIcon = { Text("💵", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
            trailingIcon = { Text("COP", color = TextSecondary, modifier = Modifier.padding(end = 12.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp)
        )

        // 3. Días Laborados por Semana
        OutlinedTextField(
            value = if (workDaysWeek > 0) "$workDaysWeek días a la semana" else "6 días a la semana",
            onValueChange = {},
            readOnly = true,
            label = { Text("Días laborados por semana *", color = TextSecondary) },
            leadingIcon = { Text("📅", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
            trailingIcon = {
                Row(modifier = Modifier.padding(end = 8.dp)) {
                    TextButton(onClick = { if (workDaysWeek > 1) onWorkDaysWeekChange(workDaysWeek - 1) }) { Text("➖", color = TextSecondary) }
                    TextButton(onClick = { if (workDaysWeek < 7) onWorkDaysWeekChange(workDaysWeek + 1) }) { Text("➕", color = EmeraldAccent) }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp)
        )

        // 4. Corte Semanal Condicional (solo si es Semanal)
        AnimatedVisibility(
            visible = quotaPeriodicity == QuotaPeriodicity.WEEKLY,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Selector Día de Corte
                ExposedDropdownMenuBox(
                    expanded = cutoffDayExpanded,
                    onExpandedChange = { cutoffDayExpanded = it }
                ) {
                    val currentDayLabel = CUTOFF_DAYS.find { it.first == quotaCutoffDay }?.second ?: "Martes"
                    OutlinedTextField(
                        value = currentDayLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Día de corte *", color = TextSecondary) },
                        leadingIcon = { Text("🗓️", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cutoffDayExpanded) },
                        colors = fieldColors,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = cutoffDayExpanded,
                        onDismissRequest = { cutoffDayExpanded = false }
                    ) {
                        CUTOFF_DAYS.forEach { (num, name) ->
                            DropdownMenuItem(
                                text = { Text(name, color = if (quotaCutoffDay == num) EmeraldAccent else TextPrimary) },
                                onClick = { onQuotaCutoffDayChange(num); cutoffDayExpanded = false }
                            )
                        }
                    }
                }

                // Selector Hora de Corte
                ExposedDropdownMenuBox(
                    expanded = cutoffHourExpanded,
                    onExpandedChange = { cutoffHourExpanded = it }
                ) {
                    val currentHourLabel = CUTOFF_HOURS.find { it.first == quotaCutoffHour }?.second ?: "11:59 PM (Cierre)"
                    OutlinedTextField(
                        value = currentHourLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hora de corte *", color = TextSecondary) },
                        leadingIcon = { Text("⏰", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cutoffHourExpanded) },
                        colors = fieldColors,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = cutoffHourExpanded,
                        onDismissRequest = { cutoffHourExpanded = false }
                    ) {
                        CUTOFF_HOURS.forEach { (num, name) ->
                            DropdownMenuItem(
                                text = { Text(name, color = if (quotaCutoffHour == num) EmeraldAccent else TextPrimary) },
                                onClick = { onQuotaCutoffHourChange(num); cutoffHourExpanded = false }
                            )
                        }
                    }
                }
            }
        }

        // 5. Tenencia del Vehículo
        ExposedDropdownMenuBox(
            expanded = tenureExpanded,
            onExpandedChange = { tenureExpanded = it }
        ) {
            OutlinedTextField(
                value = tenureType.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tu vehículo es: *", color = TextSecondary) },
                leadingIcon = { Text("🚘", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tenureExpanded) },
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = tenureExpanded,
                onDismissRequest = { tenureExpanded = false }
            ) {
                VehicleTenure.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayName, color = if (tenureType == option) EmeraldAccent else TextPrimary) },
                        onClick = {
                            onTenureChange(option)
                            tenureExpanded = false
                        }
                    )
                }
            }
        }

        // Zona de Desarrollo para soporte interno (blindada para conductores regulares)
        DevTestingSection(userEmail = userEmail, onResetData = onResetData)
    }
}
