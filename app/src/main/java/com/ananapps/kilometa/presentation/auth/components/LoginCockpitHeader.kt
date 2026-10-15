// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/auth/components/LoginCockpitHeader.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Cabecera estilizada de autenticación inspirada en el botón Start/Stop de cabina deportiva.
// Integra halo luminoso de neón y tipografía calibrada de alta gama (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.R
import com.ananapps.kilometa.presentation.theme.EmeraldAccent
import com.ananapps.kilometa.presentation.theme.EmeraldPrimary
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

@Composable
fun LoginCockpitHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Halo Neón Esmeralda y Badge "Start/Stop Engine"
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            // Halo de brillo difuso de fondo
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                EmeraldAccent.copy(alpha = 0.32f),
                                EmeraldPrimary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
            )

            // Contenedor del Logo con borde pulido esmeralda
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), spotColor = EmeraldAccent)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                EmeraldAccent.copy(alpha = 0.85f),
                                EmeraldPrimary.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF131922)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo KiloMeta Cockpit",
                    modifier = Modifier.size(84.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Marca y Lema de Cabina
        Text(
            text = "KiloMeta",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            ),
            color = TextPrimary
        )

        Text(
            text = "TU ESFUERZO · TU CONTROL · TU META",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            ),
            color = EmeraldAccent,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = "Inicia sesión para continuar en tu cabina financiera.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, start = 20.dp, end = 20.dp)
        )
    }
}
