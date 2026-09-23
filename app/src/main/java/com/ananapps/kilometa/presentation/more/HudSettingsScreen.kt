// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/more/HudSettingsScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla completa de calibración de parámetros del Copiloto Flotante (Orden #047).
// Permite ajustar umbrales del semáforo, distancia máxima de recogida, combustible y comisiones.
// ==============================================================================

package com.ananapps.kilometa.presentation.more

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ananapps.kilometa.presentation.more.components.HudPermissionsCard
import com.ananapps.kilometa.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HudSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: HudSettingsViewModel = hiltViewModel()
) {
    val config by viewModel.config.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collect {
            Toast.makeText(context, "Parámetros del Copiloto guardados", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración del Copiloto", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepObsidianSurface)
            )
        },
        containerColor = DeepObsidianBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sección 0: Estado de Permisos del Copiloto Flotante
            item {
                HudPermissionsCard(
                    onSimulateOffer = { viewModel.simulateTestOffer() }
                )
            }

            // Sección 1: Umbrales del Semáforo
            item {
                SectionCard(title = "🚦 Semáforo de Rentabilidad") {
                    NumberSliderSetting(
                        label = "Mínimo Verde Rentable ($/km)",
                        value = config.minProfitPerKm,
                        range = 1500f..4500f,
                        step = 100f,
                        unit = "COP/km",
                        onValueChange = { viewModel.updateConfig { c -> c.copy(minProfitPerKm = it) } }
                    )
                    NumberSliderSetting(
                        label = "Mínimo Verde Rentable ($/hora)",
                        value = config.minProfitPerHour,
                        range = 15000f..50000f,
                        step = 1000f,
                        unit = "COP/h",
                        onValueChange = { viewModel.updateConfig { c -> c.copy(minProfitPerHour = it) } }
                    )
                    NumberSliderSetting(
                        label = "Umbral Amarillo Punto de Equilibrio ($/km)",
                        value = config.breakEvenPerKm,
                        range = 1000f..2500f,
                        step = 100f,
                        unit = "COP/km",
                        onValueChange = { viewModel.updateConfig { c -> c.copy(breakEvenPerKm = it) } }
                    )
                }
            }

            // Sección 2: Recogida Máxima
            item {
                SectionCard(title = "📍 Filtro de Recogida") {
                    NumberSliderSetting(
                        label = "Distancia Máxima al Pasajero",
                        value = config.maxPickupDistanceKm,
                        range = 1.0f..6.0f,
                        step = 0.5f,
                        unit = "km",
                        onValueChange = { viewModel.updateConfig { c -> c.copy(maxPickupDistanceKm = it) } }
                    )
                    Text(
                        text = "Carreras con recogida superior se marcarán automáticamente como NO RENTABLES (Rojo).",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Sección 3: Costo de Combustible
            item {
                SectionCard(title = "⛽ Costo de Combustible Aplicado") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Usar costo del vehículo activo", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Calculado según GNV vs Gasolina en perfil", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = config.useAutoFuelCost,
                            onCheckedChange = { viewModel.updateConfig { c -> c.copy(useAutoFuelCost = it) } }
                        )
                    }
                    if (!config.useAutoFuelCost) {
                        NumberSliderSetting(
                            label = "Costo Fijo Manual por Kilómetro",
                            value = config.customFuelCostPerKm,
                            range = 150f..900f,
                            step = 25f,
                            unit = "COP/km",
                            onValueChange = { viewModel.updateConfig { c -> c.copy(customFuelCostPerKm = it) } }
                        )
                    }
                }
            }

            // Sección 4: Comportamiento y Permisos
            item {
                SectionCard(title = "⚙️ Comportamiento de la Burbuja") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-abrir al iniciar turno", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Aparece automáticamente al arrancar jornada", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = config.autoStartWithShift,
                            onCheckedChange = { viewModel.updateConfig { c -> c.copy(autoStartWithShift = it) } }
                        )
                    }
                }
            }

            // Botón Guardar Cambios
            item {
                Button(
                    onClick = { viewModel.saveConfig() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar Parámetros", color = DeepObsidianBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DeepObsidianSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun NumberSliderSetting(
    label: String,
    value: Double,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    unit: String,
    onValueChange: (Double) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "${value.toInt()} $unit", color = StatusGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = range,
            steps = ((range.endInclusive - range.start) / step).toInt() - 1,
            colors = SliderDefaults.colors(thumbColor = StatusGreen, activeTrackColor = StatusGreen)
        )
    }
}
