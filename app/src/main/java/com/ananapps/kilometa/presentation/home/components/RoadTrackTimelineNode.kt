// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/RoadTrackTimelineNode.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Nodo gráfico de la Pista de Asfalto Vertical Continua (Road Track estilo Drivvo).
// Renderiza un carril de asfalto oscuro con líneas centrales discontinuas y el
// pin circular del evento (Carrera, Combustible, Gasto, Servicio, Transferencia).
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ananapps.kilometa.domain.model.DayActivityEvent
import com.ananapps.kilometa.presentation.theme.FuelGasoline
import com.ananapps.kilometa.presentation.theme.StatusGreen
import com.ananapps.kilometa.presentation.theme.StatusRed

private val AsphaltBackground = Color(0xFF14171E)
private val AsphaltBorder = Color(0xFF222733)
private val RoadLaneDashColor = Color(0xFFE6EDF3).copy(alpha = 0.35f)
private val MaintenancePinColor = Color(0xFF8D6E63)
private val TransferPinColor = Color(0xFF7E57C2)

@Composable
fun RoadTrackTimelineNode(
    event: DayActivityEvent,
    modifier: Modifier = Modifier
) {
    val (pinColor, pinEmoji) = when (event) {
        is DayActivityEvent.TripItem -> Pair(StatusGreen, "🚗")
        is DayActivityEvent.FuelingItem -> Pair(FuelGasoline, "⛽")
        is DayActivityEvent.ExpenseItem -> Pair(StatusRed, "🔴")
        is DayActivityEvent.TransferItem -> Pair(TransferPinColor, "⇄")
        is DayActivityEvent.MaintenanceItem -> Pair(MaintenancePinColor, "🔧")
    }

    Box(
        modifier = modifier
            .width(36.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Pista de Asfalto con Línea Central Segmentada
        Canvas(
            modifier = Modifier
                .width(18.dp)
                .fillMaxHeight()
                .background(AsphaltBackground)
        ) {
            val strokeWidth = 2.dp.toPx()
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
            val centerX = size.width / 2f

            drawLine(
                color = RoadLaneDashColor,
                start = Offset(centerX, 0f),
                end = Offset(centerX, size.height),
                strokeWidth = strokeWidth,
                pathEffect = dashEffect
            )

            drawLine(
                color = AsphaltBorder,
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = AsphaltBorder,
                start = Offset(size.width, 0f),
                end = Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 2. Pin Circular Elevado Temático
        Box(
            modifier = Modifier
                .size(28.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(pinColor)
                .border(2.dp, Color.Black.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = pinEmoji, fontSize = 13.sp)
        }
    }
}
