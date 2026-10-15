package com.ananapps.kilometa.presentation.platform.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.core.util.IconCatalog
import com.ananapps.kilometa.domain.model.Platform
import com.ananapps.kilometa.presentation.common.components.VehicularColorSelector
import com.ananapps.kilometa.presentation.common.components.VehicularIconSelector
import com.ananapps.kilometa.presentation.theme.*

private val DISBURSEMENT_DAYS = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Inmediato")

/**
 * Hoja modal inferior para crear o editar plataformas de transporte (Orden #054 / #055).
 * Modularidad estricta con selectores vehiculares desacoplados (< 200 LOC).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformEditSheet(
    platform: Platform?,
    onDismiss: () -> Unit,
    onSave: (Platform) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember(platform) { mutableStateOf(platform?.name.orEmpty()) }
    var monogram by remember(platform) { mutableStateOf(platform?.monogram.orEmpty()) }
    var iconName by remember(platform) { mutableStateOf(platform?.iconName ?: IconCatalog.DEFAULT_PLATFORM_ICON) }
    var colorHex by remember(platform) { mutableStateOf(platform?.brandColorHex ?: "#1C1C1E") }
    var commission by remember(platform) { mutableDoubleStateOf(platform?.defaultCommissionPct ?: 20.0) }
    var disbursementDay by remember(platform) { mutableStateOf(platform?.disbursementDay ?: "Lunes") }

    fun updateName(newName: String) {
        name = newName
        if (platform == null && monogram.length <= 2) {
            val words = newName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
            monogram = when {
                words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
                newName.length >= 2 -> newName.take(2).uppercase()
                else -> newName.uppercase()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DeepObsidianSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = ObsidianBorder) },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (platform == null) "Nueva Plataforma" else "Editar Plataforma",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Nombre comercial
            OutlinedTextField(
                value = name,
                onValueChange = { updateName(it) },
                label = { Text("Nombre comercial") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBluePrimary,
                    unfocusedBorderColor = ObsidianBorder
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Monograma de 2 letras
            OutlinedTextField(
                value = monogram,
                onValueChange = { if (it.length <= 2) monogram = it.uppercase() },
                label = { Text("Monograma (2 letras)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBluePrimary,
                    unfocusedBorderColor = ObsidianBorder
                )
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Selector de Icono Vehicular
            Text("Icono vehicular", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            VehicularIconSelector(
                selectedIcon = iconName,
                onIconSelected = { iconName = it }
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Selector de Color Vehicular Oficial
            Text("Color corporativo", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            VehicularColorSelector(
                selectedColorHex = colorHex,
                onColorSelected = { colorHex = it }
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Comisión estimada con botones +/-
            Text("Comisión estimada: ${"%.1f".format(commission)}%", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { commission = (commission - 1.0).coerceAtLeast(0.0) },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepObsidianSurfaceVariant)
                ) { Text("-1%", color = TextPrimary) }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { commission = (commission + 1.0).coerceAtMost(50.0) },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepObsidianSurfaceVariant)
                ) { Text("+1%", color = TextPrimary) }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Día de dispersión
            Text("Día habitual de dispersión", color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DISBURSEMENT_DAYS) { day ->
                    val isSelected = disbursementDay == day
                    FilterChip(
                        selected = isSelected,
                        onClick = { disbursementDay = day },
                        label = { Text(day, color = if (isSelected) Color.White else TextSecondary) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBluePrimary,
                            containerColor = DeepObsidianSurfaceVariant
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Botón Guardar
            Button(
                onClick = {
                    val p = platform?.copy(
                        name = name.trim(),
                        monogram = monogram.trim().take(2).uppercase(),
                        iconName = iconName,
                        brandColorHex = colorHex,
                        defaultCommissionPct = commission,
                        disbursementDay = disbursementDay
                    ) ?: Platform(
                        id = name.lowercase().trim().replace("\\s+".toRegex(), "_"),
                        name = name.trim(),
                        monogram = monogram.trim().take(2).uppercase(),
                        iconName = iconName,
                        brandColorHex = colorHex,
                        defaultCommissionPct = commission,
                        disbursementDay = disbursementDay,
                        isCustom = true,
                        isActive = true
                    )
                    onSave(p)
                },
                enabled = name.isNotBlank() && monogram.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
            ) {
                Text("Guardar Plataforma", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
