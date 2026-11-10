package com.app.rumbo.core.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.app.rumbo.R
import java.util.Locale

private val esCO = Locale.forLanguageTag("es-CO")

/** 12.4 -> "12,4 km" */
fun Double.formatKm(): String = String.format(esCO, "%.1f km", this)

/** 1284 -> "1.284" */
fun Int.formatThousands(): String = String.format(esCO, "%,d", this)

/** Tiempo transcurrido en el formato del prototipo: "hace 3 h", "ayer", "hace 1 sem". */
@Composable
fun Long.toTimeAgo(now: Long = System.currentTimeMillis()): String {
    val minutes = (now - this) / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> stringResource(R.string.time_ago_now)
        minutes < 60 -> stringResource(R.string.time_ago_minutes, minutes)
        hours < 24 -> stringResource(R.string.time_ago_hours, hours)
        days < 2 -> stringResource(R.string.time_ago_yesterday)
        days < 7 -> stringResource(R.string.time_ago_days, days)
        else -> stringResource(R.string.time_ago_weeks, days / 7)
    }
}
