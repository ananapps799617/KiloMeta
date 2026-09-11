// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/CheckAppUpdateUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso para verificar de forma segura si existe una versión superior en la nube.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.AppUpdateInfo
import com.ananapps.kilometa.domain.repository.AppUpdateRepository
import javax.inject.Inject

class CheckAppUpdateUseCase @Inject constructor(
    private val appUpdateRepository: AppUpdateRepository
) {
    suspend operator fun invoke(currentVersionCode: Long): Result<AppUpdateInfo> {
        return appUpdateRepository.checkForUpdate(currentVersionCode)
    }
}
