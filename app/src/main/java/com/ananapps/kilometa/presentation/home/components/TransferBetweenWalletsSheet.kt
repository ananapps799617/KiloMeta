// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/TransferBetweenWalletsSheet.kt
// DESCRIPCIÓN PEDAGÓGICA:
// ModalBottomSheet ergonómico para registrar transferencias inter-bolsillos (Orden #033).
// Permite mover saldos entre Efectivo en Mano, Billetera Digital (Nequi/Davi), Uber y DiDi.
// Regla inquebrantable: Modularidad estricta < 280 líneas de código.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ananapps.kilometa.core.util.Constants
import com.ananapps.kilometa.domain.model.TransferType
import com.ananapps.kilometa.domain.model.WalletType
import com.ananapps.kilometa.presentation.theme.DeepObsidianSurface
import com.ananapps.kilometa.presentation.theme.ElectricBluePrimary
import com.ananapps.kilometa.presentation.theme.TextPrimary
import com.ananapps.kilometa.presentation.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferBetweenWalletsSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirmTransfer: (
        source: WalletType,
        destination: WalletType,
        amount: Double,
        type: TransferType,
        notes: String?
    ) -> Unit
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
            newSource in listOf(WalletType.UBER, WalletType.DIDI) -> TransferType.PLATFORM_PAYOUT
            newSource == WalletType.CASH_PHYSICAL && newDest == WalletType.DIGITAL_WALLET -> TransferType.CASH_DEPOSIT
            newSource == WalletType.DIGITAL_WALLET && newDest == WalletType.CASH_PHYSICAL -> TransferType.CASH_WITHDRAWAL
            newDest in listOf(WalletType.UBER, WalletType.DIDI) -> TransferType.COMMISSION_PAYMENT
            else -> TransferType.CASH_DEPOSIT
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepObsidianSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "⇄ TRANSFERENCIA ENTRE BOLSILLOS",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ElectricBluePrimary
            )

            // Selector Bolsillo Origen
            Text(text = "Desde (Origen de fondos):", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WalletType.entries.forEach { wallet ->
                    FilterChip(
                        selected = sourceWallet == wallet,
                        onClick = {
                            val nextDest = if (destinationWallet == wallet) {
                                WalletType.entries.first { it != wallet }
                            } else destinationWallet
                            updateWallets(wallet, nextDest)
                        },
                        label = { Text(wallet.displayName, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBluePrimary,
                            selectedLabelColor = TextPrimary
                        )
                    )
                }
            }

            // Selector Bolsillo Destino
            Text(text = "Hacia (Destino de fondos):", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WalletType.entries.forEach { wallet ->
                    val isSame = sourceWallet == wallet
                    FilterChip(
                        selected = destinationWallet == wallet,
                        onClick = { if (!isSame) updateWallets(sourceWallet, wallet) },
                        enabled = !isSame,
                        label = { Text(wallet.displayName, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Monto a transferir
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

            // Chips rápidos de monto
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(20_000.0, 50_000.0, 100_000.0, 200_000.0).forEach { chipAmount ->
                    SuggestionChip(
                        onClick = { amountText = chipAmount.toInt().toString() },
                        label = { Text("$${chipAmount.toInt() / 1000}k", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Tipo de Transferencia / Concepto Contable
            Text(text = "Tipo de Movimiento:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TransferType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.displayName) }
                    )
                }
            }

            // Notas opcionales
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notas / Referencia (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Botón de Confirmación Ergonómico (>= 56dp)
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
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
            ) {
                Text(
                    text = "Transferir ${formatCop(amountDouble)}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        }
    }
}

private fun formatCop(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return String.format(Locale.GERMANY, "$%,d COP", rounded)
}
