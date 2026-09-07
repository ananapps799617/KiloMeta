// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/BiFuelSavingsCard.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Tarjeta estelar de analíticas bi-combustible con borde cian de alto impacto visual.
// Cuantifica de forma transparente el dinero neto ahorrado en COP gracias al uso de GNV
// y la relación porcentual de eficiencia frente al costo de gasolina corriente.
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.BiFuelSavingsMetrics
import com.ananapps.kilometa.presentation.theme.CyanSavings
import com.ananapps.kilometa.presentation.theme.CyanSavingsContainer
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurfaceVariant
import com.ananapps.kilometa.presentation.theme.FuelGas
import com.ananapps.kilometa.presentation.theme.FuelGasoline
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: TARJETA ESTELAR DE AHORRO GNV (BORDE CIAN)
// ------------------------------------------------------------------------------
/**
 * Renderiza la tarjeta de auditoría bi-combustible con el ahorro neto logrado.
 *
 * @param metrics Métricas consolidadas calculadas por GetBiFuelSavingsUseCase.
 * @param modifier Modificador Compose.
 */
@Composable
fun BiFuelSavingsCard(
    metrics: BiFuelSavingsMetrics,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
        border = BorderStroke(1.5.dp, CyanSavings)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Encabezado con Badge de Ahorro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AUDITORÍA BI-COMBUSTIBLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .background(CyanSavingsContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AHORRO GNV",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanSavings,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Monto de Ahorro Principal
            val formattedSavings = currencyFormat.format(metrics.netSavingsAmount)
            Text(
                text = "+$formattedSavings",
                style = MaterialTheme.typography.headlineMedium,
                color = CyanSavings,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "${String.format(Locale.US, "%.1f", metrics.savingsPercentage)}% de ahorro vs Gasolina Corriente",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Desglose de Costos de Combustible
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepObsidianSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Gasto Real GNV",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = currencyFormat.format(metrics.actualFuelExpense),
                        style = MaterialTheme.typography.titleMedium,
                        color = FuelGas,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Equivalente Gasolina",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = currencyFormat.format(metrics.equivalentGasolineExpense),
                        style = MaterialTheme.typography.titleMedium,
                        color = FuelGasoline,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Costo por Kilómetro Recorrido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Costo GNV: ${currencyFormat.format(metrics.costPerKmGas)}/km",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "${metrics.totalKmDriven} km rodados",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}
