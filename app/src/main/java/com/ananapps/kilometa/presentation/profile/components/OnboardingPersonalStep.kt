// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/OnboardingPersonalStep.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Paso 1 del Onboarding / Edición: Identidad humana del conductor (PII protegida).
// Integra banner de bienvenida institucional en modo onboarding, nombre, correo
// de solo lectura, teléfono numérico, selector ergonómico de género y reset de clave.
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.Gender
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

@Composable
fun OnboardingPersonalStep(
    driverName: String,
    onDriverNameChange: (String) -> Unit,
    email: String,
    phone: String,
    onPhoneChange: (String) -> Unit,
    gender: Gender,
    onGenderChange: (Gender) -> Unit,
    onRequestPasswordReset: () -> Unit,
    isOnboardingMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner de Bienvenida Institucional (Solo en Onboarding)
        if (isOnboardingMode) {
            OnboardingWelcomeBanner()
        }

        // Título del Paso
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "👤 Identidad del Conductor",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Ingresa tus datos personales para personalizar tu cabina operativa.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Campo 1: Nombre del conductor
        OutlinedTextField(
            value = driverName,
            onValueChange = onDriverNameChange,
            label = { Text("Nombre Completo") },
            placeholder = { Text("Ej. Andrés Gómez") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Campo 2: Correo institucional / usuario (Solo lectura)
        OutlinedTextField(
            value = email,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Correo Electrónico (Solo Lectura)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Campo 3: Teléfono con teclado numérico
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            label = { Text("Teléfono de Contacto") },
            placeholder = { Text("Ej. 310 123 4567") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Campo 4: Selector de Género ergonómico (evita truncamiento de texto)
        Text(
            text = "Género",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(Gender.MALE, Gender.FEMALE).forEach { item ->
                FilterChip(
                    selected = gender == item,
                    onClick = { onGenderChange(item) },
                    label = {
                        Text(
                            text = item.displayName,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBluePrimary,
                        selectedLabelColor = DeepObsidianBackground
                    )
                )
            }
        }
        FilterChip(
            selected = gender == Gender.PREFER_NOT_TO_SAY,
            onClick = { onGenderChange(Gender.PREFER_NOT_TO_SAY) },
            label = {
                Text(
                    text = Gender.PREFER_NOT_TO_SAY.displayName,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ElectricBluePrimary,
                selectedLabelColor = DeepObsidianBackground
            )
        )

        // Botón de restablecimiento de contraseña vía Firebase Auth
        OutlinedButton(
            onClick = onRequestPasswordReset,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text(text = "🔒 Restablecer Contraseña por Correo")
        }
    }
}
