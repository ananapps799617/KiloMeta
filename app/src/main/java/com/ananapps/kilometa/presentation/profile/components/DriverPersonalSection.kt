// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/profile/components/DriverPersonalSection.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Módulo de datos personales del conductor: nombre, teléfono, correo institucional,
// selector de género ergonómico y disparador de reseteo seguro de clave.
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
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.domain.model.Gender
import com.ananapps.kilometa.presentation.theme.DeepObsidianBackground
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary

@Composable
fun DriverPersonalSection(
    driverName: String,
    onDriverNameChange: (String) -> Unit,
    email: String,
    phone: String,
    onPhoneChange: (String) -> Unit,
    gender: Gender,
    onGenderChange: (Gender) -> Unit,
    onRequestPasswordReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "👤 Datos del Conductor",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        OutlinedTextField(
            value = driverName,
            onValueChange = onDriverNameChange,
            label = { Text("Nombre Completo") },
            placeholder = { Text("Ej. Andrés Gómez") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Correo Electrónico (Solo Lectura)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            label = { Text("Teléfono de Contacto") },
            placeholder = { Text("Ej. 310 123 4567") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(text = "Género", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Gender.entries.forEach { item ->
                FilterChip(
                    selected = gender == item,
                    onClick = { onGenderChange(item) },
                    label = { Text(item.displayName) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBluePrimary,
                        selectedLabelColor = DeepObsidianBackground
                    )
                )
            }
        }

        OutlinedButton(
            onClick = onRequestPasswordReset,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            Text(text = "🔒 Restablecer Contraseña por Correo")
        }
    }
}
