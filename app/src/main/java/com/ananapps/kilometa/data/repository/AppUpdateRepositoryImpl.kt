// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/data/repository/AppUpdateRepositoryImpl.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Implementación del repositorio de actualización remota consultando Firestore
// de forma segura y tolerante a fallos para el sistema In-App OTA.
// ==============================================================================

package com.ananapps.kilometa.data.repository

import com.ananapps.kilometa.domain.model.AppUpdateInfo
import com.ananapps.kilometa.domain.repository.AppUpdateRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AppUpdateRepository {

    override suspend fun checkForUpdate(currentVersionCode: Long): Result<AppUpdateInfo> {
        return try {
            // Timeout de 5 segundos para nunca bloquear el arranque si hay mala señal
            val doc = withTimeoutOrNull(5000L) {
                firestore.collection("app_metadata")
                    .document("kilo_meta")
                    .get()
                    .await()
            }

            if (doc != null && doc.exists()) {
                val remoteVersionCode = doc.getLong("latest_version_code") ?: 0L
                val versionName = doc.getString("version_name") ?: ""
                val apkUrl = doc.getString("apk_url") ?: ""
                val releaseNotesRaw = doc.get("release_notes") as? List<*>
                val releaseNotes = releaseNotesRaw?.mapNotNull { it?.toString() } ?: emptyList()
                val isMandatory = doc.getBoolean("is_mandatory") ?: false

                val isUpdateAvailable = remoteVersionCode > currentVersionCode && apkUrl.isNotBlank()

                Result.success(
                    AppUpdateInfo(
                        remoteVersionCode = remoteVersionCode,
                        versionName = versionName,
                        apkUrl = apkUrl,
                        releaseNotes = releaseNotes,
                        isMandatory = isMandatory,
                        isUpdateAvailable = isUpdateAvailable
                    )
                )
            } else {
                // Fallback silencioso si no existe el documento o no hay internet
                Result.success(AppUpdateInfo(isUpdateAvailable = false))
            }
        } catch (e: Exception) {
            // Falla tolerante sin crashear
            Result.failure(e)
        }
    }
}
