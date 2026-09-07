// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/test/java/com/ananapps/kilometa/ArchitectureTest.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Suite de pruebas unitarias arquitecturales y de cordura (Smoke Tests) para la Orden #002.
// Valida la integridad inmutable de constantes de cabina, modelos sellados del semáforo
// y la presencia de rótulos de cero mentiras técnicas.
// ==============================================================================

package com.ananapps.kilometa

import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.data.repository.RepositoryPlaceholder
import com.ananapps.kilometa.domain.model.FinancialStatus
import com.ananapps.kilometa.presentation.home.HomeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchitectureTest {

    // --------------------------------------------------------------------------
    // BLOQUE 1: VALIDACIÓN DE CONSTANTES Y ERGONOMÍA DE CABINA
    // --------------------------------------------------------------------------
    @Test
    fun testCabinaErgonomicsConstants() {
        assertTrue(
            "El touch target mínimo de cabina debe ser mayor o igual a 56dp",
            Constants.MIN_TOUCH_TARGET_DP >= 56
        )
        assertTrue(
            "La cuota semanal por defecto debe ser positiva",
            Constants.DEFAULT_WEEKLY_QUOTA_COP > 0L
        )
        assertEquals(
            "La semana laboral base para prorrateo debe ser de 6 días",
            6,
            Constants.WORKING_DAYS_PER_WEEK
        )
    }

    // --------------------------------------------------------------------------
    // BLOQUE 2: VALIDACIÓN DE MODELOS SELLADOS DEL SEMÁFORO
    // --------------------------------------------------------------------------
    @Test
    fun testFinancialStatusExhaustiveModels() {
        val states: List<FinancialStatus> = listOf(
            FinancialStatus.Red,
            FinancialStatus.Amber,
            FinancialStatus.Green
        )

        for (status in states) {
            assertNotNull(status.title)
            assertNotNull(status.subtitle)
            assertTrue("El título no debe estar vacío", status.title.isNotBlank())
            assertTrue("El subtítulo no debe estar vacío", status.subtitle.isNotBlank())
        }

        assertEquals("EN DÉFICIT", FinancialStatus.Red.title)
        assertEquals("EN RUTA A META", FinancialStatus.Amber.title)
        assertEquals("META CUMPLIDA", FinancialStatus.Green.title)
    }

    // --------------------------------------------------------------------------
    // BLOQUE 3: VALIDACIÓN DE CERO MENTIRAS TÉCNICAS (PLACEHOLDERS)
    // --------------------------------------------------------------------------
    @Test
    fun testZeroTechnicalLiesVerification() {
        // Validación del repositorio provisional
        assertTrue(
            "El repositorio provisional debe advertir explícitamente su estado",
            RepositoryPlaceholder.STATUS.contains("⚠️ PENDIENTE DE CONEXIÓN REAL")
        )

        // Validación del estado de UI provisional
        val uiState = HomeUiState.StandbyPlaceholder()
        assertTrue(
            "HomeUiState provisional debe contener el rótulo de cero mentiras técnicas",
            uiState.notice.contains("⚠️ PENDIENTE DE CONEXIÓN REAL")
        )
        assertTrue("La app base debe arrancar en modo offline", uiState.isOffline)
    }
}
