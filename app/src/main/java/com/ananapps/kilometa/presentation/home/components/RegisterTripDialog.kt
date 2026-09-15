// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterTripDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante centrado de 3 capas para registro de carreras (Orden #037).
// Cabecera verde fija con botón de retroceso/cierre, cuerpo scrollable para telemetría
// y finanzas, y botón GUARDAR fijo de 56dp en el pie inamovible.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ananapps.kilometa.domain.model.*
import com.ananapps.kilometa.presentation.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RegisterTripDialog(
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
    ) -> Unit,
    modifier: Modifier = Modifier
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

    Dialog(
        onDismissRequest = { if (hasDirtyData) showDiscardDialog = true else onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .border(1.dp, DeepObsidianBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = DeepObsidianSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // CAPA 1: CABECERA FIJA
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1B5E20),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🏁 Nueva Carrera",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        IconButton(onClick = { if (hasDirtyData) showDiscardDialog = true else onDismiss() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CAPA 2: CUERPO DESPLAZABLE INTERNO
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TripPlatformSelector(
                        selectedPlatform = selectedPlatform,
                        onPlatformSelected = { newPlatform ->
                            selectedPlatform = newPlatform
                            updateSuggestedCommission(grossAmountText, newPlatform)
                        }
                    )

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
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CAPA 3: BOTONERA DE ACCIÓN FIJA (56dp)
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
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusGreen,
                        disabledContainerColor = StatusGreen.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = if (gross > 0.0) "GUARDAR (+)" else "GUARDAR CARRERA",
                        color = if (isFormValid) Color.Black else TextSecondary,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
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
