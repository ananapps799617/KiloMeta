// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/TripFinancialSummaryRow.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Componente desacoplado para el resumen contable en vivo en la captura de carreras.
// Muestra el Neto a Caja, la Comisión retenida, micro-chips de calibración fina (-1% / +1%)
// y la alerta de deuda corriente con la app si el viaje fue en efectivo.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun TripFinancialSummaryRow(
    platform: PlatformType,
    calculatedGross: Double,
    calculatedCommission: Double,
    calculatedNet: Double,
    commissionPct: Double,
    paymentDestination: PaymentDestination,
    onCommissionPctChange: (Double) -> Unit,
    onExactCommissionChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showExactCommissionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DeepObsidianSurfaceVariant)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Fila de métricas: Comisión vs Neto
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "COMISIÓN ${platform.displayName.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "- $ ${String.format(Locale.GERMANY, "%,d", calculatedCommission.toLong())}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = StatusRed
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "TU GANANCIA NETA",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusGreen
                )
                Text(
                    text = "+ $ ${String.format(Locale.GERMANY, "%,d", calculatedNet.toLong())}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = StatusGreen
                )
            }
        }

        // Micro-chips de ajuste fino
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip -1%
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepObsidianSurface)
                        .clickable { onCommissionPctChange((commissionPct - 1.0).coerceAtLeast(0.0)) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("-1%", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                }

                // Chip % actual / Editar exacto
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricBluePrimary.copy(alpha = 0.15f))
                        .border(1.dp, ElectricBluePrimary, RoundedCornerShape(8.dp))
                        .clickable { showExactCommissionDialog = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", commissionPct)}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricBluePrimary
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar comisión exacta",
                            tint = ElectricBluePrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Chip +1%
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepObsidianSurface)
                        .clickable { onCommissionPctChange((commissionPct + 1.0).coerceAtMost(100.0)) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("+1%", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                }
            }

            // Nota de destino de fondos
            val isCash = paymentDestination == PaymentDestination.CASH_IN_HAND
            Text(
                text = if (isCash) "⚠️ Deuda con App" else "💳 Saldo App",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isCash) StatusAmber else CyanSavings
            )
        }
    }

    // Diálogo modal para ingresar comisión exacta en pesos
    if (showExactCommissionDialog) {
        var exactText by remember { mutableStateOf(calculatedCommission.toLong().toString()) }

        AlertDialog(
            onDismissRequest = { showExactCommissionDialog = false },
            title = { Text("Comisión Exacta de ${platform.displayName}", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Digita el valor exacto en pesos que la app te descontó:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = exactText,
                        onValueChange = { exactText = it.filter { c -> c.isDigit() } },
                        label = { Text("Comisión en COP") },
                        prefix = { Text("$ ") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = exactText.toDoubleOrNull() ?: calculatedCommission
                        onExactCommissionChange(num)
                        showExactCommissionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                ) {
                    Text("Aplicar Exacto", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExactCommissionDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = DeepObsidianSurface
        )
    }
}
