// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/auth/LoginScreen.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pantalla de bienvenida y autenticación vehicular "Cockpit Dark" con fondo automotriz nocturno,
// iluminación esmeralda, formulario expuesto, recuperación por correo y pilares de confianza (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.R
import com.ananapps.kilometa.presentation.auth.components.LoginCockpitHeader
import com.ananapps.kilometa.presentation.auth.components.LoginFormSection
import com.ananapps.kilometa.presentation.auth.components.LoginTrustPillars
import com.ananapps.kilometa.presentation.auth.components.PasswordResetDialog
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.StatusRed
import com.ananapps.kilometa.presentation.theme.StatusRedContainer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onSignInWithGoogleToken: (idToken: String) -> Unit,
    onGoogleSignInError: (errorMessage: String) -> Unit,
    onSignInWithEmail: (email: String, pass: String) -> Unit,
    onSendPasswordReset: suspend (email: String) -> Result<Unit> = { Result.success(Unit) },
    authError: String? = null,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var resetDialogEmail by remember { mutableStateOf("") }

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

    val launchGoogleSignIn: () -> Unit = {
        val webClientId = runCatching {
            context.getString(R.string.default_web_client_id)
        }.getOrNull()

        if (webClientId.isNullOrBlank()) {
            onGoogleSignInError("Configuración de Google no encontrada en el dispositivo.")
        } else {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            GoogleSignIn.getClient(context, gso).apply {
                signOut().addOnCompleteListener {
                    googleSignInLauncher.launch(signInIntent)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidianBackground)
    ) {
        // 1. Imagen de fondo automotriz con estela esmeralda (Cabina Nocturna Limpia)
        Image(
            painter = painterResource(id = R.drawable.bg_cockpit_car_top),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .align(Alignment.TopCenter)
        )

        // Degradado calibrado: despejado en montañas/auto y transición suave hacia los inputs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.62f to Color.Transparent,
                        0.85f to DeepObsidianBackground.copy(alpha = 0.65f),
                        1.0f to DeepObsidianBackground
                    )
                )
        )

        // 2. Contenido desplazable principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Cabecera con Logotipo Oficial Soberano y Bienvenida
            LoginCockpitHeader()

            Spacer(modifier = Modifier.height(18.dp))

            // Banner de Alerta de Error de Firebase si aplica
            if (authError != null) {
                Surface(
                    color = StatusRedContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "⚠️ $authError",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            // Formulario Expuesto de Acceso y Botones
            LoginFormSection(
                onSignInWithEmail = onSignInWithEmail,
                onGoogleSignInClick = launchGoogleSignIn,
                onForgotPasswordClick = { email ->
                    resetDialogEmail = email
                    showResetDialog = true
                },
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Pilares de Confianza y Firma Oficial "Una creación de AnAnApps"
            LoginTrustPillars()

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 3. Diálogo Modal de Recuperación de Contraseña
        if (showResetDialog) {
            PasswordResetDialog(
                initialEmail = resetDialogEmail,
                onDismiss = { showResetDialog = false },
                onSendResetEmail = onSendPasswordReset
            )
        }
    }
}
