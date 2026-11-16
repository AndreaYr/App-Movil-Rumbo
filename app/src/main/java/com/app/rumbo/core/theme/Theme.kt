package com.app.rumbo.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.app.rumbo.domain.model.TextScale
import com.app.rumbo.domain.model.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = White,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealDark,
    secondary = Coral,
    onSecondary = White,
    secondaryContainer = CoralContainer,
    tertiary = Gold,
    onTertiary = Ink,
    tertiaryContainer = GoldContainer,
    background = Sand,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = InkMuted,
    outline = Outline,
    outlineVariant = Outline,
    error = ErrorRed,
    errorContainer = ErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkTeal,
    onPrimary = White,
    primaryContainer = DarkTealContainer,
    onPrimaryContainer = DarkOnTealContainer,
    secondary = Coral,
    onSecondary = White,
    secondaryContainer = DarkCoralContainer,
    onSecondaryContainer = White,
    tertiary = Gold,
    onTertiary = Ink,
    tertiaryContainer = DarkGoldContainer,
    onTertiaryContainer = White,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = Color(0xFF0C1210),
    surfaceContainerLow = Color(0xFF161E1C),
    surfaceContainer = Color(0xFF1A2321),
    surfaceContainerHigh = Color(0xFF212B28),
    surfaceContainerHighest = Color(0xFF28332F),
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = DarkError,
    onError = Ink,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkError
)

/** Indica si el tema oscuro está activo; sirve para elegir colores propios de cada tema. */
val LocalDarkTheme = staticCompositionLocalOf { false }

/** Devuelve [light] en el tema claro y [dark] en el oscuro. */
@Composable
@ReadOnlyComposable
fun adaptiveColor(light: Color, dark: Color): Color = if (LocalDarkTheme.current) dark else light

/** Verde de la marca para texto e iconos: en el tema oscuro es más claro para que contraste con el fondo. */
@Composable
@ReadOnlyComposable
fun accentTeal(): Color = adaptiveColor(Teal, DarkTealText)

/** Naranja de acción para texto, bordes e iconos sobre la superficie. */
@Composable
@ReadOnlyComposable
fun accentCoral(): Color = adaptiveColor(Coral, DarkCoralText)

/** Dorado para estrellas y datos destacados sobre la superficie. */
@Composable
@ReadOnlyComposable
fun accentGold(): Color = adaptiveColor(Color(0xFFB4841A), DarkGoldText)

/**
 * Tema de Rumbo. [themeMode] decide entre claro, oscuro o el del sistema, y [textScale]
 * multiplica el tamaño de letra del sistema para quien necesita letra más grande o más pequeña.
 */
@Composable
fun RumboTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    textScale: TextScale = TextScale.NORMAL,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val density = LocalDensity.current
    val scaledDensity = Density(density.density, density.fontScale * textScale.factor)

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalDensity provides scaledDensity
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
