// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/DriverProfileScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Orquestador maestro del Cockpit Flow (Onboarding) y Edición de Perfil en 3 pasos.
// Implementa protección estricta de Insets seguros (statusBarsPadding, navigationBarsPadding)
// garantizando cero solapamientos visuales y preservando el 100% de integridad de datos.
// ==============================================================================

package com.ananapps.kilometa.presentation.profile

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ananapps.kilometa.presentation.profile.components.OnboardingNavControls
import com.ananapps.kilometa.presentation.profile.components.OnboardingStepIndicator
import com.ananapps.kilometa.presentation.profile.components.PasswordResetDialog
import com.ananapps.kilometa.presentation.profile.components.ProfileStepContent
import com.ananapps.kilometa.presentation.profile.components.ProfileTopBar
import com.ananapps.kilometa.presentation.profile.components.rememberDriverProfileFormState
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.StatusGreen
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

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    var showPasswordResetDialog by remember { mutableStateOf(false) }

    val formState = rememberDriverProfileFormState(initialDriver, initialVehicle)

    LaunchedEffect(isSaved) {
        if (isSaved) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(context, "Perfil guardado exitosamente", Toast.LENGTH_SHORT).show()
            viewModel.resetSavedNotification()
            delay(250)
            onNavigateBack()
        }
    }

    val onCommitSave = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val updatedDriver = formState.toUpdatedDriver(initialDriver)
        val updatedVehicle = formState.toUpdatedVehicle(initialVehicle)
        viewModel.saveFullProfile(updatedDriver, updatedVehicle)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = DeepObsidianBackground,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Colchón superior seguro para evitar colisión con reloj, batería o wifi
                Spacer(modifier = Modifier.height(8.dp))

                ProfileTopBar(
                    isOnboardingMode = isOnboardingMode,
                    onNavigateBack = onNavigateBack
                )

                OnboardingStepIndicator(
                    currentStep = currentStep,
                    onStepClick = { target -> currentStep = target }
                )

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "stepper_content_transition"
                ) { step ->
                    ProfileStepContent(
                        step = step,
                        state = formState,
                        email = initialDriver.email.ifBlank { userEmail.ifBlank { "conductor@kilometa.co" } },
                        onRequestPasswordReset = { showPasswordResetDialog = true },
                        onResetData = {
                            viewModel.resetProfileForTesting {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Datos reiniciados con éxito.")
                                    onNavigateBack()
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Barra inferior en Thumb Zone
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                if (isOnboardingMode) {
                    val isNextValid = when (currentStep) {
                        0 -> formState.driverName.isNotBlank()
                        1 -> formState.vehiclePlate.isNotBlank()
                        else -> true
                    }
                    OnboardingNavControls(
                        currentStep = currentStep,
                        onBackClick = { if (currentStep > 0) currentStep-- },
                        onNextClick = { if (currentStep < 2) currentStep++ },
                        onFinishClick = onCommitSave,
                        isNextEnabled = isNextValid
                    )
                } else {
                    Button(
                        onClick = onCommitSave,
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
                }
            }
        }
    }

    if (showPasswordResetDialog) {
        PasswordResetDialog(
            initialEmail = initialDriver.email.ifBlank { userEmail.ifBlank { "conductor@kilometa.co" } },
            onConfirm = { emailToReset ->
                showPasswordResetDialog = false
                viewModel.sendPasswordResetEmail(emailToReset) { success, errorMsg ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (success) "Enlace de restablecimiento enviado a $emailToReset"
                            else errorMsg ?: "No fue posible enviar el enlace."
                        )
                    }
                }
            },
            onDismiss = { showPasswordResetDialog = false }
        )
    }
}
