// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterTripSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet ergonómico y minimalista estilo Cockpit KiloMeta para el registro de carreras.
// Erradica la saturación de botones y estructura la información en filas verticales limpias:
// plataforma, fecha/hora, odómetros inicial/final, finanzas en pesos y botón GUARDAR verde.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.PaymentDestination
import com.ananapps.kilometa.domain.model.PaymentMethod
import com.ananapps.kilometa.domain.model.PlatformConfig
import com.ananapps.kilometa.domain.model.PlatformType
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.presentation.theme.*
import java.text.NumberFormat
import java.util.Locale

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterTripSheet(
    sheetState: SheetState,
    vehicleProfile: VehicleProfile = VehicleProfile(),
    lastKnownOdometer: Int = 0,
    onDismiss: () -> Unit,
    onConfirmTrip: (
        platform: PlatformType,
        gross: Double,
        commission: Double,
        payment: PaymentMethod,
        destination: PaymentDestination,
        timestamp: Long,
        startOdometer: Int?,
        endOdometer: Int?
    ) -> Unit
) {
    var selectedPlatform by remember { mutableStateOf(PlatformType.UBER) }
    var selectedDateTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var startOdometerText by remember(lastKnownOdometer) {
        mutableStateOf(if (lastKnownOdometer > 0) lastKnownOdometer.toString() else "")
    }
    var endOdometerText by remember { mutableStateOf("") }
    var grossAmountText by remember { mutableStateOf("") }
    var commissionAmountText by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }

    // Asistencia inteligente para sugerir comisión al escribir monto o cambiar de plataforma
    fun updateSuggestedCommission(grossStr: String, platform: PlatformType) {
        val gross = grossStr.toDoubleOrNull() ?: 0.0
        if (gross > 0.0) {
            val rule = PlatformConfig.getRule(platform)
            val suggested = (gross * (rule.referenceCommissionPct / 100.0)).toInt()
            commissionAmountText = suggested.toString()
        }
    }

    val gross = grossAmountText.toDoubleOrNull() ?: 0.0
    val commission = commissionAmountText.toDoubleOrNull() ?: 0.0
    val startOdo = startOdometerText.toIntOrNull()
    val endOdo = endOdometerText.toIntOrNull()
    val net = (gross - commission).coerceAtLeast(0.0)

    var showDiscardDialog by remember { mutableStateOf(false) }
    val hasDirtyData = grossAmountText.isNotBlank() || commissionAmountText.isNotBlank() || endOdometerText.isNotBlank()

    val isOdometerValid = if (startOdo != null && endOdo != null) endOdo >= startOdo else true
    val isFormValid = gross > 0.0 && commission <= gross && isOdometerValid

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }

    ModalBottomSheet(
        onDismissRequest = { if (hasDirtyData) showDiscardDialog = true else onDismiss() },
        sheetState = sheetState,
        containerColor = DeepObsidianSurface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera Oficial KiloMeta (Barra Verde Sólida con Flecha y "Carrera")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1B5E20), // Verde Esmeralda KiloMeta
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (hasDirtyData) showDiscardDialog = true else onDismiss() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Carrera",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // 1. Selector de Plataforma
            TripPlatformSelector(
                selectedPlatform = selectedPlatform,
                onPlatformSelected = { newPlatform ->
                    selectedPlatform = newPlatform
                    updateSuggestedCommission(grossAmountText, newPlatform)
                }
            )

            // 2. Fila Telemetría (Vehículo, Fecha, Hora, Odómetros inicial/final)
            TripTelemetrySection(
                vehicleProfile = vehicleProfile,
                selectedDateTimeMillis = selectedDateTimeMillis,
                onDateTimeSelected = { selectedDateTimeMillis = it },
                startOdometerText = startOdometerText,
                onStartOdometerChange = { startOdometerText = it },
                endOdometerText = endOdometerText,
                onEndOdometerChange = { endOdometerText = it },
                lastKnownOdometer = lastKnownOdometer
            )

            // 3. Fila Financiera (Cobrado, Descuento, Ganancia Neta, Medio de cobro)
            TripFinancialSection(
                grossAmountText = grossAmountText,
                onGrossAmountChange = {
                    grossAmountText = it
                    updateSuggestedCommission(it, selectedPlatform)
                },
                commissionAmountText = commissionAmountText,
                onCommissionAmountChange = { commissionAmountText = it },
                selectedPaymentMethod = selectedPaymentMethod,
                onPaymentMethodSelected = {
                    selectedPaymentMethod = it
                    if (commissionAmountText.isBlank() && grossAmountText.isNotBlank()) {
                        updateSuggestedCommission(grossAmountText, selectedPlatform)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Botón Único Principal GUARDAR (58dp de altura, color verde StatusGreen)
            Button(
                onClick = {
                    val dest = when (selectedPaymentMethod) {
                        PaymentMethod.CASH, PaymentMethod.NEQUI -> PaymentDestination.CASH_IN_HAND
                        PaymentMethod.APP_WALLET -> PaymentDestination.APP_RETAINED
                    }

                    onConfirmTrip(
                        selectedPlatform,
                        gross,
                        commission,
                        selectedPaymentMethod,
                        dest,
                        selectedDateTimeMillis,
                        startOdo,
                        endOdo
                    )
                    onDismiss()
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusGreen,
                    disabledContainerColor = StatusGreen.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = if (gross > 0.0) "GUARDAR (+${currencyFormatter.format(net)})" else "GUARDAR",
                    color = if (isFormValid) Color.Black else TextSecondary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            title = "¿Descartar Carrera?",
            message = "Tienes datos digitados sin guardar. ¿Deseas salir y perder los cambios de esta carrera?",
            onConfirmDiscard = {
                showDiscardDialog = false
                onDismiss()
            },
            onDismissRequest = { showDiscardDialog = false }
        )
    }
}
