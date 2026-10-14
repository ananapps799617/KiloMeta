package com.ananapps.kilometa.domain.usecase.platform

import com.ananapps.kilometa.domain.model.Platform
import com.ananapps.kilometa.domain.repository.PlatformRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso reactivo para observar plataformas activas en la cabina vehicular (Orden #054).
 */
class GetActivePlatformsUseCase @Inject constructor(
    private val platformRepository: PlatformRepository
) {
    operator fun invoke(): Flow<List<Platform>> = platformRepository.observeActivePlatforms()
}
