// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/history/components/DayQuickActionRow.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Fila de acciones rápidas contextuales incrustada en cada tarjeta de día (Orden #043).
// Permite añadir carreras, tanqueos, gastos o mantenimientos pre-seleccionando la fecha.
// ==============================================================================

package com.ananapps.kilometa.presentation.history.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.presentation.theme.*

@Composable
fun DayQuickActionRow(
    onAddTrip: () -> Unit,
    onAddFueling: () -> Unit,
    onAddExpense: () -> Unit,
    onAddMaintenance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "REGISTRAR EN ESTE DÍA",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = TextSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuickActionButton(
                label = "+ Carrera",
                color = ElectricBluePrimary,
                onClick = onAddTrip,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "+ Tanqueo",
                color = Color(0xFFFF7A00),
                onClick = onAddFueling,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "+ Gasto",
                color = Color(0xFFE53935),
                onClick = onAddExpense,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "+ Mant",
                color = Color(0xFFAB47BC),
                onClick = onAddMaintenance,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = color.copy(alpha = 0.12f),
            contentColor = color
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 1
        )
    }
}
