package com.app.rumbo.domain.model

/** Tema de la app: sigue al sistema o se fuerza claro u oscuro. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Tamaño de letra elegido por el usuario; [factor] multiplica el tamaño de letra del sistema. */
enum class TextScale(val factor: Float) {
    SMALL(0.85f),
    NORMAL(1f),
    LARGE(1.15f),
    EXTRA_LARGE(1.3f)
}

/** Preferencias de accesibilidad y apariencia que se guardan en el dispositivo. */
data class AppearanceSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val textScale: TextScale = TextScale.NORMAL
)
