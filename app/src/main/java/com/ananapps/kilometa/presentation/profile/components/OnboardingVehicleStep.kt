// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/OnboardingVehicleStep.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Paso 2 del Onboarding / Edición: Configuración mecánica y legal del vehículo.
// Modalidad (Carro, Taxi, Moto), placa colombiana, odómetro base, marca, modelo,
// sistema de tanques en tarjetas de ancho completo y régimen de tenencia.
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.TankSystem
import com.ananapps.kilometa.domain.model.VehicleCatalog
import com.ananapps.kilometa.domain.model.VehicleTenure
import com.ananapps.kilometa.domain.model.VehicleType
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianBorder
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

@Composable
fun OnboardingVehicleStep(
    vehiclePlate: String,
    onVehiclePlateChange: (String) -> Unit,
    vehicleType: VehicleType,
    onVehicleTypeChange: (VehicleType) -> Unit,
    tankSystem: TankSystem,
    onTankSystemChange: (TankSystem) -> Unit,
    brand: String,
    onBrandChange: (String) -> Unit,
    model: String,
    onModelChange: (String) -> Unit,
    propulsionType: PropulsionType = PropulsionType.BI_FUEL,
    onPropulsionChange: (PropulsionType) -> Unit = {},
    tenureType: VehicleTenure,
    onTenureChange: (VehicleTenure) -> Unit,
    initialOdometer: Int,
    onInitialOdometerChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBrandDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }
    var odoText by remember(initialOdometer) {
        mutableStateOf(if (initialOdometer > 0) initialOdometer.toString() else "")
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Título del Paso
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "🚗 Vehículo de Trabajo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            Text(text = "Calibra especificaciones para rentabilidad y autonomía exacta.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }

        // 1. Selector Modalidad Tripartita
        Text(text = "Modalidad del Vehículo", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VehicleType.entries.forEach { type ->
                FilterChip(
                    selected = vehicleType == type,
                    onClick = {
                        onVehicleTypeChange(type)
                        val newBrands = VehicleCatalog.getBrandsFor(type)
                        if (newBrands.isNotEmpty() && brand !in newBrands) {
                            onBrandChange(newBrands.first())
                            val models = VehicleCatalog.getModelsForBrand(newBrands.first())
                            if (models.isNotEmpty()) onModelChange(models.first())
                        }
                    },
                    label = { Text("${type.icon} ${type.displayName}", maxLines = 1) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ElectricBluePrimary, selectedLabelColor = DeepObsidianBackground)
                )
            }
        }

        // 2. Placa Colombiana Estilizada
        Text(text = "Placa del Vehículo", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFFFD54F).copy(alpha = 0.15f)).border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            OutlinedTextField(
                value = vehiclePlate,
                onValueChange = { if (it.length <= 8) onVehiclePlateChange(it.uppercase()) },
                placeholder = { Text("AAA-123", textAlign = TextAlign.Center) },
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center, color = Color(0xFFFFD54F)),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // 3. Odómetro Base de Ingreso (km)
        OutlinedTextField(
            value = odoText,
            onValueChange = { input ->
                val clean = input.filter { it.isDigit() }
                odoText = clean
                onInitialOdometerChange(clean.toIntOrNull() ?: 0)
            },
            placeholder = { Text("Ej: 208500") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Odómetro Base de Ingreso (km)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 4. Selector de Marca y Modelo
        CatalogSelector(label = "Marca (${vehicleType.displayName})", value = brand, placeholder = "Seleccionar Marca...", onClick = { showBrandDialog = true })
        CatalogSelector(label = "Modelo / Línea ($brand)", value = model, placeholder = "Seleccionar Modelo...", onClick = { showModelDialog = true })

        // 5. Configuración de Tanques (Tarjetas completas para evitar partir texto)
        Text(text = "Sistema de Tanques de Combustible", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        TankSystem.entries.forEach { tank ->
            val isSelected = tankSystem == tank
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (isSelected) ElectricBluePrimary.copy(alpha = 0.15f) else DeepObsidianSurface).border(1.5.dp, if (isSelected) ElectricBluePrimary else DeepObsidianBorder, RoundedCornerShape(12.dp)).clickable {
                    onTankSystemChange(tank)
                    val prop = if (tank == TankSystem.DUAL_TANK) PropulsionType.BI_FUEL else PropulsionType.GASOLINE_ONLY
                    onPropulsionChange(prop)
                }.padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = tank.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = if (isSelected) ElectricBluePrimary else TextPrimary)
                        if (isSelected) Text("✓ Seleccionado", style = MaterialTheme.typography.labelSmall, color = ElectricBluePrimary)
                    }
                    Text(text = tank.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        // 6. Régimen de Tenencia (Propio vs Renta)
        Text(text = "Régimen de Tenencia", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VehicleTenure.entries.forEach { ten ->
                FilterChip(
                    selected = tenureType == ten,
                    onClick = { onTenureChange(ten) },
                    label = { Text(ten.displayName, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ElectricBluePrimary, selectedLabelColor = DeepObsidianBackground)
                )
            }
        }
    }

    if (showBrandDialog) {
        BrandSelectionDialog(vehicleType = vehicleType, selectedBrand = brand, onBrandSelected = { newBrand ->
            onBrandChange(newBrand)
            val suggested = VehicleCatalog.getModelsForBrand(newBrand)
            if (suggested.isNotEmpty()) onModelChange(suggested.first())
        }, onDismiss = { showBrandDialog = false })
    }

    if (showModelDialog) {
        ModelSelectionDialog(brand = brand, selectedModel = model, onModelSelected = onModelChange, onDismiss = { showModelDialog = false })
    }
}

@Composable
private fun CatalogSelector(label: String, value: String, placeholder: String, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Surface(
            modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, DeepObsidianBorder, RoundedCornerShape(12.dp)).clickable(onClick = onClick),
            color = DeepObsidianSurfaceVariant
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = value.ifBlank { placeholder }, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (value.isNotBlank()) FontWeight.Bold else FontWeight.Normal, color = if (value.isNotBlank()) TextPrimary else TextSecondary))
                Text("⌄", fontSize = 20.sp, color = ElectricBluePrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
