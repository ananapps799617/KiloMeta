// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/ModelSelectionDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante para selección de modelo vehicular de la marca activa
// o ingreso libre de cualquier referencia o versión.
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ananapps.kilometa.domain.model.VehicleCatalog
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun ModelSelectionDialog(
    brand: String,
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var customModelText by remember { mutableStateOf("") }
    val catalogModels = remember(brand) { VehicleCatalog.getModelsForBrand(brand) }

    val filteredModels = remember(searchQuery, catalogModels) {
        if (searchQuery.isBlank()) catalogModels
        else catalogModels.filter { it.contains(searchQuery.trim(), ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.80f)
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
                            text = "Modelo / Línea",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Marca: ${brand.ifBlank { "Personalizada" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricBluePrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campo de búsqueda o ingreso manual rápido
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Text("🔍", modifier = Modifier.padding(start = 8.dp)) },
                    placeholder = { Text("Buscar modelo o escribir nuevo...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredModels) { mName ->
                        val isSelected = mName.equals(selectedModel, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onModelSelected(mName)
                                    onDismiss()
                                },
                            color = if (isSelected) ElectricBluePrimary.copy(alpha = 0.15f) else DeepObsidianSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ElectricBluePrimary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = mName,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                    color = if (isSelected) ElectricBluePrimary else TextPrimary
                                )
                                if (isSelected) {
                                    Text("✓", color = ElectricBluePrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Si el usuario escribe algo que no está en la lista exacta, permitir usarlo como modelo
                    if (searchQuery.isNotBlank() && filteredModels.none { it.equals(searchQuery.trim(), ignoreCase = true) }) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onModelSelected(searchQuery.trim())
                                        onDismiss()
                                    },
                                color = DeepObsidianBackground,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBluePrimary)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("➕")
                                    Text(
                                        text = "Usar: \"${searchQuery.trim()}\"",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ElectricBluePrimary
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
