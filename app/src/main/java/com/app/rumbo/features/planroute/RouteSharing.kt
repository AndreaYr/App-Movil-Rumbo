package com.app.rumbo.features.planroute

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.net.toUri
import com.app.rumbo.R
import com.app.rumbo.domain.model.SavedRoute
import com.app.rumbo.domain.model.StartPoint
import com.app.rumbo.domain.model.Transport

/** Formas de compartir una ruta; cada una se muestra con su icono en la hoja "Compartir ruta". */
enum class ShareTarget(val label: String, @DrawableRes val icon: Int) {
    LINK("Copiar enlace", R.drawable.ic_link),
    WHATSAPP("WhatsApp", R.drawable.ic_whatsapp),
    FACEBOOK("Facebook", R.drawable.ic_facebook),
    INSTAGRAM("Instagram", R.drawable.ic_instagram),
    EMAIL("Correo electrónico", R.drawable.ic_mail)
}

/** Enlace que abre el recorrido completo en Google Maps (app o navegador), con el transporte elegido. */
internal fun directionsUrl(route: SavedRoute, start: StartPoint? = null): String {
    val coordinates = route.stops.map { "${it.latitude},${it.longitude}" }
    val mode = when (route.transport) {
        Transport.WALK -> "walking"
        Transport.BIKE -> "bicycling"
        Transport.MOTO -> "two-wheeler"
        Transport.CAR -> "driving"
    }
    return buildString {
        append("https://www.google.com/maps/dir/?api=1")
        if (start != null) append("&origin=").append("${start.latitude},${start.longitude}")
        append("&destination=").append(coordinates.last())
        append("&travelmode=").append(mode)
        if (coordinates.size > 1) {
            append("&waypoints=").append(Uri.encode(coordinates.dropLast(1).joinToString("|")))
        }
    }
}

internal fun shareText(route: SavedRoute, url: String, transportLabel: String): String =
    "Mira mi ruta en Rumbo: ${route.name} · ${route.stops.size} paradas · $transportLabel\n$url"

/** Comparte la ruta por [target]. Si la app elegida no está instalada, avisa en lugar de fallar. */
internal fun Context.shareRoute(route: SavedRoute, start: StartPoint?, target: ShareTarget) {
    if (route.stops.isEmpty()) return
    val url = directionsUrl(route, start)
    val text = shareText(route, url, getString(route.transport.labelRes))
    when (target) {
        ShareTarget.LINK -> {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Ruta de Rumbo", url))
            Toast.makeText(this, "Enlace copiado", Toast.LENGTH_SHORT).show()
        }
        ShareTarget.WHATSAPP -> sendToApp(text, "com.whatsapp", "WhatsApp")
        ShareTarget.INSTAGRAM -> sendToApp(text, "com.instagram.android", "Instagram")
        ShareTarget.FACEBOOK -> {
            // Facebook casi no acepta texto desde otras apps, así que si no abre se usa su página para compartir enlaces
            val opened = sendToApp(text, "com.facebook.katana", null)
            if (!opened) openUri("https://www.facebook.com/sharer/sharer.php?u=${Uri.encode(url)}".toUri())
        }
        ShareTarget.EMAIL -> {
            val intent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri()).apply {
                putExtra(Intent.EXTRA_SUBJECT, "Mi ruta en Rumbo: ${route.name}")
                putExtra(Intent.EXTRA_TEXT, text)
            }
            launch(intent, "No hay una app de correo")
        }
    }
}

/** Envía texto a una app concreta; con [appName] null no avisa si falla y devuelve false. */
private fun Context.sendToApp(text: String, packageName: String, appName: String?): Boolean {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        setPackage(packageName)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    return launch(intent, appName?.let { "$it no está instalado" })
}

private fun Context.openUri(uri: Uri) {
    launch(Intent(Intent.ACTION_VIEW, uri), "No hay una app para abrir el enlace")
}

private fun Context.launch(intent: Intent, errorMessage: String?): Boolean = try {
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (_: ActivityNotFoundException) {
    errorMessage?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
    false
}
