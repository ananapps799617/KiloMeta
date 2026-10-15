// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/auth/components/PasswordResetDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal para recuperación de contraseña vía Firebase Auth en cabina.
// Permite al conductor recibir el enlace oficial de restablecimiento sin soporte técnico.
// ==============================================================================

package com.ananapps.kilometa.presentation.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
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
import com.ananapps.kilometa.presentation.theme.*
import kotlinx.coroutines.launch

@Composable
fun PasswordResetDialog(
    initialEmail: String = "",
    onDismiss: () -> Unit,
    onSendResetEmail: suspend (String) -> Result<Unit>,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf(initialEmail) }
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = DeepObsidianSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🔑 Recuperar Contraseña",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    fontSize = 18.sp
                )

                Text(
                    text = "Ingresa tu correo electrónico registrado y te enviaremos un enlace oficial de Firebase para restablecer tu acceso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                if (resultMessage != null) {
                    Surface(
                        color = if (isSuccess) StatusGreenContainer else StatusRedContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = resultMessage.orEmpty(),
                            color = if (isSuccess) StatusGreen else StatusRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (!isSuccess) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; resultMessage = null },
                        label = { Text("Correo electrónico", color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = EmeraldAccent) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldAccent,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                if (email.isBlank() || !email.contains("@")) {
                                    resultMessage = "Por favor ingresa un correo válido."
                                    isSuccess = false
                                    return@Button
                                }
                                isLoading = true
                                resultMessage = null
                                scope.launch {
                                    val res = onSendResetEmail(email.trim())
                                    isLoading = false
                                    if (res.isSuccess) {
                                        isSuccess = true
                                        resultMessage = "¡Enlace enviado! Revisa tu bandeja de entrada o spam para restablecer tu contraseña."
                                    } else {
                                        isSuccess = false
                                        resultMessage = res.exceptionOrNull()?.localizedMessage ?: "Error al enviar correo de recuperación."
                                    }
                                }
                            },
                            enabled = !isLoading && email.isNotBlank(),
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Enviar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Entendido", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
