// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/VehicleConfigSection.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Configuración técnica vehicular ampliada: Tipología (Carro vs Moto),
// matrícula colombiana, marcas y modelos asistidos, sistema de tanques
// (Un tanque vs Bi-combustible GNV) y régimen de tenencia.
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.TankSystem
import com.ananapps.kilometa.domain.model.VehicleCatalog
import com.ananapps.kilometa.domain.model.VehicleTenure
import com.ananapps.kilometa.domain.model.VehicleType
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VehicleConfigSection(
    vehiclePlate: String,
    onVehiclePlateChange: (String) -> Unit,
    vehicleType: VehicleType = VehicleType.CAR,
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
    val availableBrands = VehicleCatalog.getBrandsFor(vehicleType)
    val isCustomBrand = brand !in availableBrands

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "🚗 Identidad y Mecánica del Vehículo",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        // 1. Selector Tipo de Vehículo (Carro vs Moto)
        Text(text = "Tipo de Vehículo", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    label = { Text("${type.icon} ${type.displayName}") },
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

        // 3. Catálogo Asistido: Marcas según Tipo
        Text(text = "Marca (${vehicleType.displayName})", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableBrands.forEach { bName ->
                FilterChip(
                    selected = brand == bName,
                    onClick = {
                        onBrandChange(bName)
                        val suggested = VehicleCatalog.getModelsForBrand(bName)
                        if (suggested.isNotEmpty()) onModelChange(suggested.first())
                    },
                    label = { Text(bName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBluePrimary,
                        selectedLabelColor = DeepObsidianBackground
                    )
                )
            }
            FilterChip(
                selected = isCustomBrand,
                onClick = { onBrandChange("") },
                label = { Text("Otro") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElectricBluePrimary,
                    selectedLabelColor = DeepObsidianBackground
                )
            )
        }

        if (isCustomBrand) {
            OutlinedTextField(
                value = brand,
                onValueChange = onBrandChange,
                label = { Text("Escribe la Marca") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // 4. Modelos sugeridos de la marca
        val suggestedModels = VehicleCatalog.getModelsForBrand(brand)
        if (suggestedModels.isNotEmpty()) {
            Text(text = "Modelo / Línea ($brand)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedModels.forEach { m ->
                    FilterChip(
                        selected = model == m,
                        onClick = { onModelChange(m) },
                        label = { Text(m) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusGreen,
                            selectedLabelColor = DeepObsidianBackground
                        )
                    )
                }
            }
        } else {
            OutlinedTextField(
                value = model,
                onValueChange = onModelChange,
                label = { Text("Modelo / Referencia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
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

        // 6. Régimen de Tenencia
        Text(text = "Régimen de Tenencia", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VehicleTenure.entries.forEach { ten ->
                FilterChip(
                    selected = tenureType == ten,
                    onClick = { onTenureChange(ten) },
                    label = { Text(ten.displayName) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusGreen,
                        selectedLabelColor = DeepObsidianBackground
                    )
                )
            }
        }
    }
}
