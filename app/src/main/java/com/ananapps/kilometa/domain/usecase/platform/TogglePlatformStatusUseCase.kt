package com.ananapps.kilometa.domain.usecase.platform

import com.ananapps.kilometa.domain.repository.PlatformRepository
import javax.inject.Inject

/**
 * Caso de uso para conmutar el estado lógico activo/inactivo de una plataforma (Orden #054).
 */
class TogglePlatformStatusUseCase @Inject constructor(
    private val platformRepository: PlatformRepository
) {
    suspend operator fun invoke(id: String, isActive: Boolean): Result<Unit> {
        return platformRepository.togglePlatformStatus(id, isActive)
    }
}
