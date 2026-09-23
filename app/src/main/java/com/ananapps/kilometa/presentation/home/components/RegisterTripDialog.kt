// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterTripDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante unificado para Registro y Edición simétrica de Carreras (Orden #041).
// Utiliza CockpitFloatingScaffold y subcomponentes modulares garantizando paridad total y < 280 LOC.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.background
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
    scannedReceipt: ScannedTripReceipt? = null,
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
    val initialPlatform = existingTrip?.platformType ?: scannedReceipt?.platform ?: PlatformType.UBER
    val initialTime = existingTrip?.timestamp ?: scannedReceipt?.timestamp ?: initialTimestamp ?: System.currentTimeMillis()
    val initialGross = existingTrip?.grossAmount?.toLong()?.toString()
        ?: scannedReceipt?.grossAmount?.takeIf { it > 0.0 }?.toLong()?.toString()
        ?: ""
    val initialComm = existingTrip?.commissionAmount?.toLong()?.toString()
        ?: scannedReceipt?.commissionAmount?.toLong()?.toString()
        ?: ""
    val initialPayment = existingTrip?.paymentMethod ?: scannedReceipt?.paymentMethod ?: PaymentMethod.CASH

    var selectedPlatform by remember(existingTrip, scannedReceipt) { mutableStateOf(initialPlatform) }
    var selectedDateTimeMillis by remember(existingTrip, scannedReceipt, initialTimestamp) { mutableStateOf(initialTime) }
    var grossAmountText by remember(existingTrip, scannedReceipt) { mutableStateOf(initialGross) }
    var commissionAmountText by remember(existingTrip, scannedReceipt) { mutableStateOf(initialComm) }
    var selectedPaymentMethod by remember(existingTrip, scannedReceipt) { mutableStateOf(initialPayment) }

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
    val net = (gross - commission).coerceAtLeast(0.0)

    val hasDirtyData = if (isEditing) {
        gross != existingTrip.grossAmount ||
                commission != existingTrip.commissionAmount ||
                selectedPlatform != existingTrip.platformType ||
                selectedPaymentMethod != existingTrip.paymentMethod
    } else {
        grossAmountText.isNotBlank() || commissionAmountText.isNotBlank()
    }
    val isFormValid = gross > 0.0 && commission <= gross

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
                            distanceKm = existingTrip.distanceKm ?: scannedReceipt?.distanceKm,
                            durationMinutes = existingTrip.durationMinutes ?: scannedReceipt?.durationMinutes,
                            platformPromoDiscount = if (existingTrip.platformPromoDiscount > 0) existingTrip.platformPromoDiscount else (scannedReceipt?.platformPromoDiscount ?: 0.0)
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
                        scannedReceipt?.distanceKm,
                        scannedReceipt?.durationMinutes,
                        scannedReceipt?.platformPromoDiscount ?: 0.0
                    )
                }
                onDismiss()
            }
        },
        hasDirtyData = hasDirtyData,
        modifier = modifier
    ) {
        if (scannedReceipt != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StatusGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "📸 Auto-completado (${scannedReceipt.confidenceSummary ?: scannedReceipt.platform.displayName})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = StatusGreen
                )
            }
        }

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
            onDateTimeSelected = { selectedDateTimeMillis = it }
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
