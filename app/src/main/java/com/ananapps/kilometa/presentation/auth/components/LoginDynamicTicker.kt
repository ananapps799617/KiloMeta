// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/auth/components/LoginDynamicTicker.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Cápsula Glassmorphism de telemetría dinámica con rotación automática cada 3.5 segundos.
// Comunica el valor premium de KiloMeta con estética de tacómetro digital (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.auth.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.CyanSavings
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusAmber
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import kotlinx.coroutines.delay

private data class TickerHighlight(
    val icon: String,
    val text: String,
    val accentColor: Color
)

private val TICKER_ITEMS = listOf(
    TickerHighlight("⛽", "Telemetría Bi-Combustible GNV / Gas", CyanSavings),
    TickerHighlight("🎯", "Control de Cuota y Bolsa Semanal", StatusGreen),
    TickerHighlight("📸", "Auditoría OCR DiDi · Uber · inDrive", StatusAmber),
    TickerHighlight("🛡️", "Modo Cabina 100% Offline-First", ElectricBluePrimary)
)

@Composable
fun LoginDynamicTicker(
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3500L)
            currentIndex = (currentIndex + 1) % TICKER_ITEMS.size
        }
    }

    val currentItem = TICKER_ITEMS[currentIndex]

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF131A24).copy(alpha = 0.85f))
            .border(
                width = 1.dp,
                color = currentItem.accentColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentItem,
            transitionSpec = {
                (slideInVertically { height -> height / 2 } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height / 2 } + fadeOut())
            },
            label = "LoginTickerTransition"
        ) { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.icon, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    ),
                    color = TextPrimary
                )
            }
        }
    }
}
