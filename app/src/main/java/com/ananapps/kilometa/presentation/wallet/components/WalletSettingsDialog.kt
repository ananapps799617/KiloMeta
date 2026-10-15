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
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Add
import androidx.hilt.navigation.compose.hiltViewModel
import com.ananapps.kilometa.presentation.theme.*
import com.ananapps.kilometa.presentation.wallet.WalletSettingsViewModel

/**
 * Diálogo modal para la gestión y auditoría de bolsillos y billeteras soberanas (Orden #054 / #056).
 * Permite edición visual de bolsillos del sistema y borrado definitivo de personalizados.
 * Regla inquebrantable: Modularidad estricta < 280 líneas de código.
 */
@Composable
fun WalletSettingsDialog(
    viewModel: WalletSettingsViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wallets by viewModel.wallets.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isSheetOpen by viewModel.isSheetOpen.collectAsStateWithLifecycle()
    val editingWallet by viewModel.editingWallet.collectAsStateWithLifecycle()

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
            Box(modifier = Modifier.fillMaxSize()) {
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
                                text = "Audita saldos, categorías y crea billeteras",
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
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
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
                                    onToggleActive = { viewModel.toggleWalletActive(wallet.id, it) },
                                    onClick = { viewModel.openEditWallet(wallet) }
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
                                    onToggleActive = { viewModel.toggleWalletActive(wallet.id, it) },
                                    onClick = { viewModel.openEditWallet(wallet) }
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
                                    onToggleActive = { viewModel.toggleWalletActive(wallet.id, it) },
                                    onClick = { viewModel.openEditWallet(wallet) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }

                // Botón inferior para agregar nuevo bolsillo
                Button(
                    onClick = { viewModel.openCreateWallet() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBluePrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nuevo Bolsillo",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (isSheetOpen) {
        WalletEditSheet(
            wallet = editingWallet,
            onDismiss = { viewModel.closeSheet() },
            onSave = {
                viewModel.saveWallet(it)
                viewModel.closeSheet()
            },
            onDelete = {
                viewModel.deleteWallet(it)
            }
        )
    }
}
