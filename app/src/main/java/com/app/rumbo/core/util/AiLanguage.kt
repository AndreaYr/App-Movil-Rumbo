package com.app.rumbo.core.util

import java.util.Locale

/**
 * Idioma ("es" o "en") en que la IA debe escribir, según el idioma activo de la app.
 * Cualquier idioma distinto de inglés se atiende en español.
 */
fun currentAiLanguage(): String = if (Locale.getDefault().language == "en") "en" else "es"
