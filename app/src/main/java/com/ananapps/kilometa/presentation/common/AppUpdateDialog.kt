// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/common/AppUpdateDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante centrado de 3 capas para la actualización OTA en cabina.
// Erradica el BottomSheet, garantizando cabecera fija, changelog desplazable
// y botonera de acción (56dp) permanentemente anclada sin riesgo de corte.
// ==============================================================================

package com.ananapps.kilometa.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ananapps.kilometa.domain.model.AppUpdateInfo
import com.ananapps.kilometa.presentation.theme.*
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    var downloadedApkFile by remember { mutableStateOf<File?>(null) }
    var hasInstallPermission by remember { mutableStateOf(ApkInstallerHelper.canInstallApk(context)) }

    Dialog(
        onDismissRequest = {
            if (!updateInfo.isMandatory && downloadState !is DownloadState.Downloading) onDismiss()
        },
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
                    .padding(20.dp)
            ) {
                // --------------------------------------------------------------
                // CAPA 1: CABECERA FIJA (Título + Versión + Botón Salida)
                // --------------------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🚀 ¡KILOMETA ACTUALIZACIÓN!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = ElectricBluePrimary
                        )
                        Text(
                            text = "Versión  disponible",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    if (!updateInfo.isMandatory && downloadState !is DownloadState.Downloading) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --------------------------------------------------------------
                // CAPA 2: CUERPO DESPLAZABLE INTERNO (Changelog & Avisos)
                // --------------------------------------------------------------
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (updateInfo.releaseNotes.isNotEmpty()) {
                        Text(
                            text = "Novedades de la versión:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DeepObsidianSurfaceVariant)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            updateInfo.releaseNotes.forEach { note ->
                                Text(
                                    text = "• ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    if (downloadState is DownloadState.Error) {
                        Text(
                            text = (downloadState as DownloadState.Error).message,
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --------------------------------------------------------------
                // CAPA 3: BOTONERA DE ACCIÓN FIJA (Pie Inamovible 56dp)
                // --------------------------------------------------------------
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Barra de progreso reactiva si está descargando
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
                                    text = "%",
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

                    when {
                        downloadedApkFile != null -> {
                            if (!hasInstallPermission) {
                                Button(
                                    onClick = {
                                        ApkInstallerHelper.requestInstallPermission(context)
                                        hasInstallPermission = ApkInstallerHelper.canInstallApk(context)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusAmber)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Autorizar Instalador", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        downloadedApkFile?.let { ApkInstallerHelper.launchApkInstaller(context, it) }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Instalar Actualización Ahora", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        downloadState is DownloadState.Downloading -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Descargando...", fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
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
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Descargar e Instalar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }

                    if (!updateInfo.isMandatory && downloadState !is DownloadState.Downloading) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Recordar más tarde", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
