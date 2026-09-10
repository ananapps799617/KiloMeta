// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/auth/LoginScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla de inicio de sesión obligatoria de KiloMeta.
// Integra Google Sign-In nativo (play-services-auth) con obtención real de idToken
// y sección modular para autenticación mediante correo/contraseña.
// ==============================================================================

package com.ananapps.kilometa.presentation.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.R
import com.ananapps.kilometa.presentation.auth.components.EmailLoginSection
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

// ------------------------------------------------------------------------------
// BLOQUE 1: PANTALLA PRINCIPAL DE AUTENTICACIÓN
// ------------------------------------------------------------------------------
@Composable
fun LoginScreen(
    onSignInWithGoogleToken: (idToken: String) -> Unit,
    onGoogleSignInError: (errorMessage: String) -> Unit,
    onSignInWithEmail: (email: String, pass: String) -> Unit,
    authError: String? = null,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Launcher oficial de Play Services para Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrBlank()) {
                onSignInWithGoogleToken(idToken)
            } else {
                onGoogleSignInError("No se recibió el token de identidad de Google.")
            }
        } catch (e: ApiException) {
            // Códigos 12501 y 16 corresponden a cancelación voluntaria por el usuario
            if (e.statusCode != 12501 && e.statusCode != 16) {
                onGoogleSignInError("Error al conectar con Google (${e.statusCode}): ${e.localizedMessage ?: "Fallo de conexión"}")
            }
        } catch (e: Exception) {
            onGoogleSignInError("Error Google: ${e.localizedMessage ?: e.message}")
        }
    }

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
                    val webClientId = runCatching {
                        context.getString(R.string.default_web_client_id)
                    }.getOrNull()

                    if (webClientId.isNullOrBlank()) {
                        onGoogleSignInError("Configuración de Google no encontrada en el dispositivo.")
                        return@Button
                    }

                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(webClientId)
                        .requestEmail()
                        .build()

                    GoogleSignIn.getClient(context, gso).apply {
                        signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(signInIntent)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                enabled = !isLoading
            ) {
                if (isLoading) {
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

            // Subcomponente modular de autenticación por correo electrónico
            EmailLoginSection(
                onSignInWithEmail = onSignInWithEmail,
                isLoading = isLoading
            )
        }
    }
}
