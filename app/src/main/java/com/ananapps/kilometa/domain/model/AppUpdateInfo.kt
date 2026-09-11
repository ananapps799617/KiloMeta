// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/model/AppUpdateInfo.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Modelo inmutable para el sistema de Auto-Actualización In-App (OTA).
// Transporta la información de versión remota, notas de la versión y URL de descarga.
// ==============================================================================

package com.ananapps.kilometa.domain.model

data class AppUpdateInfo(
    val remoteVersionCode: Long = 0L,
    val versionName: String = "",
    val apkUrl: String = "",
    val releaseNotes: List<String> = emptyList(),
    val isMandatory: Boolean = false,
    val isUpdateAvailable: Boolean = false
)
