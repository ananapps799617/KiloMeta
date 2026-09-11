// ==============================================================================
// PROYECTO: KiloMeta (Ecosistema AnAnApps v2.0)
// ARCHIVO: app/src/main/java/com/ananapps/kilometa/presentation/home/components/AccountingFormatters.kt
// DESCRIPCIÓN PEDAGÓGICA:
// Utilidades puras de formateo monetario, métricas de cabina y etiquetas de corte
// semanal para las tarjetas de auditoría contable.
// ==============================================================================

package com.ananapps.kilometa.presentation.home.components

import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.util.Locale



// ------------------------------------------------------------------------------
// BLOQUE 1: FORMATEADORES MONETARIOS Y DE CABINA
// ------------------------------------------------------------------------------
fun formatAccountingCop(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return if (rounded < 0) {
        String.format(Locale.GERMANY, "-$%,d", -rounded)
    } else {
        String.format(Locale.GERMANY, "$%,d", rounded)
    }
}

fun formatAccountingCopPerKm(amount: Double): String {
    val rounded = kotlin.math.round(amount).toLong()
    return if (rounded < 0) {
        String.format(Locale.GERMANY, "-$%,d/km", -rounded)
    } else {
        String.format(Locale.GERMANY, "$%,d/km", rounded)
    }
}

// ------------------------------------------------------------------------------
// BLOQUE 2: FORMATEADOR DE BADGE DE CORTE SEMANAL Y RANGO DE FECHAS
// ------------------------------------------------------------------------------
/**
 * Formatea el rango de fechas de corte del ciclo semanal (ej. "10 Sep – 16 Sep").
 */
fun formatCycleDateRange(
    cycleStartDateMillis: Long,
    cycleEndDateMillis: Long,
    zoneId: ZoneId = ZoneId.of("America/Bogota")
): String {
    val start = Instant.ofEpochMilli(cycleStartDateMillis).atZone(zoneId)
    val end = Instant.ofEpochMilli(cycleEndDateMillis).atZone(zoneId)
    val monthNames = arrayOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    val startMonth = monthNames.getOrElse(start.monthValue - 1) { "Sep" }
    val endMonth = monthNames.getOrElse(end.monthValue - 1) { "Sep" }
    return "${start.dayOfMonth} $startMonth – ${end.dayOfMonth} $endMonth"
}

/**
 * Genera la etiqueta dinámica para el badge de corte en la tarjeta de auditoría.
 * Ejemplo: "Corte Mar 18:00" o "Corte Dom 20:00".
 */
fun formatCutoffBadge(
    cycleEndDateMillis: Long,
    zoneId: ZoneId = ZoneId.of("America/Bogota")
): String {
    val endDateTime = Instant.ofEpochMilli(cycleEndDateMillis).atZone(zoneId)
    val dayName = when (endDateTime.dayOfWeek) {
        DayOfWeek.MONDAY -> "Lun"
        DayOfWeek.TUESDAY -> "Mar"
        DayOfWeek.WEDNESDAY -> "Mié"
        DayOfWeek.THURSDAY -> "Jue"
        DayOfWeek.FRIDAY -> "Vie"
        DayOfWeek.SATURDAY -> "Sáb"
        DayOfWeek.SUNDAY -> "Dom"
        null -> "Mar"
    }

    val hourStr = if (endDateTime.minute == 59 && endDateTime.hour == 23) {
        "23:59"
    } else {
        String.format(Locale.ROOT, "%02d:%02d", endDateTime.hour, endDateTime.minute)
    }

    return "Corte $dayName $hourStr"
}
