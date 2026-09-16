// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/common/CockpitFloatingScaffold.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Andamiaje modal flotante centrado de 3 capas para ventanas de captura en cabina (Orden #037).
// Capa 1: Cabecera fija con botón volver/cerrar.
// Capa 2: Contenedor con scroll vertical, imePadding y aislamiento de eventos.
// Capa 3: Pie fijo con botón inamovible de 56dp y confirmación de descarte.
// ==============================================================================

package com.ananapps.kilometa.presentation.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ananapps.kilometa.presentation.home.components.DiscardChangesDialog
import com.ananapps.kilometa.presentation.theme.DeepObsidianBorder
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface

@Composable
fun CockpitFloatingScaffold(
    title: String,
    headerColor: Color,
    onDismiss: () -> Unit,
    confirmButtonText: String,
    confirmButtonColor: Color,
    isConfirmEnabled: Boolean,
    onConfirm: () -> Unit,
    hasDirtyData: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var showDiscardDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (hasDirtyData) showDiscardDialog = true else onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .imePadding()
                .border(1.dp, DeepObsidianBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = DeepObsidianSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // CAPA 1: CABECERA FIJA
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = headerColor,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (hasDirtyData) showDiscardDialog = true else onDismiss() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        IconButton(onClick = { if (hasDirtyData) showDiscardDialog = true else onDismiss() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CAPA 2: CUERPO SCROLLABLE
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    content()
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // CAPA 3: PIE FIJO INAMOVIBLE (56dp)
                Surface(
                    color = DeepObsidianSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = isConfirmEnabled,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = confirmButtonColor,
                            disabledContainerColor = confirmButtonColor.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = confirmButtonText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            onConfirmDiscard = onDismiss,
            onDismissRequest = { showDiscardDialog = false }
        )
    }
}
