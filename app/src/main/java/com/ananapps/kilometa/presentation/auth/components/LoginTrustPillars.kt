// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/auth/components/LoginTrustPillars.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pilares de confianza y firma corporativa "Una creación de AnAnApps" para la cabina.
// Comunica autoría, solidez arquitectural y ergonomía automotriz (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun LoginTrustPillars(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Línea estética neón con lema
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                EmeraldAccent.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Text(
                text = "CONDUCE TUS FINANZAS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = EmeraldAccent.copy(alpha = 0.9f),
                modifier = Modifier
                    .background(DeepObsidianBackground)
                    .padding(horizontal = 12.dp)
            )
        }

        // 2. Firma oficial de autoría AnAnApps
        Text(
            text = "« Una creación de AnAnApps »",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = TextSecondary.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )

        // 3. Cuadrícula de 3 Pilares de Confianza
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PillarCard(
                iconGlyph = "🛡️",
                title = "Acceso seguro",
                description = "Autenticación confiable en cabina",
                modifier = Modifier.weight(1f)
            )
            PillarCard(
                iconGlyph = "🚘",
                title = "Integración total",
                description = "Vehículo, odómetro y bolsillos",
                modifier = Modifier.weight(1f)
            )
            PillarCard(
                iconGlyph = "⏱️",
                title = "Experiencia fluida",
                description = "Diseño rápido sin complicaciones",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PillarCard(
    iconGlyph: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DeepObsidianSurface)
            .border(1.dp, ObsidianBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = iconGlyph, fontSize = 20.sp)
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                fontSize = 9.sp,
                lineHeight = 12.sp
            )
        }
    }
}
