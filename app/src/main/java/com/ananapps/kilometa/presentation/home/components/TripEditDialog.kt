// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/TripEditDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal ergonómico para la edición integral de carreras en cabina.
// Permite modificar plataforma, método de cobro, odómetros (inicial/final) y finanzas
// con recálculo reactivo en vivo de ganancia neta.
// Regla inquebrantable: Archivo pedagógico < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.Trip
import com.ananapps.kilometa.presentation.theme.*
import com.ananapps.kilometa.presentation.theme.visualToken
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TripEditDialog(
    trip: Trip,
    onDismiss: () -> Unit,
    onConfirm: (Trip) -> Unit
) {
    var platform by remember(trip.id) { mutableStateOf(trip.platformType) }
    var paymentMethod by remember(trip.id) { mutableStateOf(trip.paymentMethod) }
    var grossText by remember(trip.id) { mutableStateOf(trip.grossAmount.toLong().toString()) }
    var commissionText by remember(trip.id) { mutableStateOf(trip.commissionAmount.toLong().toString()) }
    var startOdoText by remember(trip.id) { mutableStateOf(trip.startOdometer?.toString() ?: "") }
    var endOdoText by remember(trip.id) { mutableStateOf(trip.endOdometer?.toString() ?: "") }
    var errorMessage by remember(trip.id) { mutableStateOf<String?>(null) }

    val gross = grossText.toDoubleOrNull() ?: 0.0
    val commission = commissionText.toDoubleOrNull() ?: 0.0
    val net = (gross - commission).coerceAtLeast(0.0)

    val startOdo = startOdoText.toIntOrNull()
    val endOdo = endOdoText.toIntOrNull()

    val copFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepObsidianSurface,
        title = { Text("Editar Carrera", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Selector de Plataforma
                Text("Plataforma:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PlatformType.entries.forEach { p ->
                        val token = p.visualToken
                        val isSelected = platform == p
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { platform = p },
                            color = if (isSelected) ElectricBluePrimary.copy(alpha = 0.2f) else DeepObsidianSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) ElectricBluePrimary else token.backgroundColor.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.size(16.dp).clip(CircleShape).background(token.backgroundColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = token.monogram, fontSize = 8.sp, fontWeight = FontWeight.Black, color = token.contentColor)
                                }
                                Text(text = " " + token.badgeLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }
                }

                // Selector de Método de Pago
                Text("Medio de Cobro:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val methods = listOf(
                        PaymentMethod.CASH to "💵 Efectivo",
                        PaymentMethod.NEQUI to "🟣 Nequi",
                        PaymentMethod.APP_WALLET to "📱 App"
                    )
                    methods.forEach { (m, label) ->
                        val isSelected = paymentMethod == m
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { paymentMethod = m },
                            color = if (isSelected) ElectricBluePrimary.copy(alpha = 0.25f) else DeepObsidianSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) ElectricBluePrimary else DeepObsidianSurfaceVariant)
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) ElectricBluePrimary else TextPrimary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Odómetros
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startOdoText,
                        onValueChange = { startOdoText = it },
                        label = { Text("Odo. Inicial (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = ElectricBluePrimary)
                    )
                    OutlinedTextField(
                        value = endOdoText,
                        onValueChange = { endOdoText = it },
                        label = { Text("Odo. Final (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = ElectricBluePrimary)
                    )
                }

                // Indicador de Km calculados
                if (startOdo != null && endOdo != null) {
                    val dist = endOdo - startOdo
                    if (dist >= 0) {
                        Text("🚗 Carrera: $dist km recorridos", color = ElectricBluePrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    } else {
                        Text("⚠️ Odómetro final menor al inicial", color = StatusRed, style = MaterialTheme.typography.labelSmall)
                    }
                }

                // Cobro y Comisión
                OutlinedTextField(
                    value = grossText,
                    onValueChange = { grossText = it },
                    label = { Text("¿Cuánto cobraste? ($ COP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = ElectricBluePrimary)
                )

                OutlinedTextField(
                    value = commissionText,
                    onValueChange = { commissionText = it },
                    label = { Text("¿Cuánto descuentan? ($ COP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = ElectricBluePrimary)
                )

                // Tarjeta de Ganancia Limpia
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StatusGreenContainer)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TU GANANCIA LIMPIA: ${copFormat.format(net)}",
                        color = StatusGreen,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = StatusRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (gross < commission) {
                        errorMessage = "El cobro no puede ser menor a la comisión"
                        return@Button
                    }
                    if (startOdo != null && endOdo != null && endOdo < startOdo) {
                        errorMessage = "El odómetro final no puede ser menor al inicial"
                        return@Button
                    }
                    val destination = when (paymentMethod) {
                        PaymentMethod.APP_WALLET -> PaymentDestination.APP_RETAINED
                        PaymentMethod.CASH, PaymentMethod.NEQUI -> PaymentDestination.CASH_IN_HAND
                    }
                    onConfirm(
                        trip.copy(
                            platformType = platform,
                            paymentMethod = paymentMethod,
                            paymentDestination = destination,
                            grossAmount = gross,
                            commissionAmount = commission,
                            netAmount = net,
                            startOdometer = startOdo,
                            endOdometer = endOdo
                        )
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
            ) {
                Text("GUARDAR", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = TextSecondary) }
        }
    )
}
