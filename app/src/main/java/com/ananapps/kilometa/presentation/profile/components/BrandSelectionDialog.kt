// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/BrandSelectionDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante de selección de marca vehicular (Benchmark Drivvo).
// Ofrece buscador rápido por texto, listado alfabético y opción de ingreso manual.
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ananapps.kilometa.domain.model.VehicleCatalog
import com.ananapps.kilometa.domain.model.VehicleType
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun BrandSelectionDialog(
    vehicleType: VehicleType,
    selectedBrand: String,
    onBrandSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isCustomMode by remember { mutableStateOf(false) }
    var customBrandText by remember { mutableStateOf("") }

    val filteredBrands = remember(searchQuery, vehicleType) {
        VehicleCatalog.searchBrands(searchQuery, vehicleType)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .border(1.dp, DeepObsidianBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = DeepObsidianSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Seleccionar Marca",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "${vehicleType.icon} ${vehicleType.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricBluePrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isCustomMode) {
                    // Modo ingreso manual
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Ingresa la marca del vehículo:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        OutlinedTextField(
                            value = customBrandText,
                            onValueChange = { customBrandText = it },
                            label = { Text("Nombre de la Marca") },
                            placeholder = { Text("Ej: Geely, Great Wall, Bajaj...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                val trimmed = customBrandText.trim()
                                if (trimmed.isNotBlank()) {
                                    onBrandSelected(trimmed)
                                    onDismiss()
                                }
                            },
                            enabled = customBrandText.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                        ) {
                            Text("Confirmar Marca", fontWeight = FontWeight.Bold, color = DeepObsidianBackground)
                        }
                        TextButton(
                            onClick = { isCustomMode = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("← Volver a la lista de marcas", color = TextSecondary)
                        }
                    }
                } else {
                    // Campo de búsqueda rápida
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = { Text("🔍", modifier = Modifier.padding(start = 8.dp)) },
                        placeholder = { Text("Buscar marca (ej: BYD, Chevrolet...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Lista alfabética de marcas
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredBrands) { bName ->
                            val isSelected = bName.equals(selectedBrand, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onBrandSelected(bName)
                                        onDismiss()
                                    },
                                color = if (isSelected) ElectricBluePrimary.copy(alpha = 0.15f) else DeepObsidianSurfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ElectricBluePrimary) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) ElectricBluePrimary else DeepObsidianBackground),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = bName.take(2).uppercase(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) DeepObsidianBackground else TextPrimary
                                        )
                                    }
                                    Text(
                                        text = bName,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                        color = if (isSelected) ElectricBluePrimary else TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Text("✓", color = ElectricBluePrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            // Opción de contingencia
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { isCustomMode = true },
                                color = DeepObsidianBackground,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DeepObsidianBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("➕", fontSize = 16.sp)
                                    Text(
                                        text = "Otra marca / Escribir manualmente",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = StatusAmber
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
