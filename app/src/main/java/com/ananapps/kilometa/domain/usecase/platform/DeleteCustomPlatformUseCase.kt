package com.ananapps.kilometa.domain.usecase.platform

import com.ananapps.kilometa.domain.repository.PlatformRepository
import javax.inject.Inject

/**
 * Caso de uso para eliminar de forma segura plataformas personalizadas creadas por el usuario (Orden #056).
 * Protege de forma inmutable las plataformas nativas del sistema.
 */
class DeleteCustomPlatformUseCase @Inject constructor(
    private val platformRepository: PlatformRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = runCatching {
        require(id.isNotBlank()) { "El identificador de plataforma no puede estar vacío." }
        if (id in listOf("uber", "didi", "indrive", "particular")) {
            throw SecurityException("Violación de integridad: Las plataformas nativas son inmutables.")
        }
        platformRepository.deleteCustomPlatform(id).getOrThrow()
    }
}
