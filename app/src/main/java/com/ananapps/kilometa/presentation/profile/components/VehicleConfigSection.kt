// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/VehicleConfigSection.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Configuración técnica vehicular: Modalidad tripartita (Particular, Taxi, Moto),
// placa colombiana estilizada, selectores asistidos de marca/modelo y régimen de tenencia.
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.TankSystem
import com.ananapps.kilometa.domain.model.VehicleCatalog
import com.ananapps.kilometa.domain.model.VehicleTenure
import com.ananapps.kilometa.domain.model.VehicleType
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun VehicleConfigSection(
    vehiclePlate: String,
    onVehiclePlateChange: (String) -> Unit,
    vehicleType: VehicleType = VehicleType.PARTICULAR,
    onVehicleTypeChange: (VehicleType) -> Unit = {},
    tankSystem: TankSystem = TankSystem.DUAL_TANK,
    onTankSystemChange: (TankSystem) -> Unit = {},
    brand: String,
    onBrandChange: (String) -> Unit,
    model: String,
    onModelChange: (String) -> Unit,
    propulsionType: PropulsionType,
    onPropulsionChange: (PropulsionType) -> Unit,
    tenureType: VehicleTenure,
    onTenureChange: (VehicleTenure) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBrandDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "🚗 Identidad y Mecánica del Vehículo",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        // 1. Selector Tipo de Vehículo Tripartito (Particular vs Taxi vs Moto)
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
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBluePrimary,
                        selectedLabelColor = DeepObsidianBackground
                    )
                )
            }
        }

        // 2. Matrícula Colombiana Estilizada
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "Placa del Vehículo", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFD54F).copy(alpha = 0.15f))
                    .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedTextField(
                    value = vehiclePlate,
                    onValueChange = { if (it.length <= 8) onVehiclePlateChange(it.uppercase()) },
                    placeholder = { Text("AAA-123", textAlign = TextAlign.Center) },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFFFD54F)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // 3. Selector de Marca Desplegable con Buscador
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "Marca (${vehicleType.displayName})", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DeepObsidianBorder, RoundedCornerShape(12.dp))
                    .clickable { showBrandDialog = true },
                color = DeepObsidianSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("🏷️", fontSize = 18.sp)
                        Text(
                            text = brand.ifBlank { "Seleccionar Marca..." },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (brand.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                                color = if (brand.isNotBlank()) TextPrimary else TextSecondary
                            )
                        )
                    }
                    Text("⌄", fontSize = 20.sp, color = ElectricBluePrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. Selector de Modelo Desplegable
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "Modelo / Línea ($brand)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DeepObsidianBorder, RoundedCornerShape(12.dp))
                    .clickable { showModelDialog = true },
                color = DeepObsidianSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("🚘", fontSize = 18.sp)
                        Text(
                            text = model.ifBlank { "Seleccionar Modelo..." },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (model.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                                color = if (model.isNotBlank()) TextPrimary else TextSecondary
                            )
                        )
                    }
                    Text("⌄", fontSize = 20.sp, color = ElectricBluePrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 5. Sistema de Tanques (Un Tanque vs Dos Tanques Bi-Combustible)
        Text(text = "Configuración de Tanques", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TankSystem.entries.forEach { tank ->
                FilterChip(
                    selected = tankSystem == tank,
                    onClick = { onTankSystemChange(tank) },
                    label = { Text(tank.displayName) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBluePrimary,
                        selectedLabelColor = DeepObsidianBackground
                    )
                )
            }
        }

        // 6. Régimen de Tenencia (Unificado a ElectricBluePrimary)
        Text(text = "Régimen de Tenencia", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VehicleTenure.entries.forEach { ten ->
                FilterChip(
                    selected = tenureType == ten,
                    onClick = { onTenureChange(ten) },
                    label = { Text(ten.displayName) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBluePrimary,
                        selectedLabelColor = DeepObsidianBackground
                    )
                )
            }
        }
    }

    if (showBrandDialog) {
        BrandSelectionDialog(
            vehicleType = vehicleType,
            selectedBrand = brand,
            onBrandSelected = { newBrand ->
                onBrandChange(newBrand)
                val suggested = VehicleCatalog.getModelsForBrand(newBrand)
                if (suggested.isNotEmpty()) onModelChange(suggested.first())
            },
            onDismiss = { showBrandDialog = false }
        )
    }

    if (showModelDialog) {
        ModelSelectionDialog(
            brand = brand,
            selectedModel = model,
            onModelSelected = onModelChange,
            onDismiss = { showModelDialog = false }
        )
    }
}
