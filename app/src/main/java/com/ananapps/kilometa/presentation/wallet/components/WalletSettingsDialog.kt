package com.ananapps.kilometa.presentation.wallet.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ananapps.kilometa.presentation.theme.*
import com.ananapps.kilometa.presentation.wallet.WalletSettingsViewModel

/**
 * Diálogo modal para la gestión y auditoría de bolsillos y billeteras soberanas (Orden #054).
 * Regla inquebrantable: Modularidad estricta < 280 líneas de código.
 */
@Composable
fun WalletSettingsDialog(
    viewModel: WalletSettingsViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wallets by viewModel.wallets.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val protectedWallets = wallets.filter { it.isSystemProtected }
    val appWallets = wallets.filter { !it.isSystemProtected && it.category == "APP_PLATFORM" }
    val customWallets = wallets.filter { !it.isSystemProtected && it.category != "APP_PLATFORM" }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = DeepObsidianBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                // Cabecera con botón de cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Gestión de Bolsillos",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Audita saldos y billeteras del sistema",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                    }
                }

                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = StatusRedContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = error, color = StatusRed, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("Entendido", color = StatusRed, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sección 1: Bolsillos del Sistema
                    if (protectedWallets.isNotEmpty()) {
                        item {
                            Text(
                                text = "🛡️ BOLSILLOS DEL SISTEMA (INMUTABLES)",
                                color = TextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(protectedWallets, key = { it.id }) { wallet ->
                            WalletItemRow(
                                wallet = wallet,
                                onToggleActive = { viewModel.toggleWalletActive(wallet.id, it) }
                            )
                        }
                    }

                    // Sección 2: Billeteras de Apps
                    if (appWallets.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "📱 BILLETERAS DE APLICACIONES",
                                color = TextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(appWallets, key = { it.id }) { wallet ->
                            WalletItemRow(
                                wallet = wallet,
                                onToggleActive = { viewModel.toggleWalletActive(wallet.id, it) }
                            )
                        }
                    }

                    // Sección 3: Ahorro y Cuotas
                    if (customWallets.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🎯 AHORRO Y CUOTAS (PERSONALIZADOS)",
                                color = TextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(customWallets, key = { it.id }) { wallet ->
                            WalletItemRow(
                                wallet = wallet,
                                onToggleActive = { viewModel.toggleWalletActive(wallet.id, it) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
