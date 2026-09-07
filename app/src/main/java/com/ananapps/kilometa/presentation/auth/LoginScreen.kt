// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/auth/LoginScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla de inicio de sesión obligatoria de KiloMeta.
// Presenta el logo institucional, título, lema de cabina y botón táctil prominente
// 'Continuar con Google' (>= 56dp), más sección secundaria desplegable para correo/contraseña.
// ==============================================================================

package com.ananapps.kilometa.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.R
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

// ------------------------------------------------------------------------------
// BLOQUE 1: PANTALLA PRINCIPAL DE AUTENTICACIÓN
// ------------------------------------------------------------------------------
@Composable
fun LoginScreen(
    onSignInWithGoogle: () -> Unit,
    onSignInWithEmail: (email: String, pass: String) -> Unit,
    authError: String? = null,
    isLoading: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showEmailAccordion by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidianBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Institucional KiloMeta
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo KiloMeta",
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(22.dp))
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Título y lema de cabina
            Text(
                text = "KiloMeta",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Tu Copiloto Financiero en Cabina",
                style = MaterialTheme.typography.bodyMedium,
                color = ElectricBluePrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Control total de producción, cuotas y bi-combustible para conductores profesionales.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mensaje de Error Amigable
            if (authError != null) {
                Text(
                    text = "⚠️ $authError",
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Botón Prominente de Google (Touch Target >= 56dp)
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSignInWithGoogle()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                enabled = !isLoading
            ) {
                if (isLoading && !showEmailAccordion) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeepObsidianBackground)
                } else {
                    Text(
                        text = "Continuar con Google",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle para Acordeón de Correo y Contraseña
            TextButton(
                onClick = { showEmailAccordion = !showEmailAccordion }
            ) {
                Text(
                    text = if (showEmailAccordion) "Ocultar acceso con correo ▲" else "O ingresar con correo electrónico ▼",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElectricBluePrimary
                )
            }

            // Sección Secundaria Acordeón
            AnimatedVisibility(
                visible = showEmailAccordion,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DeepObsidianSurface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña (mínimo 6 caracteres)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (email.isNotBlank() && password.length >= 6) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSignInWithEmail(email.trim(), password)
                            }
                        },
                        enabled = email.isNotBlank() && password.length >= 6 && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextPrimary)
                        } else {
                            Text(
                                text = "Iniciar / Registrar Cuenta",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
