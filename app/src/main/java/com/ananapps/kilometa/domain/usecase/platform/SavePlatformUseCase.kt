package com.ananapps.kilometa.domain.usecase.platform

import com.ananapps.kilometa.core.util.IconCatalog
import com.ananapps.kilometa.domain.model.Platform
import com.ananapps.kilometa.domain.repository.PlatformRepository
import javax.inject.Inject

/**
 * Caso de uso para crear o actualizar plataformas con validaciones de dominio (Orden #054 / #055).
 */
class SavePlatformUseCase @Inject constructor(
    private val platformRepository: PlatformRepository
) {
    suspend operator fun invoke(platform: Platform): Result<Unit> {
        if (platform.name.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre de la plataforma no puede estar vacío."))
        }
        if (platform.monogram.isBlank()) {
            return Result.failure(IllegalArgumentException("El monograma no puede estar vacío."))
        }
        val normalizedMonogram = platform.monogram.trim().take(2).uppercase()
        val sanitizedIcon = IconCatalog.sanitizeIcon(platform.iconName, default = IconCatalog.DEFAULT_PLATFORM_ICON)
        val normalized = platform.copy(
            id = platform.id.ifBlank { platform.name.lowercase().trim().replace("\\s+".toRegex(), "_") },
            name = platform.name.trim(),
            monogram = normalizedMonogram,
            iconName = sanitizedIcon
        )
        return platformRepository.savePlatform(normalized)
    }
}
