// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/auth/LoginScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla de bienvenida y autenticación estilo "Tesla Cockpit" (Orden #052).
// Integra halo de encendido, ticker dinámico de telemetría y botón Google oficial (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.R
import com.ananapps.kilometa.presentation.auth.components.EmailLoginSection
import com.ananapps.kilometa.presentation.auth.components.GoogleSignInButton
import com.ananapps.kilometa.presentation.auth.components.LoginCockpitHeader
import com.ananapps.kilometa.presentation.auth.components.LoginDynamicTicker
import com.ananapps.kilometa.presentation.theme.CyanSavings
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.TextSecondary
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onSignInWithGoogleToken: (idToken: String) -> Unit,
    onGoogleSignInError: (errorMessage: String) -> Unit,
    onSignInWithEmail: (email: String, pass: String) -> Unit,
    authError: String? = null,
    isLoading: Boolean = false
) {
    val context = LocalContext.current

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
            if (e.statusCode != 12501 && e.statusCode != 16) {
                onGoogleSignInError("Error Google (${e.statusCode}): ${e.localizedMessage ?: "Fallo de conexión"}")
            }
        } catch (e: Exception) {
            onGoogleSignInError("Error Google: ${e.localizedMessage ?: e.message}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1722),
                        DeepObsidianBackground,
                        DeepObsidianBackground
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Cabecera Cockpit con Halo y Logo Start/Stop
            LoginCockpitHeader()

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Cápsula Dinámica Glassmorphism de Telemetría
            LoginDynamicTicker()

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Alerta de error si aplica
            if (authError != null) {
                Text(
                    text = "⚠️ $authError",
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // 4. Botón de Google Oficial (56dp Thumb Zone)
            GoogleSignInButton(
                onClick = {
                    val webClientId = runCatching {
                        context.getString(R.string.default_web_client_id)
                    }.getOrNull()

                    if (webClientId.isNullOrBlank()) {
                        onGoogleSignInError("Configuración de Google no encontrada en el dispositivo.")
                        return@GoogleSignInButton
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
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Acceso tradicional por correo electrónico
            EmailLoginSection(
                onSignInWithEmail = onSignInWithEmail,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 6. Footer de Confianza y Ciberseguridad
            Text(
                text = "🔒 Cifrado Local · Datos Protegidos en Cabina",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
