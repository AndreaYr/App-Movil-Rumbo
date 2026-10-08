package com.app.rumbo.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// El prototipo solo define paleta clara; el modo oscuro queda pendiente de diseño
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

@Composable
fun RumboTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
