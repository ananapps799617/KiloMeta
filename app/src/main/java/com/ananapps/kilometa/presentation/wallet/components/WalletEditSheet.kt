package com.ananapps.kilometa.presentation.wallet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.core.util.ColorUtils
import com.ananapps.kilometa.domain.model.Wallet
import com.ananapps.kilometa.presentation.theme.*
import java.util.UUID

private val WALLET_PALETTE = listOf(
    "#00E676", "#00B0FF", "#FFB800", "#7145D6",
    "#FF5252", "#E040FB", "#00E5FF", "#78909C"
)

private val WALLET_CATEGORIES = listOf(
    "SAVINGS" to "Ahorro",
    "APP_PLATFORM" to "Plataforma",
    "DIGITAL" to "Billetera Digital",
    "EXPENSE" to "Gastos Ruta",
    "OTHER" to "Personalizado"
)

/**
 * Hoja modal inferior para crear o editar bolsillos/billeteras personalizadas (Orden #054).
 * Regla inquebrantable: Modularidad estricta < 280 líneas de código.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletEditSheet(
    wallet: Wallet?,
    onDismiss: () -> Unit,
    onSave: (Wallet) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember(wallet) { mutableStateOf(wallet?.name.orEmpty()) }
    var category by remember(wallet) { mutableStateOf(wallet?.category ?: "SAVINGS") }
    var colorHex by remember(wallet) { mutableStateOf(wallet?.colorHex ?: "#00E676") }

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
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Nombre
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Bolsillo") },
                placeholder = { Text("Ej. Ahorro Vacaciones, Billetera Cabify...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBluePrimary,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Categoría
            Text(
                text = "Categoría Contable",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
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

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Color
            Text(
                text = "Color Distintivo",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(WALLET_PALETTE) { hex ->
                    val color = ColorUtils.parseHexColor(hex)
                    val isSelected = colorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(color, CircleShape)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else ObsidianBorder,
                                shape = CircleShape
                            )
                            .clickable { colorHex = hex }
                    )
                }
            }

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
                                category = category,
                                colorHex = colorHex,
                                isSystemProtected = wallet?.isSystemProtected ?: false,
                                isActive = wallet?.isActive ?: true,
                                balance = wallet?.balance ?: 0.0
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
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
        }
    }
}
