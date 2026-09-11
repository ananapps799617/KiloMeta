// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/common/ApkInstallerHelper.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Helper seguro para la descarga en streaming de actualizaciones OTA y despacho
// del Intent nativo de instalación mediante FileProvider y PackageInstaller de Android.
// ==============================================================================

package com.ananapps.kilometa.presentation.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Float, val downloadedBytes: Long, val totalBytes: Long) : DownloadState()
    data class Success(val apkFile: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

object ApkInstallerHelper {

    fun downloadApk(context: Context, apkUrl: String): Flow<DownloadState> = flow {
        emit(DownloadState.Downloading(0f, 0L, 0L))

        try {
            val updateDir = File(context.cacheDir, "apk_updates").apply { mkdirs() }
            val tempFile = File(updateDir, "update_temp.apk")
            val targetFile = File(updateDir, "update.apk")

            val url = URL(apkUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = true
            }

            val totalBytes = connection.contentLength.toLong()
            var downloadedBytes = 0L

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0f
                        emit(DownloadState.Downloading(progress, downloadedBytes, totalBytes))
                    }
                }
            }

            // Renombrado atómico
            if (targetFile.exists()) targetFile.delete()
            tempFile.renameTo(targetFile)

            emit(DownloadState.Success(targetFile))
        } catch (e: Exception) {
            emit(DownloadState.Error(e.localizedMessage ?: "Error en la descarga de actualización"))
        }
    }.flowOn(Dispatchers.IO)

    fun canInstallApk(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun requestInstallPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun launchApkInstaller(context: Context, apkFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(intent)
    }
}
