// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/more/HudSettingsScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla minimalista para calibración de Umbrales de Rentabilidad del Conductor (Orden #066).
// Permite ajustar el semáforo ($/km y $/hora) purgando controles y permisos obsoletos (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.more

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ananapps.kilometa.presentation.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HudSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: HudSettingsViewModel = hiltViewModel()
) {
    val config by viewModel.config.collectAsState()
    val context = LocalContext.current
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply { maximumFractionDigits = 0 }
    }

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collect {
            Toast.makeText(context, "Umbrales de rentabilidad actualizados", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Umbrales de Rentabilidad", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            // 1. Card Explicativo Minimalista
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DeepObsidianSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondary.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🚦", fontSize = 20.sp)
                            Text("Semáforo de Rentabilidad Personalizado", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                        }
                        Text(
                            text = "Calibra tus estándares mínimos de ganancia en cabina. KiloMeta evaluará cada carrera registrada comparando su $/km y proyección por hora con estos valores.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 2. Controles Deslizantes de Umbrales
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DeepObsidianSurface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ThresholdSlider(
                            label = "Tarifa Mínima Rentable ($/km)",
                            value = config.minProfitPerKm,
                            range = 1000f..4500f,
                            step = 100f,
                            unit = "COP/km",
                            color = StatusGreen,
                            formatter = currencyFormatter,
                            onValueChange = { viewModel.updateConfig { c -> c.copy(minProfitPerKm = it) } }
                        )

                        ThresholdSlider(
                            label = "Proyección Mínima Rentable ($/hora)",
                            value = config.minProfitPerHour,
                            range = 10000f..50000f,
                            step = 1000f,
                            unit = "COP/h",
                            color = StatusGreen,
                            formatter = currencyFormatter,
                            onValueChange = { viewModel.updateConfig { c -> c.copy(minProfitPerHour = it) } }
                        )

                        ThresholdSlider(
                            label = "Punto de Equilibrio Mínimo ($/km)",
                            value = config.breakEvenPerKm,
                            range = 800f..3000f,
                            step = 100f,
                            unit = "COP/km",
                            color = StatusAmber,
                            formatter = currencyFormatter,
                            onValueChange = { viewModel.updateConfig { c -> c.copy(breakEvenPerKm = it) } }
                        )
                    }
                }
            }

            // 3. Previsualizador del Semáforo en Vivo
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DeepObsidianSurfaceVariant,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("VISTA PREVIA DEL SEMÁFORO", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)

                        PreviewTierRow(
                            emoji = "🟢",
                            name = "RENTABLE",
                            desc = "≥ ${currencyFormatter.format(config.minProfitPerKm)}/km y ≥ ${currencyFormatter.format(config.minProfitPerHour)}/h",
                            color = StatusGreen
                        )
                        PreviewTierRow(
                            emoji = "🟡",
                            name = "EQUILIBRIO",
                            desc = "Entre ${currencyFormatter.format(config.breakEvenPerKm)} y ${currencyFormatter.format(config.minProfitPerKm)}/km",
                            color = StatusAmber
                        )
                        PreviewTierRow(
                            emoji = "🔴",
                            name = "NO RENTABLE",
                            desc = "< ${currencyFormatter.format(config.breakEvenPerKm)}/km (Bajo rendimiento)",
                            color = StatusRed
                        )
                    }
                }
            }

            // 4. Botón Guardar Umbrales
            item {
                Button(
                    onClick = { viewModel.saveConfig() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("💾 GUARDAR UMBRALES", color = DeepObsidianBackground, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ThresholdSlider(
    label: String,
    value: Double,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    unit: String,
    color: Color,
    formatter: NumberFormat,
    onValueChange: (Double) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "${formatter.format(value)} / ${unit.substringAfter('/')}",
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Slider(
            value = value.toFloat().coerceIn(range),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = range,
            steps = (((range.endInclusive - range.start) / step).toInt() - 1).coerceAtLeast(0),
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
        )
    }
}

@Composable
private fun PreviewTierRow(emoji: String, name: String, desc: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 14.sp)
            Text(name, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Text(desc, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}
