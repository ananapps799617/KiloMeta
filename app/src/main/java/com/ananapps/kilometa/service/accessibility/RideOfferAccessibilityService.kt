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
        if (event == null) return

        // 1. Obtener la ventana completa de la app de transporte de forma síncrona en el hilo principal
        val (targetPkg, rootNode) = getTargetRootNode(event) ?: return
        val texts = try {
            parser.extractAllTexts(rootNode)
        } catch (e: Exception) {
            emptyList()
        }
        if (texts.isEmpty()) return

        // 2. Procesar matemáticamente la oferta en segundo plano
        serviceScope.launch {
            try {
                val offer = parser.parse(targetPkg, texts) ?: return@launch
                val signature = "${offer.platform}_${offer.offeredFare}_${offer.pickupDistanceKm}_${offer.tripDistanceKm}"

                // Debounce para evitar recalcular la misma oferta en eventos continuos
                val now = System.currentTimeMillis()
                if (signature == lastOfferSignature && (now - lastOfferTime) < 4_000) {
                    return@launch
                }

                lastOfferSignature = signature
                lastOfferTime = now

                val evaluation = evaluateTripOfferUseCase(offer, currentConfig)
                android.util.Log.d("RideOfferService", "Oferta detectada: ${offer.platform} Fare: ${offer.offeredFare} - Semáforo: ${evaluation.semaphoreTier}")
                withContext(Dispatchers.Main) {
                    overlayManager.showOfferEvaluation(evaluation)
                }
            } catch (e: Exception) {
                android.util.Log.w("RideOfferService", "Error procesando evento de accesibilidad: ${e.message}")
            }
        }
    }

    private fun getTargetRootNode(event: AccessibilityEvent): Pair<String, android.view.accessibility.AccessibilityNodeInfo>? {
        val eventPkg = event.packageName?.toString() ?: ""

        // Prioridad 1: Si el evento viene de Uber, DiDi o inDrive, buscar su ventana de aplicación real (TYPE_APPLICATION)
        if (isAuthorizedPlatform(eventPkg)) {
            findAppWindowRoot(eventPkg)?.let { return Pair(eventPkg, it) }

            // Fallback al rootInActiveWindow si pertenece al mismo paquete
            try {
                rootInActiveWindow?.let { root ->
                    val activePkg = root.packageName?.toString() ?: ""
                    if (activePkg.equals(eventPkg, ignoreCase = true)) {
                        return Pair(eventPkg, root)
                    }
                }
            } catch (_: Exception) {}

            // Fallback al source del evento navegando a la raíz del árbol
            event.source?.let {
                val root = findWindowRoot(it)
                val pkg = root.packageName?.toString() ?: eventPkg
                if (isAuthorizedPlatform(pkg)) return Pair(pkg, root)
            }
        }

        // Prioridad 2: Buscar entre todas las ventanas activas una ventana TYPE_APPLICATION (ignorar burbujas flotantes)
        try {
            val winList = windows
            for (win in winList) {
                if (win.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_APPLICATION) {
                    val root = win.root ?: continue
                    val pkg = root.packageName?.toString() ?: ""
                    if (isAuthorizedPlatform(pkg)) {
                        return Pair(pkg, root)
                    }
                }
            }
        } catch (_: Exception) {}

        // Prioridad 3: rootInActiveWindow
        try {
            rootInActiveWindow?.let { root ->
                val pkg = root.packageName?.toString() ?: ""
                if (isAuthorizedPlatform(pkg)) {
                    return Pair(pkg, root)
                }
            }
        } catch (_: Exception) {}

        return null
    }

    private fun findAppWindowRoot(targetPkg: String): android.view.accessibility.AccessibilityNodeInfo? {
        try {
            val winList = windows
            for (win in winList) {
                if (win.type == android.view.accessibility.AccessibilityWindowInfo.TYPE_APPLICATION) {
                    val root = win.root ?: continue
                    val pkg = root.packageName?.toString() ?: ""
                    if (pkg.equals(targetPkg, ignoreCase = true)) {
                        return root
                    }
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
