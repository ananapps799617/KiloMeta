// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterMaintenanceSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet de alta fidelidad Drivvo para registrar servicios mecánicos y
// mantenimientos preventivos. Incorpora Swipe-Down Lockdown, cabecera marrón #8D6E63,
// bloque vehicular con telemetría y selector de origen contable de fondos.
// Regla sagrada: < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.ananapps.kilometa.domain.model.MaintenanceType
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.domain.model.WalletType
import com.ananapps.kilometa.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DrivvoBrown = Color(0xFF8D6E63)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterMaintenanceSheet(
    vehicleProfile: VehicleProfile,
    lastKnownOdometer: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirmMaintenance: (
        type: MaintenanceType,
        odometer: Int,
        costCop: Double,
        notes: String?,
        paymentSource: WalletType,
        serviceTitle: String?,
        location: String?,
        timestamp: Long
    ) -> Unit
) {
    var selectedType by remember { mutableStateOf(MaintenanceType.OIL_CHANGE) }
    var serviceTitle by remember { mutableStateOf("") }
    var odoText by remember(lastKnownOdometer) {
        mutableStateOf(if (lastKnownOdometer > 0) lastKnownOdometer.toString() else "")
    }
    var costText by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var paymentSource by remember { mutableStateOf(WalletType.CASH_PHYSICAL) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasDirtyData = serviceTitle.isNotBlank() || costText.isNotBlank() || notesText.isNotBlank()
    val odoNumeric = odoText.filter { it.isDigit() }.toIntOrNull() ?: lastKnownOdometer
    val costNumeric = costText.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
    val isFormValid = costNumeric > 0.0 && odoNumeric > 0
    val nowFormatted = remember { SimpleDateFormat("dd/MM/yyyy · hh:mm a", Locale.getDefault()).format(Date()) }

    ModalBottomSheet(
        onDismissRequest = { if (hasDirtyData) showDiscardDialog = true else onDismiss() },
        sheetState = sheetState,
        containerColor = DeepObsidianSurface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera Oficial Drivvo Marrón (#8D6E63) con Flecha y "Servicio"
            Surface(modifier = Modifier.fillMaxWidth(), color = DrivvoBrown, shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (hasDirtyData) showDiscardDialog = true else onDismiss() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    Text(text = "Servicio", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White, modifier = Modifier.padding(start = 4.dp))
                }
            }

            // Bloque vehicular y telemetría
            Surface(modifier = Modifier.fillMaxWidth(), color = DeepObsidianSurfaceVariant, shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = vehicleProfile.vehicleType.icon, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "${vehicleProfile.model} (${vehicleProfile.vehiclePlate})", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Text(text = nowFormatted, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                    Text(text = "Km %,d".format(lastKnownOdometer), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = StatusGreen)
                }
            }

            // Selector horizontal de catálogo de servicios
            Text(text = "Tipo de Servicio", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            MaintenanceTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { type ->
                    selectedType = type
                    if (serviceTitle.isBlank()) serviceTitle = type.displayName
                }
            )

            // Título personalizado del servicio
            OutlinedTextField(
                value = serviceTitle,
                onValueChange = { serviceTitle = it },
                label = { Text("Título / Descripción del servicio") },
                placeholder = { Text("Ej: Cambio de Aceite 10W-40") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Odómetro actual y Costo total
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

            // Origen de Fondos (Bolsillo de Pago)
            Text(text = "Origen del Dinero", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = paymentSource == WalletType.CASH_PHYSICAL,
                    onClick = { paymentSource = WalletType.CASH_PHYSICAL },
                    label = { Text("💵 Efectivo Físico", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusGreen,
                        selectedLabelColor = Color.Black
                    )
                )
                FilterChip(
                    selected = paymentSource == WalletType.DIGITAL_WALLET,
                    onClick = { paymentSource = WalletType.DIGITAL_WALLET },
                    label = { Text("🟣 Billetera Digital", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF7B1FA2),
                        selectedLabelColor = Color.White
                    )
                )
            }

            // Taller / Serviteca y Notas
            OutlinedTextField(
                value = locationText,
                onValueChange = { locationText = it },
                label = { Text("Taller / Locación (Opcional)") },
                placeholder = { Text("Ej: Serviteca Texaco Calle 80") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notas técnicas adicionales") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            // Botón Único Principal GUARDAR
            Button(
                onClick = {
                    if (isFormValid) {
                        onConfirmMaintenance(
                            selectedType,
                            odoNumeric,
                            costNumeric,
                            notesText.ifBlank { null },
                            paymentSource,
                            serviceTitle.ifBlank { selectedType.displayName },
                            locationText.ifBlank { null },
                            System.currentTimeMillis()
                        )
                        onDismiss()
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DrivvoBrown,
                    disabledContainerColor = DrivvoBrown.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = if (costNumeric > 0.0) "GUARDAR SERVICIO (- $ %,.0f)".format(costNumeric) else "GUARDAR SERVICIO",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            title = "¿Descartar Servicio?",
            message = "Tienes datos digitados sin guardar. ¿Deseas salir y perder los cambios?",
            onConfirmDiscard = {
                showDiscardDialog = false
                onDismiss()
            },
            onDismissRequest = { showDiscardDialog = false }
        )
    }
}
