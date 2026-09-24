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
import com.ananapps.kilometa.presentation.theme.CyanSavings
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
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
        // 1. Halo Neón y Badge "Start/Stop Engine"
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            // Halo de brillo difuso de fondo
            Box(
                modifier = Modifier
                    .size(126.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CyanSavings.copy(alpha = 0.28f),
                                ElectricBluePrimary.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(34.dp)
                    )
            )

            // Contenedor del Logo con borde pulido
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(26.dp), spotColor = CyanSavings)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                CyanSavings.copy(alpha = 0.8f),
                                ElectricBluePrimary.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.15f)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF131922)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo KiloMeta Cockpit",
                    modifier = Modifier.size(92.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Marca y Lema de Telemetría
        Text(
            text = "KiloMeta",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            ),
            color = TextPrimary
        )

        Text(
            text = "Tu Copiloto Financiero en Cabina",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = ElectricBluePrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )

        Text(
            text = "Telemetría y control financiero en tiempo real para conductores profesionales.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, start = 20.dp, end = 20.dp)
        )
    }
}
