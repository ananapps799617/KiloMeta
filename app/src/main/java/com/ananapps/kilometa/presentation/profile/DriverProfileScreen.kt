// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/DriverProfileScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla de perfil vehicular y conductor. Soporta modo Onboarding (bienvenida
// guiada obligatoria) y modo edición regular, con selector de día/hora de pago
// y panel de desarrollo de reseteo exclusivo para la cuenta del Gerente General.
// ==============================================================================

package com.ananapps.kilometa.presentation.profile

import androidx.compose.foundation.border
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
import com.ananapps.kilometa.presentation.profile.components.DevTestingSection
import com.ananapps.kilometa.presentation.profile.components.DriverPersonalSection
import com.ananapps.kilometa.presentation.profile.components.PasswordResetDialog
import com.ananapps.kilometa.presentation.profile.components.VehicleConfigSection
import com.ananapps.kilometa.presentation.profile.components.VehicleQuotaSection
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DriverProfileScreen(
    onNavigateBack: () -> Unit = {},
    isOnboardingMode: Boolean = false,
    userEmail: String = "",
    viewModel: DriverProfileViewModel = hiltViewModel()
) {
    val initialDriver by viewModel.driverProfile.collectAsState()
    val initialVehicle by viewModel.vehicleProfile.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
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
    var quotaCutoffDay by remember(initialVehicle.quotaCutoffDay) { mutableIntStateOf(initialVehicle.quotaCutoffDay) }
    var quotaCutoffHour by remember(initialVehicle.quotaCutoffHour) { mutableIntStateOf(initialVehicle.quotaCutoffHour) }

    // Retorno reactivo post-guardado exitoso
    LaunchedEffect(isSaved) {
        if (isSaved) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.resetSavedNotification()
            delay(250)
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
            // Banner de Bienvenida en modo Onboarding
            if (isOnboardingMode) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, StatusGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "🚕 ¡Bienvenido a KiloMeta, Conductor!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = StatusGreen
                        )
                        Text(
                            text = "Calibremos tu identidad y vehículo para que el Semáforo de Rentabilidad y tus metas de cuota diaria sean 100% exactos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Tarjeta 1: Conductor
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DriverPersonalSection(
                    driverName = driverName,
                    onDriverNameChange = { driverName = it },
                    email = initialDriver.email.ifBlank { userEmail.ifBlank { "conductor@kilometa.co" } },
                    phone = phone,
                    onPhoneChange = { phone = it },
                    gender = gender,
                    onGenderChange = { gender = it },
                    onRequestPasswordReset = { showPasswordResetDialog = true },
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Tarjeta 2: Vehículo (Identidad y Propulsión)
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
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Tarjeta 3: Cuota Financiera y Corte de Entrega
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepObsidianSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                VehicleQuotaSection(
                    tenureType = tenureType,
                    quotaAmount = quotaAmount,
                    onQuotaAmountChange = { quotaAmount = it },
                    quotaPeriodicity = quotaPeriodicity,
                    onQuotaPeriodicityChange = { quotaPeriodicity = it },
                    workDaysWeek = workDaysWeek,
                    onWorkDaysWeekChange = { workDaysWeek = it },
                    quotaCutoffDay = quotaCutoffDay,
                    onQuotaCutoffDayChange = { quotaCutoffDay = it },
                    quotaCutoffHour = quotaCutoffHour,
                    onQuotaCutoffHourChange = { quotaCutoffHour = it },
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Tarjeta 4: Zona de Desarrollo (Solo Gerencia General)
            DevTestingSection(
                userEmail = userEmail.ifBlank { initialDriver.email },
                onResetData = {
                    viewModel.resetProfileForTesting {
                        scope.launch {
                            snackbarHostState.showSnackbar("Onboarding y datos locales reiniciados.")
                            onNavigateBack()
                        }
                    }
                }
            )

            // Botón Principal
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val updatedDriver = initialDriver.copy(
                        driverName = driverName.trim(),
                        phone = phone.trim(),
                        gender = gender,
                        vehiclePlate = vehiclePlate.trim().uppercase(),
                        propulsionType = propulsionType,
                        isProfileCompleted = true
                    )
                    val updatedVehicle = initialVehicle.copy(
                        vehiclePlate = vehiclePlate.trim().uppercase(),
                        brand = brand.trim(),
                        model = model.trim(),
                        propulsionType = propulsionType,
                        tenureType = tenureType,
                        quotaAmount = quotaAmount,
                        quotaPeriodicity = quotaPeriodicity,
                        workDaysWeek = workDaysWeek,
                        quotaCutoffDay = quotaCutoffDay,
                        quotaCutoffHour = quotaCutoffHour
                    )
                    viewModel.saveFullProfile(updatedDriver, updatedVehicle)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isOnboardingMode) "🏁 Finalizar Configuración y Entrar a Cabina" else "💾 Guardar Perfil y Regresar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepObsidianBackground
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showPasswordResetDialog) {
        PasswordResetDialog(
            initialEmail = initialDriver.email.ifBlank { userEmail.ifBlank { "conductor@kilometa.co" } },
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
