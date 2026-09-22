// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/repository/HudConfigRepository.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Contrato de dominio para persistir y emitir reactivamente los parámetros de cabina (Orden #047).
// ==============================================================================

package com.ananapps.kilometa.domain.repository

import com.ananapps.kilometa.domain.model.HudConfig
import kotlinx.coroutines.flow.Flow

interface HudConfigRepository {
    fun getHudConfig(): Flow<HudConfig>
    suspend fun saveHudConfig(config: HudConfig)
}
