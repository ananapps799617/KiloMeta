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
        val eventPkg = event.packageName?.toString() ?: ""
        val activePkg = try { rootInActiveWindow?.packageName?.toString() ?: "" } catch (_: Exception) { "" }

        val targetPkg = when {
            isAuthorizedPlatform(eventPkg) -> eventPkg
            isAuthorizedPlatform(activePkg) -> activePkg
            else -> return
        }

        // 1. Obtener la ventana completa de forma síncrona en el hilo principal
        val rootNode = findWindowRoot(event) ?: rootInActiveWindow ?: return
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
                if (signature == lastOfferSignature && (now - lastOfferTime) < 5_000) {
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

    private fun findWindowRoot(event: AccessibilityEvent): android.view.accessibility.AccessibilityNodeInfo? {
        var current = event.source ?: return null
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
