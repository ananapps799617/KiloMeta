// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/OnboardingVehicleStep.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pestaña 2 del Onboarding: Configuración del vehículo de trabajo.
// Selectores ergonómicos de modalidad, placa colombiana, marca, modelo, propulsión y odómetro (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

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
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.TankSystem
import com.ananapps.kilometa.domain.model.VehicleCatalog
import com.ananapps.kilometa.domain.model.VehicleTenure
import com.ananapps.kilometa.domain.model.VehicleType
import com.ananapps.kilometa.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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
    var typeExpanded by remember { mutableStateOf(false) }
    var brandExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }
    var propExpanded by remember { mutableStateOf(false) }

    var odoText by remember(initialOdometer) {
        mutableStateOf(if (initialOdometer > 0) initialOdometer.toString() else "")
    }

    val availableBrands = remember(vehicleType) { VehicleCatalog.getBrandsFor(vehicleType) }
    val availableModels = remember(brand) { VehicleCatalog.getModelsForBrand(brand) }

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
                text = "Datos del vehículo",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = TextPrimary
            )
            Text(
                text = "Información de tu vehículo para identificarlo en la plataforma.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // 1. Modalidad del Vehículo
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = it }
        ) {
            OutlinedTextField(
                value = "${vehicleType.icon} ${vehicleType.displayName}",
                onValueChange = {},
                readOnly = true,
                label = { Text("Modalidad *", color = TextSecondary) },
                leadingIcon = { Text("🚗", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                VehicleType.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text("${option.icon} ${option.displayName}", color = if (vehicleType == option) EmeraldAccent else TextPrimary) },
                        onClick = {
                            onVehicleTypeChange(option)
                            val newBrands = VehicleCatalog.getBrandsFor(option)
                            if (newBrands.isNotEmpty() && brand !in newBrands) {
                                onBrandChange(newBrands.first())
                                val models = VehicleCatalog.getModelsForBrand(newBrands.first())
                                if (models.isNotEmpty()) onModelChange(models.first())
                            }
                            typeExpanded = false
                        }
                    )
                }
            }
        }

        // 2. Placa del Vehículo
        OutlinedTextField(
            value = vehiclePlate,
            onValueChange = { if (it.length <= 8) onVehiclePlateChange(it.uppercase()) },
            label = { Text("Placa *", color = TextSecondary) },
            placeholder = { Text("Ingresa la placa (ej: WMB-789)", color = TextSecondary.copy(alpha = 0.5f)) },
            leadingIcon = { Text("🚘", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp)
        )

        // 3. Marca del Vehículo
        ExposedDropdownMenuBox(
            expanded = brandExpanded,
            onExpandedChange = { brandExpanded = it }
        ) {
            OutlinedTextField(
                value = brand.ifBlank { "Selecciona la marca" },
                onValueChange = {},
                readOnly = true,
                label = { Text("Marca *", color = TextSecondary) },
                leadingIcon = { Text("🏷️", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded) },
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = brandExpanded,
                onDismissRequest = { brandExpanded = false }
            ) {
                availableBrands.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = if (brand == option) EmeraldAccent else TextPrimary) },
                        onClick = {
                            onBrandChange(option)
                            val models = VehicleCatalog.getModelsForBrand(option)
                            if (models.isNotEmpty()) onModelChange(models.first())
                            brandExpanded = false
                        }
                    )
                }
            }
        }

        // 4. Modelo del Vehículo
        ExposedDropdownMenuBox(
            expanded = modelExpanded,
            onExpandedChange = { modelExpanded = it }
        ) {
            OutlinedTextField(
                value = model.ifBlank { "Selecciona el modelo" },
                onValueChange = {},
                readOnly = true,
                label = { Text("Modelo *", color = TextSecondary) },
                leadingIcon = { Text("🧭", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = modelExpanded,
                onDismissRequest = { modelExpanded = false }
            ) {
                availableModels.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = if (model == option) EmeraldAccent else TextPrimary) },
                        onClick = {
                            onModelChange(option)
                            modelExpanded = false
                        }
                    )
                }
            }
        }

        // 5. Tipo de Combustible / Propulsión
        ExposedDropdownMenuBox(
            expanded = propExpanded,
            onExpandedChange = { propExpanded = it }
        ) {
            OutlinedTextField(
                value = propulsionType.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de combustible *", color = TextSecondary) },
                leadingIcon = { Text("⛽", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = propExpanded) },
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = propExpanded,
                onDismissRequest = { propExpanded = false }
            ) {
                PropulsionType.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayName, color = if (propulsionType == option) EmeraldAccent else TextPrimary) },
                        onClick = {
                            onPropulsionChange(option)
                            propExpanded = false
                        }
                    )
                }
            }
        }

        // 6. Odómetro Inicial (km)
        OutlinedTextField(
            value = odoText,
            onValueChange = { input ->
                val clean = input.filter { it.isDigit() }
                odoText = clean
                onInitialOdometerChange(clean.toIntOrNull() ?: 0)
            },
            label = { Text("Odómetro inicial (km) *", color = TextSecondary) },
            placeholder = { Text("Ingresa el kilometraje (ej: 125000)", color = TextSecondary.copy(alpha = 0.5f)) },
            leadingIcon = { Text("🔢", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
