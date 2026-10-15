// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/OnboardingPersonalStep.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Pestaña 1 del Onboarding: Identidad humana del conductor (PII protegida).
// Campos oscuros (#121826) con bordes discretos, iconos esmeralda y correo vinculado (< 280 LOC).
// ==============================================================================

package com.ananapps.kilometa.presentation.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.Gender
import com.ananapps.kilometa.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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
    var genderDropdownExpanded by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = EmeraldAccent,
        unfocusedBorderColor = ObsidianBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedContainerColor = DeepObsidianSurface,
        unfocusedContainerColor = DeepObsidianSurface,
        disabledContainerColor = DeepObsidianSurface.copy(alpha = 0.6f),
        disabledBorderColor = ObsidianBorder.copy(alpha = 0.5f),
        disabledTextColor = TextPrimary.copy(alpha = 0.75f)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Título y Subtítulo de Cabina
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Datos personales",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = TextPrimary
            )
            Text(
                text = "Cuéntanos un poco sobre ti para personalizar tu experiencia.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // 2. Campo: Nombre Completo
        OutlinedTextField(
            value = driverName,
            onValueChange = onDriverNameChange,
            label = { Text("Nombre completo *", color = TextSecondary) },
            placeholder = { Text("Tu nombre y apellido", color = TextSecondary.copy(alpha = 0.5f)) },
            leadingIcon = { Text("👤", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp)
        )

        // 3. Campo: Teléfono de Contacto
        OutlinedTextField(
            value = phone,
            onValueChange = { input -> onPhoneChange(input.filter { it.isDigit() || it == '+' || it == ' ' }) },
            label = { Text("Teléfono de contacto *", color = TextSecondary) },
            placeholder = { Text("+57 300 123 4567", color = TextSecondary.copy(alpha = 0.5f)) },
            leadingIcon = { Text("📞", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp)
        )

        // 4. Campo: Correo Electrónico (Protección DataGuard: Solo lectura)
        OutlinedTextField(
            value = email,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Correo electrónico", color = TextSecondary) },
            placeholder = { Text("usuario@ejemplo.com", color = TextSecondary.copy(alpha = 0.5f)) },
            leadingIcon = { Text("🔒", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
            trailingIcon = {
                Text(
                    text = "Vinculado",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldAccent,
                    modifier = Modifier.padding(end = 12.dp)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp)
        )

        // 5. Campo: Selector de Género Dropdown
        ExposedDropdownMenuBox(
            expanded = genderDropdownExpanded,
            onExpandedChange = { genderDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = gender.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Género *", color = TextSecondary) },
                leadingIcon = { Text("👤", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderDropdownExpanded) },
                colors = fieldColors,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = genderDropdownExpanded,
                onDismissRequest = { genderDropdownExpanded = false }
            ) {
                Gender.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (gender == option) EmeraldAccent else TextPrimary
                            )
                        },
                        onClick = {
                            onGenderChange(option)
                            genderDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // 6. Opción auxiliar: Reset de contraseña en modo edición
        if (!isOnboardingMode) {
            TextButton(
                onClick = onRequestPasswordReset,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🔑 ¿Deseas cambiar tu contraseña? Pulsa aquí",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = EmeraldAccent
                )
            }
        }
    }
}
