// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/auth/components/LoginCockpitHeader.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Cabecera oficial de autenticación "Cockpit Dark". Presenta el logotipo oficial
// integrado (tacómetro esmeralda + KiloMeta + lema) sin contenedores artificiales,
// seguido del bloque de bienvenida alineado a la izquierda para lucir el deportivo
// en el costado derecho del fondo (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.R
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
        // 1. Logotipo Oficial de Marca (Tacómetro Esmeralda + KiloMeta + Lema con fondo transparente)
        Image(
            painter = painterResource(id = R.drawable.logo_kilometa_cockpit),
            contentDescription = "Logotipo Oficial KiloMeta — Tu Esfuerzo, Tu Control, Tu Meta",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .heightIn(min = 100.dp, max = 132.dp)
        )

        // Espacio calibrado para dejar visible la autopista y el deportivo a la derecha
        Spacer(modifier = Modifier.height(28.dp))

        // 2. Bloque de Bienvenida alineado a la izquierda (fiel a la maqueta oficial)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    letterSpacing = 0.2.sp
                ),
                color = TextPrimary,
                textAlign = TextAlign.Start
            )

            Text(
                text = "Inicia sesión para continuar\nen tu cabina financiera.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                color = TextSecondary,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
