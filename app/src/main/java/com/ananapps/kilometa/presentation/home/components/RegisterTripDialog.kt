// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterTripDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante unificado para Registro y Edición simétrica de Carreras (Orden #041).
// Utiliza CockpitFloatingScaffold y subcomponentes modulares garantizando paridad total y < 280 LOC.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ananapps.kilometa.domain.model.*
import com.ananapps.kilometa.presentation.common.CockpitFloatingScaffold
import com.ananapps.kilometa.presentation.theme.*

private val TripGreenHeader = Color(0xFF1B5E20)

@Composable
fun RegisterTripDialog(
    vehicleProfile: VehicleProfile = VehicleProfile(),
    lastKnownOdometer: Int = 0,
    onDismiss: () -> Unit,
    existingTrip: Trip? = null,
    onConfirmTrip: (
        platform: PlatformType,
        gross: Double,
        commission: Double,
        payment: PaymentMethod,
        destination: PaymentDestination,
        timestamp: Long,
        startOdometer: Int?,
        endOdometer: Int?
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onConfirmUpdate: (Trip) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isEditing = existingTrip != null
    var selectedPlatform by remember(existingTrip) {
        mutableStateOf(existingTrip?.platformType ?: PlatformType.UBER)
    }
    var selectedDateTimeMillis by remember(existingTrip) {
        mutableStateOf(existingTrip?.timestamp ?: System.currentTimeMillis())
    }
    var startOdometerText by remember(existingTrip, lastKnownOdometer) {
        val odo = existingTrip?.startOdometer ?: if (lastKnownOdometer > 0) lastKnownOdometer else null
        mutableStateOf(odo?.toString() ?: "")
    }
    var endOdometerText by remember(existingTrip) {
        mutableStateOf(existingTrip?.endOdometer?.toString() ?: "")
    }
    var grossAmountText by remember(existingTrip) {
        mutableStateOf(existingTrip?.grossAmount?.toLong()?.toString() ?: "")
    }
    var commissionAmountText by remember(existingTrip) {
        mutableStateOf(existingTrip?.commissionAmount?.toLong()?.toString() ?: "")
    }
    var selectedPaymentMethod by remember(existingTrip) {
        mutableStateOf(existingTrip?.paymentMethod ?: PaymentMethod.CASH)
    }

    fun updateSuggestedCommission(grossStr: String, platform: PlatformType) {
        val g = grossStr.toDoubleOrNull() ?: 0.0
        if (g > 0.0) {
            val rule = PlatformConfig.getRule(platform)
            val suggested = (g * (rule.referenceCommissionPct / 100.0)).toInt()
            commissionAmountText = suggested.toString()
        }
    }

    val gross = grossAmountText.toDoubleOrNull() ?: 0.0
    val commission = commissionAmountText.toDoubleOrNull() ?: 0.0
    val startOdo = startOdometerText.toIntOrNull()
    val endOdo = endOdometerText.toIntOrNull()
    val net = (gross - commission).coerceAtLeast(0.0)

    val hasDirtyData = if (isEditing) {
        gross != existingTrip.grossAmount ||
                commission != existingTrip.commissionAmount ||
                selectedPlatform != existingTrip.platformType ||
                selectedPaymentMethod != existingTrip.paymentMethod ||
                startOdo != existingTrip.startOdometer ||
                endOdo != existingTrip.endOdometer
    } else {
        grossAmountText.isNotBlank() || commissionAmountText.isNotBlank() || endOdometerText.isNotBlank()
    }
    val isOdometerValid = if (startOdo != null && endOdo != null) endOdo >= startOdo else true
    val isFormValid = gross > 0.0 && commission <= gross && isOdometerValid

    val dialogTitle = if (isEditing) "🏁 Editar Carrera" else "🏁 Nueva Carrera"
    val buttonText = if (isEditing) {
        "GUARDAR CAMBIOS"
    } else if (gross > 0.0) {
        "GUARDAR (+)"
    } else {
        "GUARDAR CARRERA"
    }

    CockpitFloatingScaffold(
        title = dialogTitle,
        headerColor = TripGreenHeader,
        onDismiss = onDismiss,
        confirmButtonText = buttonText,
        confirmButtonColor = StatusGreen,
        isConfirmEnabled = isFormValid,
        onConfirm = {
            if (isFormValid) {
                val dest = when (selectedPaymentMethod) {
                    PaymentMethod.CASH, PaymentMethod.NEQUI -> PaymentDestination.CASH_IN_HAND
                    PaymentMethod.APP_WALLET -> PaymentDestination.APP_RETAINED
                }

                if (isEditing) {
                    onConfirmUpdate(
                        existingTrip.copy(
                            platformType = selectedPlatform,
                            grossAmount = gross,
                            commissionAmount = commission,
                            netAmount = net,
                            paymentMethod = selectedPaymentMethod,
                            paymentDestination = dest,
                            timestamp = selectedDateTimeMillis,
                            startOdometer = startOdo,
                            endOdometer = endOdo
                        )
                    )
                } else {
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
                }
                onDismiss()
            }
        },
        hasDirtyData = hasDirtyData,
        modifier = modifier
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
}
