// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/AppUpdateRepository.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contrato para verificar la disponibilidad de nuevas versiones en la nube.
// ==============================================================================

package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.AppUpdateInfo

interface AppUpdateRepository {
    suspend fun checkForUpdate(currentVersionCode: Long): Result<AppUpdateInfo>
}
