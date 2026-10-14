package com.ananapps.kilometa.presentation.platform.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ananapps.kilometa.presentation.platform.PlatformCatalogViewModel
import com.ananapps.kilometa.presentation.theme.*

/**
 * Diálogo modal para la gestión del catálogo de plataformas (Orden #054).
 * Regla inquebrantable: Modularidad estricta < 280 líneas de código.
 */
@Composable
fun PlatformCatalogDialog(
    viewModel: PlatformCatalogViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val platforms by viewModel.platforms.collectAsStateWithLifecycle()
    val isSheetOpen by viewModel.isSheetOpen.collectAsStateWithLifecycle()
    val editingPlatform by viewModel.editingPlatform.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = DeepObsidianBackground
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    // Cabecera con botón de cerrar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Catálogo de Plataformas",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Configura comisiones y plataformas activas",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                        }
                    }

                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = StatusRedContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = error, color = StatusRed, fontSize = 12.sp)
                                TextButton(onClick = { viewModel.clearError() }) {
                                    Text("OK", color = StatusRed, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Listado de plataformas
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(platforms, key = { it.id }) { platform ->
                            PlatformItemRow(
                                platform = platform,
                                onToggleActive = { viewModel.togglePlatformActive(platform.id, it) },
                                onClick = { viewModel.openEditPlatform(platform) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }

                // Botón inferior para agregar nueva plataforma
                Button(
                    onClick = { viewModel.openCreatePlatform() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nueva Plataforma",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (isSheetOpen) {
        PlatformEditSheet(
            platform = editingPlatform,
            onDismiss = { viewModel.closeSheet() },
            onSave = { viewModel.savePlatform(it) }
        )
    }
}
