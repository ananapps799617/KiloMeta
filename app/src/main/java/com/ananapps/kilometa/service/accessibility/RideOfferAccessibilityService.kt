// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/service/accessibility/RideOfferAccessibilityService.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Servicio de accesibilidad pasivo de ultra-baja latencia (Orden #047).
// Monitorea ventanas de Uber, DiDi e inDrive, evalúa ofertas en memoria y orquesta la burbuja.
// MANDATO DATAGUARD ANTI-BANEO: 100% solo lectura, cero clics o toques simulados.
// ==============================================================================

package com.ananapps.kilometa.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.ananapps.kilometa.domain.model.HudConfig
import com.ananapps.kilometa.domain.model.ShiftStatus
import com.ananapps.kilometa.domain.parser.OfferNodeParser
import com.ananapps.kilometa.domain.repository.HudConfigRepository
import com.ananapps.kilometa.domain.repository.ShiftRepository
import com.ananapps.kilometa.domain.usecase.EvaluateTripOfferUseCase
import com.ananapps.kilometa.presentation.overlay.RideOfferOverlayManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class RideOfferAccessibilityService : AccessibilityService() {

    @Inject lateinit var overlayManager: RideOfferOverlayManager
    @Inject lateinit var parser: OfferNodeParser
    @Inject lateinit var evaluateTripOfferUseCase: EvaluateTripOfferUseCase
    @Inject lateinit var shiftRepository: ShiftRepository
    @Inject lateinit var hudConfigRepository: HudConfigRepository

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var shiftJob: Job? = null
    private var lastOfferSignature: String = ""
    private var lastOfferTime: Long = 0
    private var currentConfig: HudConfig = HudConfig()

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = serviceInfo ?: android.accessibilityservice.AccessibilityServiceInfo()
        info.flags = info.flags or
                android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                android.accessibilityservice.AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        serviceInfo = info
        startShiftMonitoring()
    }

    private fun startShiftMonitoring() {
        shiftJob?.cancel()
        shiftJob = serviceScope.launch {
            launch {
                shiftRepository.getActiveShift().collect { shift ->
                    val isActive = shift != null && shift.status == ShiftStatus.ACTIVE
                    withContext(Dispatchers.Main) {
                        if (isActive) {
                            overlayManager.showBubble()
                        } else {
                            overlayManager.hideBubble()
                        }
                    }
                }
            }
            launch {
                hudConfigRepository.getHudConfig().collect { config ->
                    currentConfig = config
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Lectura de pantalla en vivo desactivada por mandato de Gerencia.
        // Cero consumo de CPU en eventos de ventana y cero popups invasivos.
        return
    }

    private fun getTargetRootNode(event: AccessibilityEvent): Pair<String, android.view.accessibility.AccessibilityNodeInfo>? {
        val eventPkg = event.packageName?.toString() ?: ""
        val activeRoot = try { rootInActiveWindow } catch (_: Exception) { null }
        val activePkg = activeRoot?.packageName?.toString() ?: ""

        val targetPkg = when {
            isAuthorizedPlatform(eventPkg) -> eventPkg
            isAuthorizedPlatform(activePkg) -> activePkg
            else -> return null
        }

        // Prioridad 1: Nodo del evento ascendiendo a la raíz de la vista que emitió la alerta
        event.source?.let { sourceNode ->
            val root = findWindowRoot(sourceNode)
            return Pair(targetPkg, root)
        }

        // Prioridad 2: rootInActiveWindow directo de la pantalla en primer plano
        if (activeRoot != null) {
            return Pair(targetPkg, activeRoot)
        }

        // Prioridad 3: Ventana de aplicación activa en la colección windows
        try {
            for (win in windows) {
                if (win.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_APPLICATION) {
                    val root = win.root ?: continue
                    return Pair(targetPkg, root)
                }
            }
        } catch (_: Exception) {}

        return null
    }

    private fun findWindowRoot(node: android.view.accessibility.AccessibilityNodeInfo): android.view.accessibility.AccessibilityNodeInfo {
        var current = node
        var parent = current.parent
        while (parent != null) {
            current = parent
            parent = current.parent
        }
        return current
    }

    private fun isAuthorizedPlatform(pkg: String): Boolean {
        val lower = pkg.lowercase()
        return lower.contains("ubercab") || lower.contains("didi") || lower.contains("indriver")
    }

    override fun onInterrupt() {
        // Callback requerido por el framework Android cuando el servicio es interrumpido
    }

    override fun onDestroy() {
        super.onDestroy()
        shiftJob?.cancel()
        serviceScope.cancel()
        overlayManager.hideBubble()
    }
}
