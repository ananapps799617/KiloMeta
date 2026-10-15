// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/auth/components/LoginFormSection.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Formulario expuesto de autenticación vehicular de Nivel 1.
// Integra campos de correo con borrado rápido, contraseña con toggle de visibilidad,
// checkbox de recuerdo de sesión, recuperación de contraseña, botón esmeralda y Google (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.auth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun LoginFormSection(
    onSignInWithEmail: (email: String, pass: String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onForgotPasswordClick: (email: String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var rememberSession by rememberSaveable { mutableStateOf(true) }
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }
    var localValidationMessage by remember { mutableStateOf<String?>(null) }

    fun executeAuth() {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            localValidationMessage = "Ingresa un correo electrónico válido."
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            return
        }
        if (password.length < 6) {
            localValidationMessage = "La contraseña debe tener al menos 6 caracteres."
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            return
        }
        localValidationMessage = null
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        focusManager.clearFocus()
        onSignInWithEmail(trimmedEmail, password)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Mensaje de validación local
        if (localValidationMessage != null) {
            Surface(
                color = StatusRedContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚠️ ${localValidationMessage.orEmpty()}",
                    color = StatusRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EmeraldAccent, unfocusedBorderColor = ObsidianBorder,
            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
            focusedContainerColor = DeepObsidianSurface, unfocusedContainerColor = DeepObsidianSurface
        )

        // 2. Campo de Correo Electrónico
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; localValidationMessage = null },
            label = { Text("Correo electrónico", color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Correo", tint = EmeraldAccent)
            },
            trailingIcon = {
                if (email.isNotEmpty()) {
                    IconButton(onClick = { email = ""; localValidationMessage = null }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar correo", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp)
        )

        // 3. Campo de Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; localValidationMessage = null },
            label = { Text("Contraseña", color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Contraseña", tint = EmeraldAccent)
            },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Text(text = if (isPasswordVisible) "👁️" else "🔒", fontSize = 18.sp)
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { executeAuth() }),
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp)
        )

        // 4. Opciones auxiliares: Recordar sesión y ¿Olvidaste tu contraseña?
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Checkbox(
                    checked = rememberSession,
                    onCheckedChange = { rememberSession = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = EmeraldPrimary,
                        checkmarkColor = Color.White,
                        uncheckedColor = ObsidianBorder
                    )
                )
                Text(
                    text = "Recordar mi sesión",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (!isRegisterMode) {
                TextButton(
                    onClick = { onForgotPasswordClick(email.trim()) },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = EmeraldAccent
                    )
                }
            }
        }

        // 5. Botón Principal Verde Esmeralda
        Button(
            onClick = { executeAuth() },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                disabledContainerColor = EmeraldContainer,
                disabledContentColor = TextSecondary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.5.dp)
            } else {
                Text(
                    text = if (isRegisterMode) "✨ Crear cuenta" else "➔ Iniciar sesión",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                )
            }
        }

        // 6. Divisor con texto
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = ObsidianBorder.copy(alpha = 0.6f))
            Text(
                text = "o continúa con",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = ObsidianBorder.copy(alpha = 0.6f))
        }

        // 7. Botón de Google Oficial
        GoogleSignInButton(
            onClick = onGoogleSignInClick,
            isLoading = isLoading
        )

        // 8. Alternar entre Iniciar Sesión y Registrarse
        TextButton(
            onClick = {
                isRegisterMode = !isRegisterMode
                localValidationMessage = null
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isRegisterMode)
                    "¿Ya tienes una cuenta? Inicia sesión aquí"
                else
                    "¿No tienes una cuenta? Regístrate aquí",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = EmeraldAccent,
                textAlign = TextAlign.Center
            )
        }
    }
}
