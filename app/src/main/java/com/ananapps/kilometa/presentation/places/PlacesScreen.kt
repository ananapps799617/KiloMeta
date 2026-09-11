// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/places/PlacesScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla de "Mis Bombas y Talleres" (Lugares Frecuentes).
// Permite al conductor listar, filtrar, registrar y lanzar navegación a Waze y Google Maps.
// ==============================================================================

package com.ananapps.kilometa.presentation.places

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ananapps.kilometa.domain.model.PlaceCategory
import com.ananapps.kilometa.presentation.places.components.AddPlaceDialog
import com.ananapps.kilometa.presentation.places.components.PlaceItemCard
import com.ananapps.kilometa.presentation.places.components.PlacesOverviewMapView
import com.ananapps.kilometa.presentation.places.WazeMapLauncher
import com.ananapps.kilometa.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlacesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val places by viewModel.filteredPlaces.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var isMapView by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepObsidianBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Bombas & Talleres",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isMapView = !isMapView }) {
                        Icon(
                            imageVector = if (isMapView) Icons.Default.List else Icons.Default.Place,
                            contentDescription = if (isMapView) "Ver Lista" else "Ver en Mapa",
                            tint = ElectricBluePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepObsidianBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ElectricBluePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar lugar"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtros de categoría horizontales
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isAllSelected = selectedCategory == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isAllSelected) ElectricBluePrimary else DeepObsidianSurface)
                        .clickable { viewModel.selectCategory(null) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Todos",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isAllSelected) Color.White else TextSecondary
                    )
                }

                PlaceCategory.values().forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ElectricBluePrimary else DeepObsidianSurface)
                            .clickable { viewModel.selectCategory(cat) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${cat.icon} ${cat.displayName}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }

            if (isMapView) {
                PlacesOverviewMapView(
                    places = places,
                    onNavigateWaze = { place ->
                        WazeMapLauncher.openWaze(
                            context = context,
                            address = place.address,
                            latitude = place.latitude,
                            longitude = place.longitude
                        )
                    },
                    onNavigateMaps = { place ->
                        WazeMapLauncher.openGoogleMaps(
                            context = context,
                            address = place.address,
                            latitude = place.latitude,
                            longitude = place.longitude
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (places.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = ElectricBluePrimary.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Sin lugares registrados",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Guarda tus estaciones de GNV, gasolineras y talleres frecuentes para abrir la ruta en Waze o Maps con un solo toque.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 0.dp)
                ) {
                    items(places, key = { it.id }) { place ->
                        PlaceItemCard(
                            place = place,
                            onNavigateWaze = {
                                WazeMapLauncher.openWaze(
                                    context = context,
                                    address = place.address,
                                    latitude = place.latitude,
                                    longitude = place.longitude
                                )
                            },
                            onNavigateMaps = {
                                WazeMapLauncher.openGoogleMaps(
                                    context = context,
                                    address = place.address,
                                    latitude = place.latitude,
                                    longitude = place.longitude
                                )
                            },
                            onDelete = {
                                viewModel.deletePlace(place.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPlaceDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, category, address, notes, lat, lng ->
                viewModel.savePlace(name, category, address, notes, lat, lng)
                showAddDialog = false
            }
        )
    }
}
