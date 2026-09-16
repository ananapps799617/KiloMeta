// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterMaintenanceDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante unificado para Registro y Edición simétrica de Mantenimiento (Orden #041).
// Catálogo de servicios, taller, odómetro y origen de fondos con CockpitFloatingScaffold.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.MaintenanceRecord
import com.ananapps.kilometa.domain.model.MaintenanceType
import com.ananapps.kilometa.domain.model.Place
import com.ananapps.kilometa.domain.model.PlaceCategory
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.domain.model.WalletType
import com.ananapps.kilometa.presentation.common.CockpitFloatingScaffold
import com.ananapps.kilometa.presentation.common.PlaceSelectorField
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

private val DrivvoBrown = Color(0xFF8D6E63)

@Composable
fun RegisterMaintenanceDialog(
    vehicleProfile: VehicleProfile,
    lastKnownOdometer: Int,
    onDismiss: () -> Unit,
    savedPlaces: List<Place> = emptyList(),
    existingMaintenance: MaintenanceRecord? = null,
    onConfirmMaintenance: (
        type: MaintenanceType,
        odometer: Int,
        costCop: Double,
        notes: String?,
        paymentSource: WalletType,
        serviceTitle: String?,
        location: String?,
        timestamp: Long
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onConfirmUpdate: (MaintenanceRecord) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isEditing = existingMaintenance != null
    var selectedType by remember(existingMaintenance) {
        mutableStateOf(existingMaintenance?.type ?: MaintenanceType.OIL_CHANGE)
    }
    var serviceTitle by remember(existingMaintenance) {
        mutableStateOf(existingMaintenance?.serviceTitle ?: "")
    }
    var odoText by remember(existingMaintenance, lastKnownOdometer) {
        val odo = existingMaintenance?.odometer ?: lastKnownOdometer
        mutableStateOf(if (odo > 0) odo.toString() else "")
    }
    var costText by remember(existingMaintenance) {
        mutableStateOf(existingMaintenance?.costCop?.toLong()?.toString() ?: "")
    }
    var locationText by remember(existingMaintenance) {
        mutableStateOf(existingMaintenance?.location ?: "")
    }
    var notesText by remember(existingMaintenance) {
        mutableStateOf(existingMaintenance?.notes ?: "")
    }
    var paymentSource by remember(existingMaintenance) {
        mutableStateOf(existingMaintenance?.paymentSource ?: WalletType.CASH_PHYSICAL)
    }
    var selectedTimestamp by remember(existingMaintenance) {
        mutableStateOf(existingMaintenance?.timestamp ?: System.currentTimeMillis())
    }

    val odoNumeric = odoText.filter { it.isDigit() }.toIntOrNull() ?: lastKnownOdometer
    val costNumeric = costText.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
    val isFormValid = costNumeric > 0.0 && odoNumeric > 0

    val hasDirtyData = if (isEditing) {
        costNumeric != existingMaintenance.costCop ||
                odoNumeric != existingMaintenance.odometer ||
                selectedType != existingMaintenance.type ||
                serviceTitle != (existingMaintenance.serviceTitle ?: "") ||
                locationText != (existingMaintenance.location ?: "") ||
                notesText != (existingMaintenance.notes ?: "") ||
                paymentSource != existingMaintenance.paymentSource
    } else {
        serviceTitle.isNotBlank() || costText.isNotBlank() || notesText.isNotBlank()
    }

    val dialogTitle = if (isEditing) "🔧 Editar Servicio" else "🔧 Registrar Servicio"
    val buttonText = if (isEditing) {
        "GUARDAR CAMBIOS"
    } else if (costNumeric > 0.0) {
        "GUARDAR SERVICIO (- $ %,.0f)".format(Locale.US, costNumeric)
    } else {
        "GUARDAR SERVICIO"
    }

    CockpitFloatingScaffold(
        title = dialogTitle,
        headerColor = DrivvoBrown,
        onDismiss = onDismiss,
        confirmButtonText = buttonText,
        confirmButtonColor = DrivvoBrown,
        isConfirmEnabled = isFormValid,
        onConfirm = {
            if (isFormValid) {
                if (isEditing) {
                    onConfirmUpdate(
                        existingMaintenance.copy(
                            type = selectedType,
                            odometer = odoNumeric,
                            costCop = costNumeric,
                            notes = notesText.ifBlank { null },
                            paymentSource = paymentSource,
                            serviceTitle = serviceTitle.ifBlank { selectedType.displayName },
                            location = locationText.ifBlank { null },
                            timestamp = selectedTimestamp
                        )
                    )
                } else {
                    onConfirmMaintenance(
                        selectedType,
                        odoNumeric,
                        costNumeric,
                        notesText.ifBlank { null },
                        paymentSource,
                        serviceTitle.ifBlank { selectedType.displayName },
                        locationText.ifBlank { null },
                        selectedTimestamp
                    )
                }
                onDismiss()
            }
        },
        hasDirtyData = hasDirtyData,
        modifier = modifier
    ) {
        Surface(modifier = Modifier.fillMaxWidth(), color = DeepObsidianSurfaceVariant, shape = RoundedCornerShape(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = vehicleProfile.vehicleType.icon, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "${vehicleProfile.model} (${vehicleProfile.vehiclePlate})", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                        Text(text = "Mantenimiento preventivo", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                Text(text = "Km %,d".format(lastKnownOdometer), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = StatusGreen)
            }
        }

        DateTimePickerRow(
            selectedDateTimeMillis = selectedTimestamp,
            onDateTimeSelected = { selectedTimestamp = it },
            accentColor = DrivvoBrown
        )

        Text(text = "Tipo de Servicio", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        MaintenanceTypeSelector(
            selectedType = selectedType,
            onTypeSelected = { type ->
                selectedType = type
                if (serviceTitle.isBlank()) serviceTitle = type.displayName
            }
        )

        OutlinedTextField(
            value = serviceTitle,
            onValueChange = { serviceTitle = it },
            label = { Text("Título / Descripción del servicio") },
            placeholder = { Text("Ej: Cambio de Aceite 10W-40") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = odoText,
                onValueChange = { odoText = it.filter { ch -> ch.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Odómetro (km)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = costText,
                onValueChange = { costText = it.filter { ch -> ch.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Text("$", color = DrivvoBrown, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp)) },
                label = { Text("Costo Total ($)") },
                modifier = Modifier.weight(1.2f),
                singleLine = true
            )
        }

        Text(text = "Origen del Dinero", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = paymentSource == WalletType.CASH_PHYSICAL,
                onClick = { paymentSource = WalletType.CASH_PHYSICAL },
                label = { Text("💵 Efectivo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StatusGreen, selectedLabelColor = Color.Black)
            )
            FilterChip(
                selected = paymentSource == WalletType.DIGITAL_WALLET,
                onClick = { paymentSource = WalletType.DIGITAL_WALLET },
                label = { Text("🟣 Digital", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF7B1FA2), selectedLabelColor = Color.White)
            )
        }

        PlaceSelectorField(
            label = "Taller / Serviteca (Opcional)",
            currentValue = locationText,
            places = savedPlaces,
            categoryFilter = listOf(PlaceCategory.WORKSHOP_MECHANIC),
            onPlaceSelected = { place -> locationText = place.name },
            onValueChanged = { locationText = it },
            placeholder = "Ej: Serviteca Texaco Calle 80"
        )

        OutlinedTextField(
            value = notesText,
            onValueChange = { notesText = it },
            label = { Text("Notas técnicas adicionales") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
        )
    }
}
