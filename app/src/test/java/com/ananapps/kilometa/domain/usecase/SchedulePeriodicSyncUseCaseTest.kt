// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/domain/usecase/SchedulePeriodicSyncUseCaseTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias para la orquestación del agendamiento y disparo
// de sincronización en segundo plano mediante casos de uso de dominio.
// Regla de Oro: Tipado estricto, 100% inmutable, sin dependencias de Android framework.
// ==============================================================================

package com.ananapps.kilometa.domain.usecase

import com.ananapps.kilometa.domain.model.SyncPreferences
import com.ananapps.kilometa.domain.repository.SyncScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// ------------------------------------------------------------------------------
// BLOQUE 1: IMPLEMENTACIÓN FAKE DE SYNCSCHEDULER PARA PRUEBAS
// ------------------------------------------------------------------------------
class FakeSyncScheduler : SyncScheduler {
    var scheduledPreferences: SyncPreferences? = null
    var isCancelled: Boolean = false
    var immediateSyncTriggerCount: Int = 0

    override fun schedulePeriodicSync(preferences: SyncPreferences) {
        scheduledPreferences = preferences
        isCancelled = false
    }

    override fun triggerImmediateSync() {
        immediateSyncTriggerCount++
    }

    override fun cancelPeriodicSync() {
        isCancelled = true
        scheduledPreferences = null
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: SUITE DE PRUEBAS UNITARIAS
// ------------------------------------------------------------------------------
class SchedulePeriodicSyncUseCaseTest {

    private lateinit var fakeScheduler: FakeSyncScheduler
    private lateinit var schedulePeriodicSyncUseCase: SchedulePeriodicSyncUseCase
    private lateinit var triggerImmediateSyncUseCase: TriggerImmediateSyncUseCase

    @Before
    fun setUp() {
        fakeScheduler = FakeSyncScheduler()
        schedulePeriodicSyncUseCase = SchedulePeriodicSyncUseCase(fakeScheduler)
        triggerImmediateSyncUseCase = TriggerImmediateSyncUseCase(fakeScheduler)
    }

    @Test
    fun `invoke con auto-sync habilitado agenda sincronizacion periodica con preferencias recibidas`() {
        // GIVEN: Preferencias híbridas con auto-sync activado
        val preferences = SyncPreferences(
            isAutoSyncEnabled = true,
            requireWifiOnly = false,
            syncIntervalMinutes = 15L
        )

        // WHEN: Se ejecuta el caso de uso
        schedulePeriodicSyncUseCase(preferences)

        // THEN: Se agenda la sincronización con los parámetros exactos
        assertEquals(preferences, fakeScheduler.scheduledPreferences)
        assertFalse(fakeScheduler.isCancelled)
    }

    @Test
    fun `invoke con auto-sync deshabilitado cancela cualquier sincronizacion periodica activa`() {
        // GIVEN: Sincronización previamente activa
        fakeScheduler.schedulePeriodicSync(SyncPreferences(isAutoSyncEnabled = true))

        val disabledPreferences = SyncPreferences(
            isAutoSyncEnabled = false,
            requireWifiOnly = false,
            syncIntervalMinutes = 15L
        )

        // WHEN: Se ejecuta con auto-sync apagado
        schedulePeriodicSyncUseCase(disabledPreferences)

        // THEN: La sincronización periódica queda cancelada
        assertTrue(fakeScheduler.isCancelled)
        assertEquals(null, fakeScheduler.scheduledPreferences)
    }

    @Test
    fun `invoke con politica wifi-only preserva parametro de red unmetered`() {
        // GIVEN: Preferencia estricta de solo Wi-Fi
        val wifiOnlyPreferences = SyncPreferences(
            isAutoSyncEnabled = true,
            requireWifiOnly = true,
            syncIntervalMinutes = 30L
        )

        // WHEN: Se agenda
        schedulePeriodicSyncUseCase(wifiOnlyPreferences)

        // THEN: Las preferencias registradas reflejan requireWifiOnly = true
        assertEquals(true, fakeScheduler.scheduledPreferences?.requireWifiOnly)
        assertEquals(30L, fakeScheduler.scheduledPreferences?.syncIntervalMinutes)
    }

    @Test
    fun `triggerImmediateSyncUseCase invoca inmediatamente el programador de tareas`() {
        // GIVEN: Contador inicial en 0
        assertEquals(0, fakeScheduler.immediateSyncTriggerCount)

        // WHEN: Se dispara la sincronización expedita
        triggerImmediateSyncUseCase()
        triggerImmediateSyncUseCase()

        // THEN: Se delega la petición dos veces
        assertEquals(2, fakeScheduler.immediateSyncTriggerCount)
    }
}
