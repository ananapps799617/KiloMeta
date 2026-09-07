// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/CockpitBottomDock.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Barra inferior de acciones ergonómicas (Bottom Dock) diseñada para uso táctil en cabina.
// Garantiza objetivos táctiles (Touch Targets) superiores a 56dp con botones de alto contraste
// para registro rápido de carreras (+) y abastecimiento/gastos (-).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary

// ------------------------------------------------------------------------------
// BLOQUE 1: BOTONERA INFERIOR DE CABINA (TOUCH TARGET >= 56DP)
// ------------------------------------------------------------------------------
/**
 * Renderiza la botonera de acceso rápido del cockpit.
 *
 * @param onQuickTrip Callback para apertura del sheet de carreras (+).
 * @param onQuickFueling Callback para apertura del sheet de tanqueo / gastos (-).
 * @param modifier Modificador Compose.
 */
@Composable
fun CockpitBottomDock(
    onQuickTrip: () -> Unit,
    onQuickFueling: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DeepObsidianSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onQuickFueling,
                modifier = Modifier
                    .weight(1f)
                    .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
            ) {
                Text(
                    text = "Tanqueo (-)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }

            Button(
                onClick = onQuickTrip,
                modifier = Modifier
                    .weight(1f)
                    .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
            ) {
                Text(
                    text = "Carrera (+)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = DeepObsidianBackground
                )
            }
        }
    }
}
