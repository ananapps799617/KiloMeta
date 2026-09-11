// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/common/AppUpdateBottomSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet ergonómico en Deep Obsidian para la actualización OTA en cabina.
// Muestra versión, changelog, descarga reactiva en streaming y despacho de instalación.
// ==============================================================================

package com.ananapps.kilometa.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
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
import com.ananapps.kilometa.domain.model.AppUpdateInfo
import com.ananapps.kilometa.presentation.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateBottomSheet(
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    var downloadedApkFile by remember { mutableStateOf<File?>(null) }
    var hasInstallPermission by remember { mutableStateOf(ApkInstallerHelper.canInstallApk(context)) }

    ModalBottomSheet(
        onDismissRequest = { if (!updateInfo.isMandatory && downloadState !is DownloadState.Downloading) onDismiss() },
        containerColor = DeepObsidianSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera con versión y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🚀 ¡KILOMETA ACTUALIZACIÓN!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = ElectricBluePrimary
                    )
                    Text(
                        text = "Nueva versión ${updateInfo.versionName} disponible",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricBluePrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "OTA DIRECTA",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ElectricBluePrimary
                    )
                }
            }

            // Lista de Novedades (Changelog)
            if (updateInfo.releaseNotes.isNotEmpty()) {
                Text(
                    text = "Novedades de la versión:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepObsidianSurfaceVariant)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    updateInfo.releaseNotes.forEach { note ->
                        Text(
                            text = "• $note",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Progreso de Descarga en Streaming
            AnimatedVisibility(visible = downloadState is DownloadState.Downloading) {
                val state = downloadState as? DownloadState.Downloading
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Descargando actualización...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        val pct = ((state?.progress ?: 0f) * 100).toInt()
                        Text(
                            text = "$pct%",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricBluePrimary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { state?.progress ?: 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ElectricBluePrimary,
                        trackColor = DeepObsidianSurfaceVariant
                    )
                }
            }

            // Error de descarga si ocurrió
            if (downloadState is DownloadState.Error) {
                Text(
                    text = (downloadState as DownloadState.Error).message,
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusRed
                )
            }

            // Botón Principal de Acción (56dp ergonómico)
            when {
                downloadedApkFile != null -> {
                    // Estado: Listo para instalar
                    if (!hasInstallPermission) {
                        Button(
                            onClick = {
                                ApkInstallerHelper.requestInstallPermission(context)
                                hasInstallPermission = ApkInstallerHelper.canInstallApk(context)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusAmber)
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Autorizar Instalador de KiloMeta", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                downloadedApkFile?.let { ApkInstallerHelper.launchApkInstaller(context, it) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Instalar Actualización Ahora", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                downloadState is DownloadState.Downloading -> {
                    // En progreso: botón deshabilitado
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Descargando...", fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    // Estado Inicial: Botón Actualizar Ahora
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                ApkInstallerHelper.downloadApk(context, updateInfo.apkUrl).collect { state ->
                                    downloadState = state
                                    if (state is DownloadState.Success) {
                                        downloadedApkFile = state.apkFile
                                        hasInstallPermission = ApkInstallerHelper.canInstallApk(context)
                                        if (hasInstallPermission) {
                                            ApkInstallerHelper.launchApkInstaller(context, state.apkFile)
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Actualizar Ahora", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Botón Secundario de Descarte
            if (!updateInfo.isMandatory && downloadState !is DownloadState.Downloading) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Recordar más tarde", color = TextSecondary)
                }
            }
        }
    }
}
