// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RegisterMaintenanceDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante centrado de 3 capas para registro de servicios y mantenimientos (Orden #037).
// Cabecera marrón Drivvo fija, cuerpo scrollable para catálogo de servicios y odómetro,
// y botón fijo de 56dp en el pie inamovible.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ananapps.kilometa.domain.model.MaintenanceType
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.domain.model.WalletType
import com.ananapps.kilometa.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DrivvoBrown = Color(0xFF8D6E63)

@Composable
fun RegisterMaintenanceDialog(
    vehicleProfile: VehicleProfile,
    lastKnownOdometer: Int,
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
    ) -> Unit,
    modifier: Modifier = Modifier
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
                    color = DrivvoBrown,
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
                                text = "🔧 Registrar Servicio",
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
                                    Text(text = nowFormatted, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Text(text = "Km %,d".format(lastKnownOdometer), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = StatusGreen)
                        }
                    }

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
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CAPA 3: BOTONERA DE ACCIÓN FIJA (56dp)
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
