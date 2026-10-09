package com.app.rumbo.core.util

import java.util.Locale

private val esCO = Locale.forLanguageTag("es-CO")

/** 12.4 -> "12,4 km" */
fun Double.formatKm(): String = String.format(esCO, "%.1f km", this)

/** 1284 -> "1.284" */
fun Int.formatThousands(): String = String.format(esCO, "%,d", this)

/** Tiempo transcurrido en el formato del prototipo: "hace 3 h", "ayer", "hace 1 sem". */
fun Long.toTimeAgo(now: Long = System.currentTimeMillis()): String {
    val minutes = (now - this) / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "ahora"
        minutes < 60 -> "hace $minutes min"
        hours < 24 -> "hace $hours h"
        days < 2 -> "ayer"
        days < 7 -> "hace $days d"
        else -> "hace ${days / 7} sem"
    }
}
