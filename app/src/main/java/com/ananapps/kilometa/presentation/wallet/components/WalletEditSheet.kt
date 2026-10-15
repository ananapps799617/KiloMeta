package com.ananapps.kilometa.presentation.wallet.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.core.util.IconCatalog
import com.ananapps.kilometa.domain.model.Wallet
import com.ananapps.kilometa.presentation.common.components.VehicularColorSelector
import com.ananapps.kilometa.presentation.common.components.VehicularDeleteConfirmationDialog
import com.ananapps.kilometa.presentation.common.components.VehicularIconSelector
import com.ananapps.kilometa.presentation.theme.*
import java.util.UUID
import kotlin.math.abs

private val WALLET_CATEGORIES = listOf(
    "SAVINGS" to "Ahorro", "APP_PLATFORM" to "Plataforma",
    "DIGITAL" to "Billetera Digital", "EXPENSE" to "Gastos Ruta", "OTHER" to "Personalizado"
)

/**
 * Hoja modal inferior para crear o editar bolsillos/billeteras (Orden #054 / #055 / #056).
 * Permite edición visual de bolsillos del sistema y borrado definitivo de personalizados.
 * Modularidad estricta < 280 LOC.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletEditSheet(
    wallet: Wallet?,
    onDismiss: () -> Unit,
    onSave: (Wallet) -> Unit,
    onDelete: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var name by remember(wallet) { mutableStateOf(wallet?.name.orEmpty()) }
    var category by remember(wallet) { mutableStateOf(wallet?.category ?: "SAVINGS") }
    var iconName by remember(wallet) { mutableStateOf(wallet?.iconName ?: IconCatalog.DEFAULT_WALLET_ICON) }
    var colorHex by remember(wallet) { mutableStateOf(wallet?.colorHex ?: "#00E676") }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showBalanceWarningDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DeepObsidianSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = ObsidianBorder) },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (wallet == null) "Nuevo Bolsillo / Billetera" else "Editar Bolsillo",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Personaliza tus cuentas y categorías de saldo",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Badge informativo si es bolsillo protegido del sistema
            if (wallet?.isSystemProtected == true) {
                Surface(
                    color = StatusGreenContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "🛡️ Bolsillo del Sistema (Protegido)",
                        color = StatusGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Bolsillo") },
                placeholder = { Text("Ej. Ahorro Vacaciones, Mi Nequi...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBluePrimary,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Selector de Categoría Contable (solo para bolsillos de usuario)
            if (wallet?.isSystemProtected != true) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Categoría Contable", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(WALLET_CATEGORIES) { (catKey, catLabel) ->
                        val isSelected = category == catKey
                        Surface(
                            color = if (isSelected) ElectricBluePrimary else DeepObsidianSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { category = catKey }
                        ) {
                            Text(
                                text = catLabel,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Icono
            Text("Icono Distintivo", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            VehicularIconSelector(selectedIcon = iconName, onIconSelected = { iconName = it })

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Color
            Text("Color Distintivo", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            VehicularColorSelector(selectedColorHex = colorHex, onColorSelected = { colorHex = it })

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Guardar
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val generatedId = wallet?.id ?: "wallet_${name.trim().lowercase().replace("\\s+".toRegex(), "_")}_${UUID.randomUUID().toString().take(6)}"
                        onSave(
                            Wallet(
                                id = generatedId,
                                name = name.trim(),
                                category = if (wallet?.isSystemProtected == true) wallet.category else category,
                                iconName = iconName,
                                colorHex = colorHex,
                                isSystemProtected = wallet?.isSystemProtected ?: false,
                                isActive = wallet?.isActive ?: true,
                                balance = wallet?.balance ?: 0.0
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBluePrimary,
                    disabledContainerColor = ObsidianBorder
                )
            ) {
                Text(
                    text = if (wallet == null) "Crear Bolsillo" else "Guardar Cambios",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Botón Eliminar Bolsillo (Solo para bolsillos de usuario)
            if (wallet != null && !wallet.isSystemProtected) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        if (abs(wallet.balance) >= 0.01) showBalanceWarningDialog = true
                        else showDeleteConfirmDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = StatusRedContainer,
                        contentColor = StatusRed
                    ),
                    border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.35f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = StatusRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Bolsillo", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showDeleteConfirmDialog && wallet != null) {
        VehicularDeleteConfirmationDialog(
            title = "Eliminar Bolsillo",
            message = "¿Estás seguro de que deseas eliminar permanentemente el bolsillo '${wallet.name}'? Esta acción no se puede deshacer.",
            confirmButtonText = "Sí, Eliminar",
            onConfirm = {
                showDeleteConfirmDialog = false
                onDelete?.invoke(wallet.id)
                onDismiss()
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    if (showBalanceWarningDialog && wallet != null) {
        AlertDialog(
            onDismissRequest = { showBalanceWarningDialog = false },
            title = { Text("Saldo Activo en Bolsillo", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Text(
                    text = "El bolsillo '${wallet.name}' tiene un saldo activo de $ ${"%,.0f".format(wallet.balance)}. Transfiere primero los fondos a otro bolsillo antes de eliminarlo.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showBalanceWarningDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                ) { Text("Entendido", color = Color.White) }
            },
            containerColor = DeepObsidianSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
