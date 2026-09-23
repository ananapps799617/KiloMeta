// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/overlay/RideOfferOverlayManager.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Orquestador del ciclo de vida de la ventana flotante y burbuja de cabina (Orden #047).
// Gestiona arrastre magnético, HUD expandido de oferta y speed dial sobre cualquier app.
// ==============================================================================

package com.ananapps.kilometa.presentation.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.TripOfferEvaluation
import com.ananapps.kilometa.presentation.MainActivity
import com.ananapps.kilometa.presentation.theme.StatusGreen
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class RideOfferOverlayManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayComposeView: ComposeView? = null
    private var overlayLifecycleOwner: OverlayLifecycleOwner? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var autoDismissJob: Job? = null

    private var currentEvaluation by mutableStateOf<TripOfferEvaluation?>(null)
    private var isSpeedDialOpen by mutableStateOf(false)
    private var isBubbleAttached = false

    fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)

    fun showBubble() {
        if (!canDrawOverlays() || isBubbleAttached) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 300
        }
        layoutParams = params

        val owner = OverlayLifecycleOwner()
        owner.onCreate()
        owner.onResume()
        overlayLifecycleOwner = owner

        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setContent {
                OverlayHostContent()
            }
        }
        attachDraggableTouchListener(composeView, params)

        try {
            windowManager.addView(composeView, params)
            overlayComposeView = composeView
            isBubbleAttached = true
        } catch (e: Exception) {
            owner.onDestroy()
            overlayLifecycleOwner = null
        }
    }

    fun hideBubble() {
        if (!isBubbleAttached) return
        overlayComposeView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
        overlayLifecycleOwner?.onDestroy()
        overlayLifecycleOwner = null
        overlayComposeView = null
        isBubbleAttached = false
        currentEvaluation = null
        isSpeedDialOpen = false
        autoDismissJob?.cancel()
    }

    private var savedBubbleX = 20
    private var savedBubbleY = 300

    fun showOfferEvaluation(evaluation: TripOfferEvaluation) {
        if (!isBubbleAttached) showBubble()
        currentEvaluation = evaluation
        isSpeedDialOpen = false
        repositionWindow(isCard = true)

        autoDismissJob?.cancel()
        autoDismissJob = scope.launch {
            delay(15_000)
            dismissOffer()
        }
    }

    fun dismissOffer() {
        currentEvaluation = null
        autoDismissJob?.cancel()
        repositionWindow(isCard = false)
    }

    private fun repositionWindow(isCard: Boolean) {
        val params = layoutParams ?: return
        val view = overlayComposeView ?: return
        if (isCard) {
            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.x = 0
            params.y = 180
        } else {
            params.gravity = Gravity.TOP or Gravity.START
            params.x = savedBubbleX
            params.y = savedBubbleY
        }
        try {
            windowManager.updateViewLayout(view, params)
        } catch (_: Exception) {}
    }

    @Composable
    private fun OverlayHostContent() {
        val eval = currentEvaluation
        when {
            eval != null -> {
                RideOfferOverlayView(
                    evaluation = eval,
                    onDismiss = { dismissOffer() }
                )
            }
            isSpeedDialOpen -> {
                CockpitSpeedDialOverlay(
                    onSelectTrip = { launchKiloMetaAction("ACTION_QUICK_TRIP") },
                    onSelectFueling = { launchKiloMetaAction("ACTION_QUICK_FUELING") },
                    onSelectExpense = { launchKiloMetaAction("ACTION_QUICK_EXPENSE") },
                    onSelectMaintenance = { launchKiloMetaAction("ACTION_QUICK_MAINTENANCE") },
                    onDismiss = { isSpeedDialOpen = false }
                )
            }
            else -> {
                CockpitFloatingBubble()
            }
        }
    }

    @Composable
    private fun CockpitFloatingBubble() {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F141C))
                .border(2.dp, StatusGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "KM",
                color = StatusGreen,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }
    }

    private fun launchKiloMetaAction(action: String) {
        isSpeedDialOpen = false
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_QUICK_ACTION", action)
        }
        context.startActivity(intent)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attachDraggableTouchListener(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        view.setOnTouchListener { v, event ->
            if (currentEvaluation != null) return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    savedBubbleX = params.x
                    savedBubbleY = params.y
                    try {
                        windowManager.updateViewLayout(v, params)
                    } catch (_: Exception) {}
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = abs(event.rawX - initialTouchX)
                    val deltaY = abs(event.rawY - initialTouchY)
                    if (deltaX < 15 && deltaY < 15) {
                        isSpeedDialOpen = !isSpeedDialOpen
                    }
                    true
                }
                else -> false
            }
        }
    }
}
