// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/TransferBetweenWalletsDialog.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Diálogo modal flotante centrado de 3 capas para transferencias inter-bolsillos (Orden #037).
// Permite mover saldos entre Efectivo, Digital (Nequi/Davi), Uber y DiDi.
// Cabecera fija con 'X', formulario con scroll interno y botón inferior fijo de 56dp.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.TransferType
import com.ananapps.kilometa.domain.model.WalletType
import com.ananapps.kilometa.presentation.theme.*
import java.util.Locale

@Composable
fun TransferBetweenWalletsDialog(
    onDismiss: () -> Unit,
    onConfirmTransfer: (
        source: WalletType,
        destination: WalletType,
        amount: Double,
        type: TransferType,
        notes: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var sourceWallet by remember { mutableStateOf(WalletType.DIGITAL_WALLET) }
    var destinationWallet by remember { mutableStateOf(WalletType.CASH_PHYSICAL) }
    var amountText by remember { mutableStateOf("50000") }
    var selectedType by remember { mutableStateOf(TransferType.CASH_WITHDRAWAL) }
    var notesText by remember { mutableStateOf("") }

    val amountDouble = amountText.toDoubleOrNull() ?: 0.0

    fun updateWallets(newSource: WalletType, newDest: WalletType) {
        sourceWallet = newSource
        destinationWallet = newDest
        selectedType = when {
            newDest == WalletType.CAR_QUOTA -> TransferType.QUOTA_PROVISION
            newSource in listOf(WalletType.UBER, WalletType.DIDI) -> TransferType.PLATFORM_PAYOUT
            newSource == WalletType.CASH_PHYSICAL && newDest == WalletType.DIGITAL_WALLET -> TransferType.CASH_DEPOSIT
            newSource == WalletType.DIGITAL_WALLET && newDest == WalletType.CASH_PHYSICAL -> TransferType.CASH_WITHDRAWAL
            newDest in listOf(WalletType.UBER, WalletType.DIDI) -> TransferType.COMMISSION_PAYMENT
            else -> TransferType.CASH_DEPOSIT
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .border(1.dp, DeepObsidianBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = DeepObsidianSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // CAPA 1: CABECERA FIJA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⇄ TRANSFERENCIA DE FONDOS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ElectricBluePrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // CAPA 2: CUERPO DESPLAZABLE INTERNO
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Desde (Origen de fondos):", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        WalletType.entries.chunked(2).forEach { pair ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { wallet ->
                                    FilterChip(
                                        selected = sourceWallet == wallet,
                                        onClick = {
                                            val nextDest = if (destinationWallet == wallet) {
                                                WalletType.entries.first { it != wallet }
                                            } else destinationWallet
                                            updateWallets(wallet, nextDest)
                                        },
                                        label = { Text(wallet.displayName, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), maxLines = 1) },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = ElectricBluePrimary,
                                            selectedLabelColor = TextPrimary
                                        )
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Text(text = "Hacia (Destino de fondos):", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        WalletType.entries.chunked(2).forEach { pair ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { wallet ->
                                    val isSame = sourceWallet == wallet
                                    FilterChip(
                                        selected = destinationWallet == wallet,
                                        onClick = { if (!isSame) updateWallets(sourceWallet, wallet) },
                                        enabled = !isSame,
                                        label = { Text(wallet.displayName, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), maxLines = 1) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Text("$", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ElectricBluePrimary, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                        },
                        label = { Text("Monto a Transferir ($ COP)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(text = "Tipo de Movimiento:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TransferType.entries.chunked(2).forEach { pair ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { type ->
                                    FilterChip(
                                        selected = selectedType == type,
                                        onClick = { selectedType = type },
                                        label = { Text(type.displayName, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), maxLines = 1) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Notas / Referencia (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CAPA 3: BOTONERA DE ACCIÓN FIJA
                Button(
                    onClick = {
                        if (amountDouble > 0.0 && sourceWallet != destinationWallet) {
                            onConfirmTransfer(
                                sourceWallet,
                                destinationWallet,
                                amountDouble,
                                selectedType,
                                notesText.ifBlank { null }
                            )
                            onDismiss()
                        }
                    },
                    enabled = amountDouble > 0.0 && sourceWallet != destinationWallet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Constants.MIN_TOUCH_TARGET_DP.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                ) {
                    Text(
                        text = if (amountDouble > 0.0) "TRANSFERIR ${formatCop(amountDouble)}" else "TRANSFERIR",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

private fun formatCop(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return String.format(Locale.GERMANY, "$%,d COP", rounded)
}
