// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/GetLastKnownOdometerUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso que actúa como Única Fuente de la Verdad (SSOT) para el odómetro.
// Consulta Room SQLite para obtener el mayor kilometraje registrado entre turnos,
// tanqueos y mantenimientos. Erradica cualquier valor hardcodeado o inventado.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.repository.ShiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE ODÓMETRO INTELIGENTE
// ------------------------------------------------------------------------------
class GetLastKnownOdometerUseCase @Inject constructor(
    private val shiftRepository: ShiftRepository
) {

    /**
     * Emite de forma reactiva el último odómetro conocido registrado en el sistema.
     * Si la base de datos está totalmente vacía, retorna 0 (nunca un valor inventado).
     */
    operator fun invoke(): Flow<Int> {
        return shiftRepository.getLastKnownOdometer().map { lastOdo ->
            lastOdo ?: 0
        }
    }

    /**
     * Obtiene de forma síncrona/suspendida el último odómetro conocido.
     */
    suspend fun getSync(): Int {
        return invoke().first()
    }
}
