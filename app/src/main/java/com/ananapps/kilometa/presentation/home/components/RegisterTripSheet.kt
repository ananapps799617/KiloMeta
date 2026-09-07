// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterTripSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet express diseñado para captura de carreras a 3 toques en cabina.
// Incorpora chips rápidos de monto (+10k, +15k, +20k, +30k), selector de plataforma
// con comisión precargada y selector rápido de método de pago (Efectivo vs Digital).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.util.Locale

// ------------------------------------------------------------------------------
// BLOQUE 1: BOTTOM SHEET EXPRESS PARA CARRERAS (3 TOQUES)
// ------------------------------------------------------------------------------
/**
 * Hoja modal de captura ágil de carreras.
 *
 * @param sheetState Estado de visibilidad y animación de la hoja modal.
 * @param onDismiss Callback al cerrar la hoja.
 * @param onConfirmTrip Callback con los datos validados de la carrera registrada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterTripSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirmTrip: (platform: PlatformType, gross: Double, commission: Double, payment: PaymentMethod) -> Unit
) {
    var selectedPlatform by remember { mutableStateOf(PlatformType.UBER) }
    var grossAmount by remember { mutableDoubleStateOf(15_000.0) }
    var selectedPayment by remember { mutableStateOf(PaymentMethod.CASH) }

    // Comisión sugerida automática según plataforma: Uber 25%, DiDi 20%, InDrive 10%, Particular 0%
    val defaultCommissionRate = when (selectedPlatform) {
        PlatformType.UBER -> 0.25
        PlatformType.DIDI -> 0.20
        PlatformType.INDRIVE -> 0.10
        PlatformType.PRIVATE -> 0.00
    }
    val calculatedCommission = grossAmount * defaultCommissionRate
    val calculatedNet = grossAmount - calculatedCommission

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidianSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            Text(
                text = "REGISTRAR CARRERA EXPRESS",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ElectricBluePrimary
            )

            // Selector de Plataforma
            Text(
                text = "Plataforma de Servicio",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlatformType.entries.forEach { platform ->
                    FilterChip(
                        selected = selectedPlatform == platform,
                        onClick = { selectedPlatform = platform },
                        label = { Text(platform.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBluePrimary,
                            selectedLabelColor = DeepObsidianBackground
                        )
                    )
                }
            }

            // Monto Bruto con Chips Rápidos (+10k, +15k, +20k, +30k)
            Text(
                text = "Monto Bruto: ${formatCop(grossAmount)}",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(10_000.0, 15_000.0, 20_000.0, 30_000.0).forEach { chipAmount ->
                    SuggestionChip(
                        onClick = { grossAmount = chipAmount },
                        label = { Text("+${chipAmount.toInt() / 1000}k") }
                    )
                }
            }

            // Selector de Método de Pago (Efectivo vs Digital)
            Text(
                text = "Modalidad de Pago",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    PaymentMethod.CASH to "Efectivo",
                    PaymentMethod.NEQUI to "Nequi/Digital",
                    PaymentMethod.APP_WALLET to "Billetera App"
                ).forEach { (method, label) ->
                    FilterChip(
                        selected = selectedPayment == method,
                        onClick = { selectedPayment = method },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusGreen,
                            selectedLabelColor = DeepObsidianBackground
                        )
                    )
                }
            }

            // Resumen de liquidación estimada
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comisión: -${formatCop(calculatedCommission)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "Neto a Caja: +${formatCop(calculatedNet)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = StatusGreen
                )
            }

            // Botón de Confirmación Ergonómico (>= 56dp)
            Button(
                onClick = {
                    onConfirmTrip(selectedPlatform, grossAmount, calculatedCommission, selectedPayment)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen)
            ) {
                Text(
                    text = "Guardar Carrera (+${formatCop(calculatedNet)})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = DeepObsidianBackground
                )
            }
        }
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: FORMATEADOR AUXILIAR
// ------------------------------------------------------------------------------
private fun formatCop(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return String.format(Locale.GERMANY, "$%,d COP", rounded)
}
