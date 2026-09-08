// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/DriverProfileScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla de Perfil de Conductor y Vehículo desacoplada en secciones modulares.
// Implementa navegación reactiva post-guardado, reseteo de clave y catálogo asistido.
// Regla inquebrantable: Archivo pedagógico < 280 líneas.
// ==============================================================================

package com.ananapps.kilometa.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ananapps.kilometa.domain.model.DriverProfile
import com.ananapps.kilometa.domain.model.Gender
import com.ananapps.kilometa.domain.model.PropulsionType
import com.ananapps.kilometa.domain.model.QuotaPeriodicity
import com.ananapps.kilometa.domain.model.VehicleProfile
import com.ananapps.kilometa.domain.model.VehicleTenure
import com.ananapps.kilometa.presentation.profile.components.DriverPersonalSection
import com.ananapps.kilometa.presentation.profile.components.PasswordResetDialog
import com.ananapps.kilometa.presentation.profile.components.VehicleConfigSection
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DriverProfileScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: DriverProfileViewModel = hiltViewModel()
) {
    val initialDriver by viewModel.driverProfile.collectAsState()
    val initialVehicle by viewModel.vehicleProfile.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostStateState() }
    val scope = rememberCoroutineScope()

    var showPasswordResetDialog by remember { mutableStateOf(false) }

    // Estados locales para edición fluida
    var driverName by remember(initialDriver.driverName) { mutableStateOf(initialDriver.driverName) }
    var phone by remember(initialDriver.phone) { mutableStateOf(initialDriver.phone) }
    var gender by remember(initialDriver.gender) { mutableStateOf(initialDriver.gender) }

    var vehiclePlate by remember(initialVehicle.vehiclePlate) { mutableStateOf(initialVehicle.vehiclePlate) }
    var brand by remember(initialVehicle.brand) { mutableStateOf(initialVehicle.brand) }
    var model by remember(initialVehicle.model) { mutableStateOf(initialVehicle.model) }
    var propulsionType by remember(initialVehicle.propulsionType) { mutableStateOf(initialVehicle.propulsionType) }
    var tenureType by remember(initialVehicle.tenureType) { mutableStateOf(initialVehicle.tenureType) }
    var quotaAmount by remember(initialVehicle.quotaAmount) { mutableDoubleStateOf(initialVehicle.quotaAmount) }
    var quotaPeriodicity by remember(initialVehicle.quotaPeriodicity) { mutableStateOf(initialVehicle.quotaPeriodicity) }
    var workDaysWeek by remember(initialVehicle.workDaysWeek) { mutableIntStateOf(initialVehicle.workDaysWeek) }

    // Retorno reactivo post-guardado exitoso
    LaunchedEffect(isSaved) {
        if (isSaved) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.resetSavedNotification()
            delay(300)
            onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = DeepObsidianBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta 1: Conductor
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DriverPersonalSection(
                    driverName = driverName,
                    onDriverNameChange = { driverName = it },
                    email = initialDriver.email.ifBlank { "conductor@kilometa.co" },
                    phone = phone,
                    onPhoneChange = { phone = it },
                    gender = gender,
                    onGenderChange = { gender = it },
                    onRequestPasswordReset = { showPasswordResetDialog = true },
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Tarjeta 2: Vehículo
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                VehicleConfigSection(
                    vehiclePlate = vehiclePlate,
                    onVehiclePlateChange = { vehiclePlate = it },
                    brand = brand,
                    onBrandChange = { brand = it },
                    model = model,
                    onModelChange = { model = it },
                    propulsionType = propulsionType,
                    onPropulsionChange = { propulsionType = it },
                    tenureType = tenureType,
                    onTenureChange = { tenureType = it },
                    quotaAmount = quotaAmount,
                    onQuotaAmountChange = { quotaAmount = it },
                    quotaPeriodicity = quotaPeriodicity,
                    onQuotaPeriodicityChange = { quotaPeriodicity = it },
                    workDaysWeek = workDaysWeek,
                    onWorkDaysWeekChange = { workDaysWeek = it },
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Botón Guardar Perfil (56dp Touch Target)
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val updatedDriver = initialDriver.copy(
                        driverName = driverName.trim(),
                        phone = phone.trim(),
                        gender = gender,
                        vehiclePlate = vehiclePlate.trim().uppercase(),
                        propulsionType = propulsionType
                    )
                    val updatedVehicle = initialVehicle.copy(
                        vehiclePlate = vehiclePlate.trim().uppercase(),
                        brand = brand.trim(),
                        model = model.trim(),
                        propulsionType = propulsionType,
                        tenureType = tenureType,
                        quotaAmount = quotaAmount,
                        quotaPeriodicity = quotaPeriodicity,
                        workDaysWeek = workDaysWeek
                    )
                    viewModel.saveFullProfile(updatedDriver, updatedVehicle)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "💾 Guardar Perfil y Regresar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepObsidianBackground
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showPasswordResetDialog) {
        PasswordResetDialog(
            initialEmail = initialDriver.email.ifBlank { "conductor@kilometa.co" },
            onConfirm = { emailToReset ->
                showPasswordResetDialog = false
                viewModel.sendPasswordResetEmail(emailToReset) { success, errorMsg ->
                    scope.launch {
                        if (success) {
                            snackbarHostState.showSnackbar("Enlace de restablecimiento enviado a $emailToReset")
                        } else {
                            snackbarHostState.showSnackbar(errorMsg ?: "No fue posible enviar el enlace.")
                        }
                    }
                }
            },
            onDismiss = { showPasswordResetDialog = false }
        )
    }
}

// Helper para instanciar el estado de Snackbar
@Composable
private fun SnackbarHostStateState(): SnackbarHostState = remember { SnackbarHostState() }
