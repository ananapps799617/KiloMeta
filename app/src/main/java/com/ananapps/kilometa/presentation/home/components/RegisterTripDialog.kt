// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterTripDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante unificado para Registro y Edición simétrica de Carreras (Orden #041/#050).
// Integra telemetría completa (distancia y duración) para cálculo del semáforo de rentabilidad (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        endOdometer: Int?,
        distanceKm: Double?,
        durationMinutes: Int?,
        platformPromoDiscount: Double
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> },
    onConfirmUpdate: (Trip) -> Unit = {},
    initialTimestamp: Long? = null,
    modifier: Modifier = Modifier
) {
    val isEditing = existingTrip != null
    val initialPlatform = existingTrip?.platformType ?: PlatformType.UBER
    val initialTime = existingTrip?.timestamp ?: initialTimestamp ?: System.currentTimeMillis()
    val initialGross = existingTrip?.grossAmount?.toLong()?.toString() ?: ""
    val initialIsBonus = existingTrip?.platformPromoDiscount?.let { it > 0.0 } ?: false
    val initialComm = existingTrip?.let {
        if (it.platformPromoDiscount > 0.0) {
            it.platformPromoDiscount.toLong().toString()
        } else {
            it.commissionAmount.takeIf { c -> c > 0.0 }?.toLong()?.toString() ?: ""
        }
    } ?: ""
    val initialPayment = existingTrip?.paymentMethod ?: PaymentMethod.CASH

    val initialDistance = existingTrip?.distanceKm?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: ""
    val initialDuration = existingTrip?.durationMinutes?.toString() ?: ""

    var selectedPlatform by remember(existingTrip) { mutableStateOf(initialPlatform) }
    var selectedDateTimeMillis by remember(existingTrip, initialTimestamp) { mutableStateOf(initialTime) }
    var grossAmountText by remember(existingTrip) { mutableStateOf(initialGross) }
    var commissionAmountText by remember(existingTrip) { mutableStateOf(initialComm) }
    var isBonus by remember(existingTrip) { mutableStateOf(initialIsBonus) }
    var selectedPaymentMethod by remember(existingTrip) { mutableStateOf(initialPayment) }
    var distanceKmText by remember(existingTrip) { mutableStateOf(initialDistance) }
    var durationMinutesText by remember(existingTrip) { mutableStateOf(initialDuration) }

    fun updateSuggestedCommission(grossStr: String, platform: PlatformType) {
        val g = grossStr.toDoubleOrNull() ?: 0.0
        if (g > 0.0) {
            val rule = PlatformConfig.getRule(platform)
            val suggested = (g * (rule.referenceCommissionPct / 100.0)).toInt()
            commissionAmountText = suggested.toString()
        }
    }

    val gross = grossAmountText.toDoubleOrNull() ?: 0.0
    val appPart = commissionAmountText.toDoubleOrNull() ?: 0.0
    val net = if (isBonus) (gross + appPart) else (gross - appPart).coerceAtLeast(0.0)
    val commission = if (isBonus) 0.0 else appPart
    val promoDiscount = if (isBonus) appPart else 0.0
    val parsedDist = distanceKmText.replace(',', '.').toDoubleOrNull()
    val parsedDur = durationMinutesText.toIntOrNull()

    val hasDirtyData = if (isEditing) {
        gross != existingTrip.grossAmount ||
                commission != existingTrip.commissionAmount ||
                promoDiscount != existingTrip.platformPromoDiscount ||
                selectedPlatform != existingTrip.platformType ||
                selectedPaymentMethod != existingTrip.paymentMethod ||
                distanceKmText != initialDistance ||
                durationMinutesText != initialDuration
    } else {
        grossAmountText.isNotBlank() || commissionAmountText.isNotBlank() || distanceKmText.isNotBlank()
    }
    val isFormValid = gross > 0.0 && (isBonus || appPart <= gross)

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
                            distanceKm = parsedDist ?: existingTrip.distanceKm,
                            durationMinutes = parsedDur ?: existingTrip.durationMinutes,
                            platformPromoDiscount = promoDiscount
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
                        null,
                        null,
                        parsedDist,
                        parsedDur,
                        promoDiscount
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
            distanceKmText = distanceKmText,
            onDistanceKmChange = { distanceKmText = it },
            durationMinutesText = durationMinutesText,
            onDurationMinutesChange = { durationMinutesText = it }
        )

        TripFinancialSection(
            grossAmountText = grossAmountText,
            onGrossAmountChange = {
                grossAmountText = it
                if (!isBonus) updateSuggestedCommission(it, selectedPlatform)
            },
            commissionAmountText = commissionAmountText,
            onCommissionAmountChange = { commissionAmountText = it },
            selectedPaymentMethod = selectedPaymentMethod,
            onPaymentMethodSelected = {
                selectedPaymentMethod = it
                if (!isBonus && commissionAmountText.isBlank() && grossAmountText.isNotBlank()) {
                    updateSuggestedCommission(grossAmountText, selectedPlatform)
                }
            },
            platformPromoDiscount = promoDiscount,
            isBonus = isBonus,
            onIsBonusToggle = { isBonus = !isBonus }
        )
    }
}
