package com.ananapps.kilometa.presentation.wallet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.core.util.ColorUtils
import com.ananapps.kilometa.domain.model.Wallet
import com.ananapps.kilometa.presentation.theme.*

/**
 * Renglón desacoplado para representar un bolsillo o billetera (Orden #054 / #055).
 * Maneja diferenciación visual inmutable para billeteras del sistema (MND-04) e iconografía vehicular.
 */
@Composable
fun WalletItemRow(
    wallet: Wallet,
    onToggleActive: (Boolean) -> Unit,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val indicatorColor = ColorUtils.parseHexColor(wallet.colorHex)
    val rowAlpha = if (wallet.isActive) 1.0f else 0.5f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(rowAlpha)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = DeepObsidianSurface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador cromático del bolsillo con glifo vehicular (36dp)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(indicatorColor.copy(alpha = 0.2f), CircleShape)
                    .border(2.dp, indicatorColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = wallet.iconName.ifBlank { "💵" },
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Nombre y saldo
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = wallet.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$ ${"%,.0f".format(wallet.balance)}",
                    color = if (wallet.balance < 0) StatusRed else TextSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Control de estado: Inmutable si es del sistema, Switch si es personalizable
            if (wallet.isSystemProtected) {
                Surface(
                    color = StatusGreenContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "🔒 Protegido",
                        color = StatusGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Switch(
                    checked = wallet.isActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ElectricBluePrimary,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = DeepObsidianSurfaceVariant
                    )
                )
            }
        }
    }
}
