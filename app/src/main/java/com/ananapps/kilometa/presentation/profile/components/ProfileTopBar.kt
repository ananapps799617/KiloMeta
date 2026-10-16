// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/ProfileTopBar.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Cabecera ergonómica para el perfil vehicular. Muestra el logotipo oficial de
// KiloMeta con tacómetro esmeralda en modo Onboarding, o barra de navegación
// en modo edición de ajustes (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.R
import com.ananapps.kilometa.presentation.theme.TextPrimary

@Composable
fun ProfileTopBar(
    isOnboardingMode: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOnboardingMode) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_kilometa_cockpit),
                contentDescription = "Logotipo Oficial KiloMeta — Tu Esfuerzo, Tu Control, Tu Meta",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .heightIn(min = 68.dp, max = 92.dp)
            )
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Text(text = "←", fontSize = 24.sp, color = TextPrimary)
            }
            Text(
                text = "⚙️ Perfil y Vehículo",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}
