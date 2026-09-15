// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/more/MoreScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla de configuración secundaria (Pestaña "Más") inspirada en KiloMeta.
// Reemplaza por completo el menú hamburguesa, centralizando Perfil, Vehículo,
// Mantenimientos, Gastos, Sincronización y Sesión en una lista táctil ergonómica.
// ==============================================================================

package com.ananapps.kilometa.presentation.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ananapps.kilometa.BuildConfig
import com.ananapps.kilometa.presentation.common.AppUpdateBottomSheet
import com.ananapps.kilometa.presentation.theme.*

// ------------------------------------------------------------------------------
// BLOQUE 1: PANTALLA PRINCIPAL DE MÁS OPCIONES
// ------------------------------------------------------------------------------
@Composable
fun MoreScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToPlaces: () -> Unit,
    onNavigateToMaintenance: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToSync: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MoreViewModel = hiltViewModel()
) {
    val updateState by viewModel.updateState.collectAsState()
    var showUpToDateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(updateState) {
        if (updateState is UpdateCheckUiState.UpToDate) {
            showUpToDateDialog = true
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Más opciones",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
            )
        }

        // Sección 1: Configuración Vehicular y Personal
        item {
            MoreOptionItem(
                icon = Icons.Default.Person,
                title = "Mi Perfil & Vehículo",
                subtitle = "Nombre, placa, cuota y horario de corte",
                onClick = onNavigateToProfile
            )
        }

        item {
            MoreOptionItem(
                icon = Icons.Default.Place,
                title = "Mis Bombas & Talleres",
                subtitle = "Gasolineras, talleres y enlace a Waze / Maps",
                onClick = onNavigateToPlaces
            )
        }

        item {
            MoreOptionItem(
                icon = Icons.Default.Notifications,
                title = "🔔 Alertas & Recordatorios",
                subtitle = "Semáforo de cuota semanal y alertas de odómetro",
                onClick = onNavigateToMaintenance
            )
        }

        item {
            MoreOptionItem(
                icon = Icons.Default.ShoppingCart,
                title = "Gastos en Ruta",
                subtitle = "Histórico de combustible, peajes y alimentación",
                onClick = onNavigateToExpenses
            )
        }

        item {
            MoreOptionItem(
                icon = Icons.Default.Refresh,
                title = "Sincronización en la Nube",
                subtitle = "Estado de respaldo en Firebase Firestore",
                onClick = onNavigateToSync
            )
        }

        item {
            MoreOptionItem(
                icon = Icons.Default.Info,
                title = "Actualización de Software",
                subtitle = when (updateState) {
                    is UpdateCheckUiState.Checking -> "Buscando nueva versión OTA..."
                    is UpdateCheckUiState.UpToDate -> "KiloMeta v${BuildConfig.VERSION_NAME} · Al día"
                    is UpdateCheckUiState.Available -> "¡Nueva versión disponible! Toca para actualizar"
                    is UpdateCheckUiState.Error -> "v${BuildConfig.VERSION_NAME} · Error al buscar, reintentar"
                    else -> "Versión ${BuildConfig.VERSION_NAME} · Buscar actualizaciones OTA"
                },
                tint = when (updateState) {
                    is UpdateCheckUiState.Available -> StatusGreen
                    is UpdateCheckUiState.Error -> StatusAmber
                    else -> ElectricBluePrimary
                },
                onClick = { viewModel.checkForUpdates() }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = TextSecondary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Sección 2: Cierre de Sesión Seguro
        item {
            MoreOptionItem(
                icon = Icons.Default.ExitToApp,
                title = "Cerrar Sesión de Cabina",
                subtitle = "Purga segura de datos locales para cambio de cuenta",
                tint = StatusRed,
                onClick = onSignOut
            )
        }
    }

    if (updateState is UpdateCheckUiState.Available) {
        AppUpdateBottomSheet(
            updateInfo = (updateState as UpdateCheckUiState.Available).info,
            onDismiss = { viewModel.dismissUpdate() }
        )
    }

    if (showUpToDateDialog) {
        AlertDialog(
            onDismissRequest = {
                showUpToDateDialog = false
                viewModel.dismissUpdate()
            },
            confirmButton = {
                TextButton(onClick = {
                    showUpToDateDialog = false
                    viewModel.dismissUpdate()
                }) {
                    Text("Entendido", color = ElectricBluePrimary)
                }
            },
            title = {
                Text("KiloMeta al día", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Tienes instalada la versión más reciente (v${BuildConfig.VERSION_NAME}).")
            },
            containerColor = DeepObsidianSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: COMPONENTE DE FILA DE OPCIÓN
// ------------------------------------------------------------------------------
@Composable
private fun MoreOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = ElectricBluePrimary,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = DeepObsidianSurface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (tint == StatusRed) StatusRed else TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
