// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/domain/usecase/CheckCloudConnectionUseCase.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Caso de uso de Dominio que realiza una comprobación activa de conectividad bidireccional
// con Google Cloud Firestore y evalúa la latencia del enlace en tiempo real.
// Regla arquitectural: Cero dependencias del framework Android en la capa de Dominio.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.ConnectionDiagnostic
import com.ananapps.kilometa.domain.repository.SyncRepository
import javax.inject.Inject

// ------------------------------------------------------------------------------
// BLOQUE 1: CASO DE USO DE DIAGNÓSTICO EN VIVO
// ------------------------------------------------------------------------------
/**
 * Ejecuta una prueba de ping/pong contra los servidores de Firestore para auditar la telemetría de red.
 */
class CheckCloudConnectionUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {

    /**
     * Mide la latencia round-trip del enlace de datos y retorna el informe diagnóstico inmutable.
     *
     * @return [ConnectionDiagnostic] con estado de conexión, latencia en ms y marcas de tiempo.
     */
    suspend operator fun invoke(): ConnectionDiagnostic {
        return syncRepository.checkCloudConnection()
    }
}
