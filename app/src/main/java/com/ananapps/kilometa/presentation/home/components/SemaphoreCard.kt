// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/SemaphoreCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Componente de cabina de alta legibilidad que renderiza el Semáforo Financiero.
// Utiliza contraste cromático superior y tipografía de gran tamaño para lectura
// instantánea a más de 50 cm de distancia del tablero del vehículo.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.FinancialStatus
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.StatusAmber
import com.ananapps.kilometa.presentation.theme.StatusAmberContainer
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusGreenContainer
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.StatusRedContainer
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

// ------------------------------------------------------------------------------
// BLOQUE 1: COMPONENTE SEMAPHORE CARD DE CABINA
// ------------------------------------------------------------------------------
/**
 * Renderiza la tarjeta central del estado financiero del conductor.
 *
 * @param status Estado financiero actual ([FinancialStatus.Red], [FinancialStatus.Amber], [FinancialStatus.Green]).
 * @param modifier Modificador Compose para dimensionamiento y espaciado.
 */
@Composable
fun SemaphoreCard(
    status: FinancialStatus,
    modifier: Modifier = Modifier
) {
    val (statusColor, containerColor) = when (status) {
        is FinancialStatus.Red -> StatusRed to StatusRedContainer
        is FinancialStatus.Amber -> StatusAmber to StatusAmberContainer
        is FinancialStatus.Green -> StatusGreen to StatusGreenContainer
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(Constants.SEMAPHORE_CARD_MIN_HEIGHT_DP.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepObsidianSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(statusColor)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Fila superior: Indicador luminoso y Título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                        .border(2.dp, containerColor, CircleShape)
                )

                Text(
                    text = status.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fila central: Explicación de estado operativa
            Text(
                text = status.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Fila inferior: Indicador contextual de acción
            Text(
                text = "Semáforo en tiempo real • Cabina KiloMeta",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
