// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/navigation/components/CockpitHorizonTopBanner.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Banner Superior Fijo Oficial «Cockpit Horizon Skyline» (Orden #060).
// Sustituye el antiguo TopBar azul. Presenta textura automotriz nocturna con
// degradado vertical difuminado hacia DeepObsidianBackground (#0D1117), logotipo
// oficial nítido de KiloMeta y avatar de perfil vehicular con anillo esmeralda StatusGreen.
// Cero campanas decorativas fantasma ni duplicación de textos.
// ==============================================================================

package com.ananapps.kilometa.presentation.navigation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.R
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.StatusGreen

@Composable
fun CockpitHorizonTopBanner(
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DeepObsidianBackground)
    ) {
        // 1. TEXTURA DE ASFALTO NOCTURNO AUTOMOTRIZ CON DIFUMINADO
        Image(
            painter = painterResource(id = R.drawable.bg_cockpit_night),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
        )

        // 2. MÁSCARA DE DEGRADADO VERTICAL HACIA NEGRO ASFALTO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            DeepObsidianBackground.copy(alpha = 0.65f),
                            DeepObsidianBackground
                        )
                    )
                )
        )

        // 3. CONTENIDO PRINCIPAL DEL BANNER EN STATUS BARS PADDING
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // A. LOGOTIPO OFICIAL KILOMETA AMPLIO Y NÍTIDO
            Image(
                painter = painterResource(id = R.drawable.logo_kilometa_cockpit),
                contentDescription = "KiloMeta",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(44.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
            )

            // B. AVATAR DE PERFIL DEL CONDUCTOR (TOUCH TARGET 44dp + ANILLO ESMERALDA)
            Box(
                modifier = Modifier
                    .size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DeepObsidianSurface.copy(alpha = 0.90f))
                        .border(2.dp, StatusGreen, CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToProfile()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👤",
                        fontSize = 18.sp
                    )
                }

                // Micro-indicador de estado en vivo en cabina (verde neón)
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(StatusGreen)
                        .border(1.5.dp, DeepObsidianBackground, CircleShape)
                )
            }
        }
    }
}
