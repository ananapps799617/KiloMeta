// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/DriverProfileScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla de configuración del perfil del conductor y especificaciones técnicas
// de su vehículo (multi-propulsión, tenencia, cuota objetivo y días de trabajo).
// Diseñada con alta ergonomía en cabina (chips táctiles y botón de guardado >= 56dp).
// ==============================================================================

package com.ananapps.kilometa.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ananapps.kilometa.domain.model.DriverProfile
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.QuotaPeriodicity
import com.ananapps.kilometa.domain.model.VehicleTenure
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.ObsidianBorder
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

// ------------------------------------------------------------------------------
// BLOQUE 1: PANTALLA PRINCIPAL DE PERFIL Y VEHÍCULO
// ------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DriverProfileScreen(
    viewModel: DriverProfileViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val currentProfile by viewModel.profile.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var vehicleName by remember(currentProfile) { mutableStateOf(currentProfile.vehicleName) }
    var vehiclePlate by remember(currentProfile) { mutableStateOf(currentProfile.vehiclePlate) }
    var selectedPropulsion by remember(currentProfile) { mutableStateOf(currentProfile.propulsionType) }
    var selectedTenure by remember(currentProfile) { mutableStateOf(currentProfile.vehicleTenure) }
    var quotaAmount by remember(currentProfile) { mutableDoubleStateOf(currentProfile.targetQuotaAmount) }
    var quotaPeriodicity by remember(currentProfile) { mutableStateOf(currentProfile.quotaPeriodicity) }
    var workingDaysCount by remember(currentProfile) { mutableIntStateOf(currentProfile.workingDaysPerWeek) }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            snackbarHostState.showSnackbar("Configuración vehicular guardada exitosamente.")
            viewModel.resetSavedNotification()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidianBackground)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 76.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            Text(
                text = "Mi Perfil & Vehículo",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            // Datos del Vehículo
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = vehicleName,
                    onValueChange = { vehicleName = it },
                    label = { Text("Modelo / Referencia del Vehículo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = vehiclePlate,
                    onValueChange = { vehiclePlate = it.uppercase() },
                    label = { Text("Placa del Vehículo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Sistema de Propulsión (5 opciones)
            Text(text = "Sistema de Propulsión", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PropulsionType.entries.forEach { propulsion ->
                    FilterChip(
                        selected = selectedPropulsion == propulsion,
                        onClick = { selectedPropulsion = propulsion },
                        label = { Text(propulsion.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBluePrimary,
                            selectedLabelColor = DeepObsidianBackground
                        )
                    )
                }
            }

            // Régimen de Tenencia
            Text(text = "Régimen de Tenencia", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VehicleTenure.entries.forEach { tenure ->
                    FilterChip(
                        selected = selectedTenure == tenure,
                        onClick = { selectedTenure = tenure },
                        label = { Text(tenure.displayName) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusGreen,
                            selectedLabelColor = DeepObsidianBackground
                        )
                    )
                }
            }

            // Periodicidad y Meta de Cuota
            Text(text = "Meta Financiera Objetivo", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuotaPeriodicity.entries.forEach { periodicity ->
                    FilterChip(
                        selected = quotaPeriodicity == periodicity,
                        onClick = { quotaPeriodicity = periodicity },
                        label = { Text(periodicity.displayName) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBluePrimary,
                            selectedLabelColor = DeepObsidianBackground
                        )
                    )
                }
            }
            OutlinedTextField(
                value = quotaAmount.toInt().toString(),
                onValueChange = { quotaAmount = it.filter { ch -> ch.isDigit() }.toDoubleOrNull() ?: 0.0 },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Text("$", color = ElectricBluePrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                label = { Text("Monto de Cuota (${quotaPeriodicity.displayName})") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Días Laborados por Semana (7 toggles circulares L M M J V S D)
            Text(text = "Días Laborados: $workingDaysCount días/semana", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val daysLabels = listOf("L", "M", "M", "J", "V", "S", "D")
                daysLabels.forEachIndexed { index, label ->
                    val isDayActive = index < workingDaysCount
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isDayActive) ElectricBluePrimary else DeepObsidianSurface)
                            .border(1.dp, if (isDayActive) ElectricBluePrimary else ObsidianBorder, CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                workingDaysCount = index + 1
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDayActive) DeepObsidianBackground else TextSecondary
                        )
                    }
                }
            }
        }

        // Botón Ergonómico Flotante "Guardar Configuración" (>= 56dp)
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.updateProfile(
                    DriverProfile(
                        vehicleName = vehicleName.trim(),
                        vehiclePlate = vehiclePlate.trim(),
                        propulsionType = selectedPropulsion,
                        vehicleTenure = selectedTenure,
                        targetQuotaAmount = quotaAmount,
                        quotaPeriodicity = quotaPeriodicity,
                        workingDaysPerWeek = workingDaysCount,
                        activePlatforms = currentProfile.activePlatforms
                    )
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
        ) {
            Text(
                text = "💾 Guardar Configuración",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = DeepObsidianBackground
            )
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
